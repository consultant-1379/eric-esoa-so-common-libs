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
import com.ericsson.bos.so.security.mtls.config.SecurityConfiguration;
import com.ericsson.bos.so.security.mtls.model.ClientHttpRequestFactoryCache;
import com.ericsson.bos.so.security.mtls.util.SslContextUtil;
import org.apache.hc.client5.http.classic.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * This class reloads the Tls Rest Template configurations when ever there is a change in
 * the certificate. Clients will use this reloaded RestTemplate instance when tls enabled.
 * Clients should not call this class.
 * This class is deprecated
 */
@Component
@Deprecated
public class TlsRestTemplateReloadClient implements MtlsConfigurationReloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(TlsRestTemplateReloadClient.class);

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @Autowired
    private SslContextUtil sslContextUtil;

    @Autowired
    private ClientHttpRequestFactoryCache clientHttpRequestFactoryCache;

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
        final Map<RestTemplate, ClientHttpRequestFactory> requestFactoryMap = clientHttpRequestFactoryCache.getRequestFactoryMap();
        LOGGER.info("Reloading sslContext to TlsRestTemplate. securityTlsEnabled:{}", securityConfiguration.isSecurityTlsEnabled());
        if (securityConfiguration.isSecurityTlsEnabled()) {
            final HttpClient httpClient = sslContextUtil.getHttpClient();
            LOGGER.info("Reloading sslContext to {} TlsRestTemplate.", requestFactoryMap.size());
            for (Map.Entry<RestTemplate, ClientHttpRequestFactory> entry : requestFactoryMap.entrySet()) {
                final RestTemplate restTemplate = entry.getKey();
                final ClientHttpRequestFactory clientHttpRequestFactory = entry.getValue();
                if (clientHttpRequestFactory instanceof HttpComponentsClientHttpRequestFactory) {
                    final HttpComponentsClientHttpRequestFactory requestFactory = (HttpComponentsClientHttpRequestFactory) entry.getValue();
                    requestFactory.setHttpClient(httpClient);
                    restTemplate.setRequestFactory(requestFactory);
                } else if (clientHttpRequestFactory instanceof BufferingClientHttpRequestFactory) {
                    restTemplate.setRequestFactory(getBufferingClientHttpRequestFactory(httpClient));
                }
            }
            LOGGER.info("sslContext updated to {} TlsRestTemplate successfully.", requestFactoryMap.size());
        }
    }

    private BufferingClientHttpRequestFactory getBufferingClientHttpRequestFactory(HttpClient httpClient)
            throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        if (securityConfiguration.isSecurityTlsEnabled()) {
            requestFactory.setHttpClient(httpClient);
        }
        return new BufferingClientHttpRequestFactory(requestFactory);
    }

}
