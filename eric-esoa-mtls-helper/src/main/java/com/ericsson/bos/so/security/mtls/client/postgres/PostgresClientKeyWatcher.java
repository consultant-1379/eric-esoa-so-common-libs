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
package com.ericsson.bos.so.security.mtls.client.postgres;

import java.io.File;
import java.io.FileFilter;
import java.util.function.Consumer;

import com.ericsson.bos.so.security.mtls.config.MtlsFileChangeMonitor;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitor the postgres client key file for changes, in order to re-execute
 * the conversion from PEM to PKCS8 format on the updated key file.
 */
class PostgresClientKeyWatcher implements Consumer<File> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresClientKeyWatcher.class);

    /**
     * Register file watcher to monitor changes to the postgres client key file.
     * When changed, the updated file will be converted from PEM to PKCS8 format.
     */
    @Override
    public void accept(File keyFile) {
        final FileFilter fileFilter = file -> file.getName().equalsIgnoreCase(keyFile.getName());
        final FileAlterationObserver observer = new FileAlterationObserver(keyFile.getParentFile(), fileFilter);
        observer.addListener(new ClientKeyListener());
        LOGGER.info("Register postgres client key file watcher: {}", keyFile);
        MtlsFileChangeMonitor.registerFileWatcher(observer);
    }

    private static final class ClientKeyListener extends FileAlterationListenerAdaptor {

        @Override
        public void onFileChange(File file) {
            LOGGER.info("Postgres client key file change detected: {}", file.getPath());
            try {
                PostgresClientKeyConverter.convertClientKeyFromPemToPkcs8(file.getPath());
            } catch (final Exception e) {
                LOGGER.error("Error converting postgresql client key to pkcs8", e);
            }
        }
    }
}