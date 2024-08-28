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
package com.ericsson.bos.so.security.mtls.client.rest;

import com.ericsson.bos.so.security.mtls.MtlsConfigurationReloader;
import com.ericsson.bos.so.security.mtls.config.RestTemplateReloadConfig;
import com.ericsson.bos.so.security.mtls.util.SslContextUtil;
import jakarta.annotation.PostConstruct;
import org.apache.hc.client5.http.classic.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * This class reloads the Rest Template configurations when ever there is a change in
 * the certificate. Clients should not call this class.
 * This class is deprecated
 */
@Component
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
@Deprecated
public class RestTemplateReloadClient implements MtlsConfigurationReloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplateReloadClient.class);

    @Autowired
    private SslContextUtil sslContextUtil;

    @Autowired
    @Qualifier(RestTemplateReloadConfig.SIP_TLS_QUALIFIER)
    private RestTemplate restTemplate;

    /**
     * Method to update the SSL Context to rest client at application startup time
     *
     * @throws KeyManagementException   exception thrown due to corrupt keys
     * @throws NoSuchAlgorithmException exception thrown  due to incorrect algorithm initialization
     * @throws IOException              exception thrown  due to IO Errors
     * @throws KeyStoreException        exception thrown  due to keyStore
     */
    @PostConstruct
    private void init() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
        updateSSLContext();
    }

    /**
     * Method the reload rest client instance with updated SSL context when certificates are renewed
     *
     * @throws KeyManagementException   exception thrown due to corrupt keys
     * @throws NoSuchAlgorithmException exception thrown  due to incorrect algorithm initialization
     * @throws IOException              exception thrown  due to IO Errors
     * @throws KeyStoreException        exception thrown  due to keyStore
     */
    @Override
    public void reload() throws KeyManagementException, NoSuchAlgorithmException, IOException, KeyStoreException {
        updateSSLContext();
    }

    private void updateSSLContext() throws KeyManagementException, NoSuchAlgorithmException, IOException,
        KeyStoreException {
        LOGGER.info("Updating the SSL Context");
        final HttpClient httpClient = sslContextUtil.getHttpClient();
        final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate.setRequestFactory(requestFactory);
        LOGGER.info("Successfully updated the SSLContext");
    }

}
