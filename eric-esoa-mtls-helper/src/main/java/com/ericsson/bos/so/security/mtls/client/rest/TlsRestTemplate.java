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

import com.ericsson.bos.so.security.mtls.config.SecurityConfiguration;
import com.ericsson.bos.so.security.mtls.model.ClientHttpRequestFactoryCache;
import com.ericsson.bos.so.security.mtls.model.ClientHttpRequestFactoryType;
import com.ericsson.bos.so.security.mtls.util.SslContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * This class allows the microservices to provide required configurations for creation of RestTemplate.
 * Some of the microservices may require more than one RestTemplate, each with different configuration.
 * This class allows them to create more than one RestTemplate.
 * All these RestTemplates are configured with SSLContext and also the SSLContext is reloaded on demand.
 * This class is deprecated
 */
@Component
@Deprecated
public class TlsRestTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(TlsRestTemplate.class);

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @Autowired
    private SslContextUtil sslContextUtil;

    @Autowired
    private ClientHttpRequestFactoryCache clientHttpRequestFactoryCache;

    /**
     * Method returns SSL configured RestTemplate based on provided ClientHttpRequestFactory Type.
     *
     * @param builder                      RestTemplateBuilder
     * @param clientHttpRequestFactoryType ClientHttpRequestFactoryType
     * @return RestTemplate TlsRestTemplate
     * @throws KeyManagementException   exception thrown due to corrupt keys
     * @throws NoSuchAlgorithmException exception thrown  due to incorrect algorithm initialization
     * @throws IOException              exception thrown  due to IO Errors
     * @throws KeyStoreException        exception thrown  due to keyStore
     * @throws RuntimeException         exception thrown  due to incorrect ClientHttpRequestFactoryType
     */
    public RestTemplate getSslConfiguredRestTemplate(final RestTemplateBuilder builder, ClientHttpRequestFactoryType clientHttpRequestFactoryType)
            throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        if (ClientHttpRequestFactoryType.HTTP_COMPONENTS_CLIENT_HTTP_REQUEST_FACTORY.equals(clientHttpRequestFactoryType)) {
            return getSslConfiguredRestTemplate(builder, new HttpComponentsClientHttpRequestFactory());
        }
        if (ClientHttpRequestFactoryType.BUFFERING_CLIENT_HTTP_REQUEST_FACTORY.equals(clientHttpRequestFactoryType)) {
            return getSslConfiguredRestTemplateBufferedRequestFactory();
        }

        throw new RuntimeException(clientHttpRequestFactoryType.name() + " Type is not supported for SSL configured RestTemplate currently");
    }

    /**
     * Method returns SSL configured RestTemplate. It uses HttpComponentsClientHttpRequestFactory provided by the client.
     * Clients should use this method when they have custom configuration of RequestFactory.
     *
     * @param builder        RestTemplateBuilder
     * @param requestFactory HttpComponentsClientHttpRequestFactory
     * @return RestTemplate TlsRestTemplate
     * @throws KeyManagementException   exception thrown due to corrupt keys
     * @throws NoSuchAlgorithmException exception thrown  due to incorrect algorithm initialization
     * @throws IOException              exception thrown  due to IO Errors
     * @throws KeyStoreException        exception thrown  due to keyStore
     */
    public RestTemplate getSslConfiguredRestTemplate(final RestTemplateBuilder builder, HttpComponentsClientHttpRequestFactory requestFactory)
            throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        if (securityConfiguration.isSecurityTlsEnabled()) {
            requestFactory.setHttpClient(sslContextUtil.getHttpClient());
        }
        final RestTemplate restTemplate = builder.build();
        restTemplate.setRequestFactory(requestFactory);
        clientHttpRequestFactoryCache.getRequestFactoryMap().put(restTemplate, requestFactory);
        return restTemplate;
    }

    private RestTemplate getSslConfiguredRestTemplateBufferedRequestFactory()
            throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        if (securityConfiguration.isSecurityTlsEnabled()) {
            requestFactory.setHttpClient(sslContextUtil.getHttpClient());
        }
        final BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory = new BufferingClientHttpRequestFactory(requestFactory);
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(bufferingClientHttpRequestFactory);
        clientHttpRequestFactoryCache.getRequestFactoryMap().put(restTemplate, bufferingClientHttpRequestFactory);
        return restTemplate;
    }

}
