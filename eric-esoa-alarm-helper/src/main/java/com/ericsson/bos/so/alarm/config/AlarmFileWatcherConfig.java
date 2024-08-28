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
package com.ericsson.bos.so.alarm.config;

import com.ericsson.bos.so.alarm.client.AlarmTlsWebClientReloadClient;
import com.ericsson.bos.so.security.mtls.MtlsConfigurationReloadersRegister;
import com.ericsson.bos.so.security.mtls.config.FileWatchersRegister;
import com.ericsson.bos.so.security.mtls.config.MtlsFileChangeMonitor;
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

/**
 * This class is not needed to be exclusively called by the client.
 * This class will be called by the library internally and keep things ready
 * for its clients
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
public class AlarmFileWatcherConfig implements FileWatchersRegister {

    @Autowired
    private KeyStoreUtil alarmKeyStoreUtil;

    @Autowired
    private AlarmConfiguration alarmConfiguration;

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
     * The method is meant for registering the fileWatchers for Alarm Certs.
     */
    @Override
    public void registerFileWatcher() {
        final FileAlterationObserver alarmCertObserver = new FileAlterationObserver(alarmConfiguration.getTlsCertDirectory());
        alarmCertObserver.addListener(getListener());
        MtlsFileChangeMonitor.registerFileWatcher(alarmCertObserver);
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

    private void reloadCertsOnFileChange() {
        try {
            log.info("OnChange Detected for Alarm certificate Files");
            alarmKeyStoreUtil.createKeyStoreFromCertAndPK(alarmConfiguration.getKeyStoreTlsCertFile(),
                    alarmConfiguration.getKeyStoreTlsKeyFile(), alarmConfiguration.getKeyAlias(),
                    alarmConfiguration.getStorePass(), alarmConfiguration.getKeystorepath());

            MtlsConfigurationReloadersRegister.getInstance().getMtlsConfigurationReloaders().stream()
                    .forEach(mtlsReloadConfiguration -> {
                        try {
                            if (AlarmTlsWebClientReloadClient.class.getName().equals(mtlsReloadConfiguration.getClass().getName())) {
                                mtlsReloadConfiguration.reload();
                                log.info("Onchange done for: {}", mtlsReloadConfiguration.getClass().getName());
                            }
                        } catch (Exception exception) {
                            log.error("reloadMtlsConfiguration is failed in onChange() due to: {}", exception.getMessage());
                        }
                    });
        } catch (Exception exception) {
            log.error("Operation is failed on Alarm certificate files: {}", exception.getMessage());
        }
    }
}
