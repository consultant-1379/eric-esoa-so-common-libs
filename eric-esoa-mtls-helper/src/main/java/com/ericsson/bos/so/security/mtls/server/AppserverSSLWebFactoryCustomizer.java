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

import com.ericsson.bos.so.security.mtls.config.SecurityConfiguration;
import com.ericsson.bos.so.security.mtls.util.KeyStoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.SslStoreProvider;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

import java.security.KeyStore;

/**
 * This class is not needed to be exclusively called by the client.
 * This class will be called by the library internally and keep things ready for
 * its clients
 */
@Component
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
public class AppserverSSLWebFactoryCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AppserverSSLWebFactoryCustomizer.class);

    @Autowired
    private KeyStoreUtil keyStoreUtil;

    @Autowired
    private SecurityConfiguration securityConfiguration;

    /**
     * method of WebServerFactoryCustomizer interface used to initialize the required SSL configuration at
     * application startup time
     *
     * @Param ConfigurableServletWebServerFactory
     */
    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        LOGGER.info("Service is configured for secure communication via mTLS.");
        LOGGER.info("Updating SSL Configuration");

        final Ssl ssl = new Ssl();
        ssl.setEnabled(true);
        ssl.setKeyAlias(securityConfiguration.getKeyAlias());
        ssl.setKeyPassword(securityConfiguration.getKeyPass());
        ssl.setKeyStorePassword(securityConfiguration.getStorePass());
        ssl.setKeyStoreType(securityConfiguration.getKeyStoreType());
        ssl.setClientAuth(Ssl.ClientAuth.NEED);

        factory.setSsl(ssl);
        factory.setPort(securityConfiguration.getHttpsPort());
        factory.setSslStoreProvider(new SslStoreProvider() {
            @Override
            public KeyStore getKeyStore() throws Exception {
                return keyStoreUtil.createKeyStoreFromCertAndPK(securityConfiguration.getKeyStoreTlsCertFile(),
                        securityConfiguration.getKeyStoreTlsKeyFile(), securityConfiguration.getKeyAlias(),
                        securityConfiguration.getStorePass(), securityConfiguration.getKeystorepath());
            }

            @Override
            public KeyStore getTrustStore() throws Exception {
                return keyStoreUtil.createTrustStoreFromCACerts(securityConfiguration.getCaCertFiles(),
                        securityConfiguration.getStorePass(), securityConfiguration.getTrustStorePath());
            }
        });
    }
}

