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
package com.ericsson.bos.so.security.mtls.server;

import com.ericsson.bos.so.security.mtls.MtlsConfigurationReloader;
import com.ericsson.bos.so.security.mtls.config.SecurityConfiguration;

import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * This class is not needed to be exclusively called by the client.
 * This class will be called by the library internally and keep things ready for its clients
 */
@Component
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
public class TomcatSSLReloadConfig implements MtlsConfigurationReloader {

    public static final String DEFAULT_SSL_HOSTNAME_CONFIG_NAME = "_default_";

    private static final Logger LOGGER = LoggerFactory.getLogger(TomcatSSLReloadConfig.class);

    @Autowired
    private ServletWebServerFactory servletWebServerFactory;

    @Autowired
    private SecurityConfiguration securityConfiguration;

    /**
     * Reloads the tomcat instance with the latest truststore and keystore instances
     */
    @Override
    public void reload() {
        LOGGER.info("Trying to reload SSLHostConfig");
        final TomcatServletWebServerFactory tomcatFactory = (TomcatServletWebServerFactory) servletWebServerFactory;

        final Collection<TomcatConnectorCustomizer> customizers = tomcatFactory.getTomcatConnectorCustomizers();
        for (TomcatConnectorCustomizer tomcatConnectorCustomizer : customizers) {
            if (tomcatConnectorCustomizer instanceof TomcatSSLConnectorCustomizer) {
                final TomcatSSLConnectorCustomizer customizer = (TomcatSSLConnectorCustomizer) tomcatConnectorCustomizer;
                final Http11NioProtocol protocol = customizer.getProtocol();
                try {
                    final SSLHostConfig[] sslHostConfigs = protocol.findSslHostConfigs();
                    for (SSLHostConfig sslHostConfig : sslHostConfigs) {
                        setValuesForSSLHostConfig(sslHostConfig);
                    }
                    LOGGER.info("Successful reset the SSLHostConfigCertificate");
                    protocol.reloadSslHostConfig(DEFAULT_SSL_HOSTNAME_CONFIG_NAME);
                    LOGGER.info("Successfully reloaded the SSLHostConfig");
                    break;
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Cannot reset the SSLHostConfigCertificate. Server has to be restarted for certificate changes :: {} ",
                            e.getMessage());
                }
            }
        }
    }


    /**
     * Method that overrides the given SSLHostConfigCertificate with renewed key and truststore
     *
     * @param sslHostConfig ssl host configuration parameter
     */
    public void setValuesForSSLHostConfig(SSLHostConfig sslHostConfig) {

        final Set<SSLHostConfigCertificate> certificates = sslHostConfig.getCertificates();
        final Optional<SSLHostConfigCertificate> cert = certificates.stream().findFirst();
        try (InputStream keyStoreStream = new FileInputStream(securityConfiguration.getKeystorepath())) {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreStream, securityConfiguration.getStorePass().toCharArray());
            if (cert.isPresent()) {
                cert.get().setCertificateKeystore(keyStore);
                cert.get().setCertificateKeystorePassword(securityConfiguration.getStorePass());
                cert.get().setCertificateKeyPassword(securityConfiguration.getStorePass());
                cert.get().setCertificateKeyAlias(securityConfiguration.getKeyAlias());
                LOGGER.info("SSLHostConfig certificate alias after reset is {}", cert.get().getCertificateKeyAlias());
            }
            try (InputStream trustStoreStream = new FileInputStream(new File(securityConfiguration.getTrustStorePath()));) {
                final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(trustStoreStream, securityConfiguration.getStorePass().toCharArray());
                sslHostConfig.setTrustStore(trustStore);
            }

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOGGER.error("Couldn't update SSLHost Config ::{}", e.getMessage());
        }
    }

}
