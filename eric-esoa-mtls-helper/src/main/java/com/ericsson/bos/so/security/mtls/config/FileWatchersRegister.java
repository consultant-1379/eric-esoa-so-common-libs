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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Interface providing method to be implemented by filewatchers which intent to monitor and reload Mtls Configuration for a given service/client
 */
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
public interface FileWatchersRegister {

    /**
     * This method to be implemented by filewatchers which intent to monitor and reload Mtls Configuration.
     */
    void registerFileWatcher();
}
