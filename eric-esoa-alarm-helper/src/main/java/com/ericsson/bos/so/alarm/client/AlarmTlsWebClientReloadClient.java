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
package com.ericsson.bos.so.alarm.client;

import com.ericsson.bos.so.alarm.config.AlarmConfiguration;
import com.ericsson.bos.so.alarm.config.AlarmWebClientReloadConfig;
import com.ericsson.bos.so.alarm.util.AlarmSslContextUtil;
import com.ericsson.bos.so.security.mtls.MtlsConfigurationReloader;
import com.ericsson.bos.so.security.mtls.util.KeyStoreUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * This class reloads the Tls WebClient configurations when ever there is a change in
 * the certificate. Clients will use this reloaded WebClient instance when tls enabled.
 * Clients should not call this class.
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
public class AlarmTlsWebClientReloadClient implements MtlsConfigurationReloader {

    @Autowired
    private AlarmSslContextUtil alarmSslContextUtil;

    @Autowired
    private AlarmConfiguration alarmConfiguration;

    @Autowired
    private KeyStoreUtil alarmKeyStoreUtil;

    @Autowired
    @Qualifier(AlarmWebClientReloadConfig.ALARM_WEB_CLIENT_QUALIFIER)
    private WebClient.Builder webclientBuilder;

    /**
     * Method to update the SSL Context to rest client at application startup time
     *
     * @throws KeyManagementException   exception thrown due to corrupt keys
     * @throws NoSuchAlgorithmException exception thrown  due to incorrect algorithm initialization
     * @throws IOException              exception thrown  due to IO Errors
     * @throws KeyStoreException        exception thrown  due to keyStore
     */
    @PostConstruct
    private void init() throws Exception {
        alarmKeyStoreUtil.createKeyStoreFromCertAndPK(alarmConfiguration.getKeyStoreTlsCertFile(),
                alarmConfiguration.getKeyStoreTlsKeyFile(), alarmConfiguration.getKeyAlias(),
                alarmConfiguration.getStorePass(), alarmConfiguration.getKeystorepath());
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
        log.info("Updating the SSL Context");
        final CloseableHttpAsyncClient httpClient = alarmSslContextUtil.getCloseableHttpAsyncClient();
        final ClientHttpConnector connector = new HttpComponentsClientHttpConnector(httpClient);
        webclientBuilder.clientConnector(connector).build();
        log.info("Successfully updated the SSLContext");
    }
}
