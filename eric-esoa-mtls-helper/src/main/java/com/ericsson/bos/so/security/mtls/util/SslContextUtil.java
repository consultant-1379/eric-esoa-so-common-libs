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
package com.ericsson.bos.so.security.mtls.util;

import com.ericsson.bos.so.security.mtls.config.ConnectionConfiguration;
import com.ericsson.bos.so.security.mtls.config.SecurityConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.*;

/**
 * This class is used to get HttpClient when required.
 * This class will be called by the clients
 */
@Slf4j
@Component
public class SslContextUtil {

    @Autowired
    private SecurityConfiguration securityConfiguration;

    @Autowired
    private ConnectionConfiguration connectionConfiguration;

    /**
     * This method is deprecated
     * Method to get the SSL Configured HttpClient.
     *
     * @return HttpClient httpClient
     * @throws KeyManagementException   exception thrown due to corrupt keys
     * @throws NoSuchAlgorithmException exception thrown  due to incorrect algorithm initialization
     * @throws IOException              exception thrown  due to IO Errors
     * @throws KeyStoreException        exception thrown  due to keyStore
     */

    @Deprecated
    public CloseableHttpClient getHttpClient() throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        final PoolingHttpClientConnectionManager poolingHttpClientConnectionManager =
                PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                                .setSslContext(getSSLContext())
                                .setTlsVersions(TLS.V_1_3)
                                .build())
                        .setDefaultSocketConfig(SocketConfig.custom()
                                .setSoTimeout(Timeout.ofMilliseconds(connectionConfiguration.getReadTimeoutMsec()))
                                .build()).build();
        return HttpClientBuilder.create().setConnectionManager(poolingHttpClientConnectionManager).build();

    }

    /**
     * Method to get the SSL Configured CloseableHttpAsyncClient.
     *
     * @return CloseableHttpAsyncClient closeableHttpAsyncClient
     * @throws KeyManagementException   exception thrown due to corrupt keys
     * @throws NoSuchAlgorithmException exception thrown  due to incorrect algorithm initialization
     * @throws IOException              exception thrown  due to IO Errors
     * @throws KeyStoreException        exception thrown  due to keyStore
     */
    public CloseableHttpAsyncClient getCloseableHttpAsyncClient() throws IOException, KeyStoreException,
            NoSuchAlgorithmException, KeyManagementException {
        final PoolingAsyncClientConnectionManager poolingAsyncClientConnectionManager =
                PoolingAsyncClientConnectionManagerBuilder.create()
                        .setTlsStrategy(ClientTlsStrategyBuilder.create()
                                .setSslContext(getSSLContext())
                                .build()).build();
        return HttpAsyncClientBuilder.create().setConnectionManager(poolingAsyncClientConnectionManager)
                .disableAutomaticRetries().
                setDefaultRequestConfig(RequestConfig.custom()
                        .setResponseTimeout(Timeout.ofMilliseconds(connectionConfiguration.getReadTimeoutMsec()))
                        .build()).build();
    }

    /**
     * Method to get the SSL Configured CloseableHttpAsyncClient with custom timeout
     * @param timeoutInMilliSec timeoutInMilliSec
     *
     * @return CloseableHttpAsyncClient closeableHttpAsyncClient
     * @throws KeyManagementException   exception thrown due to corrupt keys
     * @throws NoSuchAlgorithmException exception thrown  due to incorrect algorithm initialization
     * @throws IOException              exception thrown  due to IO Errors
     * @throws KeyStoreException        exception thrown  due to keyStore
     */
    public CloseableHttpAsyncClient getCloseableHttpAsyncClient(long timeoutInMilliSec) throws IOException, KeyStoreException,
            NoSuchAlgorithmException, KeyManagementException {
        final PoolingAsyncClientConnectionManager poolingAsyncClientConnectionManager =
                PoolingAsyncClientConnectionManagerBuilder.create()
                        .setTlsStrategy(ClientTlsStrategyBuilder.create()
                                .setSslContext(getSSLContext())
                                .build()).build();
        return HttpAsyncClientBuilder.create().setConnectionManager(poolingAsyncClientConnectionManager)
                .disableAutomaticRetries().
                setDefaultRequestConfig(RequestConfig.custom()
                        .setResponseTimeout(Timeout.ofMilliseconds(timeoutInMilliSec))
                        .build()).build();
    }

    /**
     * Gets the SSL context.
     *
     * @return the SSL context
     * @throws IOException              Signals that an I/O exception has occurred.
     * @throws KeyStoreException        the key store exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws KeyManagementException   the key management exception
     */
    public SSLContext getSSLContext() throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslContext = SSLContextBuilder.create().build();
        sslContext.init(getKeyManagers(securityConfiguration.getKeystorepath()), getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    /**
     * Gets the SSL context.
     *
     * @param keyStorePath the Key Store Path
     * @return the SSL context
     * @throws IOException              Signals that an I/O exception has occurred.
     * @throws KeyStoreException        the key store exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws KeyManagementException   the key management exception
     */
    public SSLContext getSSLContext(String keyStorePath) throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslContext = SSLContextBuilder.create().build();
        sslContext.init(getKeyManagers(keyStorePath), getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    private KeyManager[] getKeyManagers(String keyStorePath) throws IOException, KeyStoreException, NoSuchAlgorithmException {
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        final InputStream inputStream = java.nio.file.Files.newInputStream(Paths.get(keyStorePath));
        try {
            if (keyStore != null) {
                keyStore.load(inputStream, securityConfiguration.getStorePass().toCharArray());
                keyManagerFactory.init(keyStore, securityConfiguration.getStorePass().toCharArray());
            }
        } catch (Exception exception) {
            log.debug("Failed to load keystore file:: {} ", exception.getMessage());
        } finally {
            inputStream.close();
        }
        return keyManagerFactory.getKeyManagers();
    }

    private TrustManager[] getTrustManagers() throws IOException {
        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            log.error("Failed to get truststore instance:: {} ", e.getMessage());
        }
        final InputStream inputStream = java.nio.file.Files.newInputStream(Paths.get(securityConfiguration.getTrustStorePath()));
        try {
            if (trustStore != null) {
                trustStore.load(inputStream, securityConfiguration.getStorePass().toCharArray());
            }
        } catch (Exception e) {
            log.error("Failed to load truststore file:: {} ", e.getMessage());
        } finally {
            inputStream.close();
        }

        TrustManager[] trustManagers = null;
        try {
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            trustManagers = tmf.getTrustManagers();

        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to get TrustManagerFactory instance:: {} ", e.getMessage());
        } catch (KeyStoreException e) {
            log.error("Failed to initialize the truststore:: {} ", e.getMessage());
        }
        return trustManagers;
    }
}
