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

package com.ericsson.bos.so.common.logging.appender;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;

/**
* HTTPS version of the LogstashHttpAppender
*/
public class LogstashHttpsAppender extends LogstashHttpAppenderBase {
    private final String keyStoreType = "PKCS12";
    private String keyStoreLocation;
    private String keyStorePassword;
    private String trustStoreLocation;
    private String trustStorePassword;

    /**
     * LogstashHttpsAppender
     */
    public LogstashHttpsAppender() {
        super("https");
    }

    @Override
    public synchronized void start() {
        if (isStarted()) {
            return;
        }
        try {
            final SSLContext sslContext = buildSSLContext();
            final SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(sslContext)
                    .build();
            /* https://hc.apache.org/httpcomponents-client-4.5.x/current/tutorial/html/connmgmt.html */
            connManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setMaxConnTotal(1)
                    .setMaxConnPerRoute(1)
                    .setSSLSocketFactory(sslSocketFactory).build();
            addInfo("start");
            super.start();
            needReload = false;
        } catch (final Exception e) {
            addError("LogstashHttpsAppender not started: ", e);
        }
    }

    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public void setKeyStoreLocation(final String keyStoreLocation) {
        this.keyStoreLocation = keyStoreLocation;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(final String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getTrustStoreLocation() {
        return trustStoreLocation;
    }

    public void setTrustStoreLocation(final String trustStoreLocation) {
        this.trustStoreLocation = trustStoreLocation;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(final String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    private SSLContext buildSSLContext() throws KeyStoreException, IOException, CertificateException,
            NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        final SSLContextBuilder builder = SSLContexts.custom();
        final File keyStoreFile = new File(keyStoreLocation);
        if (keyStoreFile.exists()) {
            final KeyStore ks = KeyStore.getInstance(keyStoreType);
            ks.load(new FileInputStream(keyStoreFile), keyStorePassword.toCharArray());
            builder.loadKeyMaterial(ks, keyStorePassword.toCharArray());
        }
        final File trustStoreFile = new File(trustStoreLocation);
        if (trustStoreFile.exists()) {

            builder.loadTrustMaterial(trustStoreFile, trustStorePassword.toCharArray());
        }
        return builder.build();
    }
}
