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

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
public class MtlsFileChangeMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MtlsFileChangeMonitor.class);
    private static final long FIVE_SECONDS_IN_MILLI_SECONDS = 5 * 1000;
    private static final FileAlterationMonitor monitor = new FileAlterationMonitor(FIVE_SECONDS_IN_MILLI_SECONDS);
    private static final List<FileAlterationObserver> observers = new ArrayList<>();

    private MtlsFileChangeMonitor() {
    }

    /**
     * This method is used to register the FileWatcher
     *
     * @param observer the FileAlterationObserver
     */
    public static void registerFileWatcher(FileAlterationObserver observer) {
        observers.add(observer);
        startMonitoring();
        LOGGER.info("Successfully registered {} FileObservers", observers.size());
    }

    /**
     * The method is called when all the fileWatchers are configured. Once all the Observers are
     * updated, this method starts the fileAlterationMonitor which monitors the changes to the certs periodically.
     */
    private static void startMonitoring() {
        observers.forEach(monitor::addObserver);
        try {
            if (observers.size() > 1) {
                monitor.stop(FIVE_SECONDS_IN_MILLI_SECONDS);
                monitor.start();
            } else {
                monitor.start();
            }
        } catch (Exception exception) {
            LOGGER.error("FileAlterationMonitor failed to start due to: {}", exception.getMessage(), exception);
        }
    }
}
