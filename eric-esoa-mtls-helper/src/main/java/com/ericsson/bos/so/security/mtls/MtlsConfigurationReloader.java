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
package com.ericsson.bos.so.security.mtls;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Interface providing method to be implemented by reloaders which intent to reload Mtls Configuration for a given service/client
*/
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
public interface MtlsConfigurationReloader {

    /**
     * This method to be implemented by reloaders to reload the Mtls configuration.
     * @throws KeyManagementException   exception thrown due to corrupt keys
     * @throws NoSuchAlgorithmException exception thrown  due to incorrect algorithm initialization
     * @throws IOException              exception thrown  due to IO Errors
     * @throws KeyStoreException        exception thrown  due to keyStore
     */
    void reload() throws KeyManagementException, NoSuchAlgorithmException, IOException, KeyStoreException;

}
