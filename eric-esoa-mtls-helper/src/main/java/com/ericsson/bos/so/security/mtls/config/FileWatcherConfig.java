/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.bos.so.security.mtls.config;

import com.ericsson.bos.so.common.logging.security.SecurityLogger;
import com.ericsson.bos.so.security.mtls.MtlsConfigurationReloadersRegister;
import com.ericsson.bos.so.security.mtls.util.KeyStoreUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;

/**
 * This class is not needed to be exclusively called by the client.
 * This class will be called by the library internally and keep things ready
 * for its clients
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
public class FileWatcherConfig implements FileWatchersRegister {

    @Autowired
    private KeyStoreUtil keyStoreUtil;

    @Autowired
    private SecurityConfiguration securityConfiguration;

    /**
     * The method is notified when there is an update to the certificates, because of their renewal. Once certs are
     * updated, this method updates the Server and Client with the renewed certificates, without needing to restart
     * the application
     */
    @PostConstruct
    public void fileWatcherConfig() {
        try {
            registerFileWatcher();
        } catch (Exception exception) {
            log.error("FileWatcherConfig failed to start due to: {}", exception.getMessage(), exception);
        }
    }

    /**
     * The method is meant for registering the fileWatchers for Mtls Certs.
     */
    @Override
    public void registerFileWatcher() {
        final List<String> caCertDirectories = securityConfiguration.getCaCertDirectories();
        for (String caCertDirectory : caCertDirectories) {
            final FileAlterationObserver fileAlterationObserver = new FileAlterationObserver(caCertDirectory);
            fileAlterationObserver.addListener(getListener());
            MtlsFileChangeMonitor.registerFileWatcher(fileAlterationObserver);
        }
        final FileAlterationObserver tlsCertObserver = new FileAlterationObserver(securityConfiguration.getTlsCertDirectory());
        tlsCertObserver.addListener(getListener());
        MtlsFileChangeMonitor.registerFileWatcher(tlsCertObserver);
    }

    private FileAlterationListener getListener() {
        final FileAlterationListener listener = new FileAlterationListenerAdaptor() {
            @Override
            public void onDirectoryChange(File file) {
                if (file.listFiles().length != 0) {
                    reloadCertsOnFileChange();
                }
            }
        };
        return listener;
    }

    private synchronized void reloadCertsOnFileChange() {
        try {
            log.info("OnChange Detected for certificate Files");
            keyStoreUtil.createTrustStoreFromCACerts(securityConfiguration.getCaCertFiles(),
                    securityConfiguration.getStorePass(), securityConfiguration.getTrustStorePath());
            keyStoreUtil.createKeyStoreFromCertAndPK(securityConfiguration.getKeyStoreTlsCertFile(),
                    securityConfiguration.getKeyStoreTlsKeyFile(), securityConfiguration.getKeyAlias(),
                    securityConfiguration.getStorePass(), securityConfiguration.getKeystorepath());

            MtlsConfigurationReloadersRegister.getInstance().getMtlsConfigurationReloaders().stream()
                    .forEach(mtlsReloadConfiguration -> {
                        try {
                            mtlsReloadConfiguration.reload();
                            SecurityLogger.withFacility(
                                    () -> log.info("Onchange done for: {}", mtlsReloadConfiguration.getClass().getName()));
                        } catch (Exception exception) {
                            SecurityLogger.withFacility(
                                    () -> log.error("Reload of MTLS Configuration is failed in onChange() due to: {}", exception.getMessage()));
                        }
                    });
        } catch (Exception exception) {
            log.error("Operation is failed on certificate files: {}", exception.getMessage());
        }
    }
}
