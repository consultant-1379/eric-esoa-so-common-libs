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
package com.ericsson.bos.so.security.mtls.client.webclient;

import com.ericsson.bos.so.security.mtls.config.SecurityConfiguration;
import com.ericsson.bos.so.security.mtls.model.WebClientHttpConnectorFactoryCache;
import com.ericsson.bos.so.security.mtls.util.SslContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

/**
 * This class allows the microservices to provide required configurations for creation of WebClient Builder.
 * Some of the microservices may require more than one WebClient, each with different configuration.
 * This class allows them to create more than one WebClient.
 * All these WebClient are configured with SSLContext and also the SSLContext is reloaded on demand.
 */
@Component
public class TlsWebClient {

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @Autowired
    private SslContextUtil sslContextUtil;

    @Autowired
    private WebClientHttpConnectorFactoryCache clientHttpConnectorFactoryCache;


    /**
     * Method returns SSL configured WebClient.Builder.
     * Clients should use this method when they want a default instantiated object for webClient builder.
     *
     * @return WebClient.Builder TlsWebClient
     * @throws KeyManagementException   exception thrown due to corrupt keys
     * @throws NoSuchAlgorithmException exception thrown  due to incorrect algorithm initialization
     * @throws IOException              exception thrown  due to IO Errors
     * @throws KeyStoreException        exception thrown  due to keyStore
     */
    public WebClient.Builder getSslConfiguredWebClient()
            throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        final WebClient.Builder webClientBuilder = WebClient.builder();
        final ClientHttpConnector clientHttpConnector;
        if (securityConfiguration.isSecurityTlsEnabled()) {
            clientHttpConnector = new HttpComponentsClientHttpConnector(sslContextUtil.getCloseableHttpAsyncClient());
            webClientBuilder.clientConnector(clientHttpConnector);
            clientHttpConnectorFactoryCache.getRequestFactoryMap().put(webClientBuilder, clientHttpConnector);
        }
        return webClientBuilder;
    }

    /**
     * Method returns SSL configured WebClient.Builder.
     * Clients should use this method when they want a default instantiated object for webClient builder.
     *
     * @param timeoutInMilliSec timeoutInMilliSec
     * @return WebClient.Builder TlsWebClient
     * @throws KeyManagementException   exception thrown due to corrupt keys
     * @throws NoSuchAlgorithmException exception thrown  due to incorrect algorithm initialization
     * @throws IOException              exception thrown  due to IO Errors
     * @throws KeyStoreException        exception thrown  due to keyStore
     */
    public WebClient.Builder getSslConfiguredWebClient(long timeoutInMilliSec)
            throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        final WebClient.Builder webClientBuilder = WebClient.builder();
        final ClientHttpConnector clientHttpConnector;
        if (securityConfiguration.isSecurityTlsEnabled()) {
            clientHttpConnector = new HttpComponentsClientHttpConnector(sslContextUtil.getCloseableHttpAsyncClient(timeoutInMilliSec));
            webClientBuilder.clientConnector(clientHttpConnector);
            clientHttpConnectorFactoryCache.getRequestFactoryMap().put(webClientBuilder, clientHttpConnector);
        } else {
            final HttpClient client = HttpClient.create()
                    .responseTimeout(Duration.ofMillis(timeoutInMilliSec));
            webClientBuilder.clientConnector(new ReactorClientHttpConnector(client)).build();
        }
        return webClientBuilder;
    }

}
