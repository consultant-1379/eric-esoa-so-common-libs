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
package com.ericsson.bos.so.alarm.util;

import com.ericsson.bos.so.alarm.config.AlarmConfiguration;
import com.ericsson.bos.so.security.mtls.config.ConnectionConfiguration;
import com.ericsson.bos.so.security.mtls.util.SslContextUtil;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * This class is used to get HttpClient when required.
 * This class will be called by the clients
 */
@Component
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
public class AlarmSslContextUtil {

    @Autowired
    private AlarmConfiguration alarmConfiguration;

    @Autowired
    private ConnectionConfiguration connectionConfiguration;

    @Autowired
    private SslContextUtil sslContextUtil;

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
                                .setSslContext(sslContextUtil.getSSLContext(alarmConfiguration.getKeystorepath()))
                                .build()).build();
        return HttpAsyncClientBuilder.create().setConnectionManager(poolingAsyncClientConnectionManager)
                .disableAutomaticRetries().
                setDefaultRequestConfig(RequestConfig.custom()
                        .setResponseTimeout(Timeout.ofMilliseconds(connectionConfiguration.getReadTimeoutMsec()))
                        .build()).build();
    }

}
