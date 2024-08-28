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

import com.ericsson.bos.so.security.mtls.MtlsConfigurationReloader;
import com.ericsson.bos.so.security.mtls.config.SecurityConfiguration;
import com.ericsson.bos.so.security.mtls.model.WebClientHttpConnectorFactoryCache;
import com.ericsson.bos.so.security.mtls.util.SslContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * This class reloads the Tls WebClient configurations when ever there is a change in
 * the certificate. Clients will use this reloaded WebClient instance when tls enabled.
 * Clients should not call this class.
 */

@Slf4j
@Component
public class TlsWebClientReloadClient implements MtlsConfigurationReloader {

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @Autowired
    private SslContextUtil sslContextUtil;

    @Autowired
    private WebClientHttpConnectorFactoryCache clientInterceptorsFactoryCache;

    /**
     * Method the reload web client builder instance with updated SSL context when certificates are renewed
     *
     * @throws KeyManagementException   exception thrown due to corrupt keys
     * @throws NoSuchAlgorithmException exception thrown  due to incorrect algorithm initialization
     * @throws IOException              exception thrown  due to IO Errors
     * @throws KeyStoreException        exception thrown  due to keyStore
     */
    @Override
    public void reload() throws KeyManagementException, NoSuchAlgorithmException, IOException, KeyStoreException {
        final Map<WebClient.Builder, ClientHttpConnector> requestFactoryMap = clientInterceptorsFactoryCache.getRequestFactoryMap();
        log.info("Reloading sslContext to TlsWebClient. securityTlsEnabled:{}", securityConfiguration.isSecurityTlsEnabled());
        if (securityConfiguration.isSecurityTlsEnabled()) {
            final CloseableHttpAsyncClient httpClient = sslContextUtil.getCloseableHttpAsyncClient();
            log.info("Reloading sslContext to {} TlsRestTemplate.", requestFactoryMap.size());
            for (Map.Entry<WebClient.Builder, ClientHttpConnector> entry : requestFactoryMap.entrySet()) {
                final WebClient.Builder webClientBuilder = entry.getKey();
                final ClientHttpConnector connector = new HttpComponentsClientHttpConnector(httpClient);
                webClientBuilder.clientConnector(connector);
            }
            log.info("sslContext updated to {} TlsWebClient successfully.", requestFactoryMap.size());
        }
    }

}
