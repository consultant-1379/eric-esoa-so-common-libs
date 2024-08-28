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
package com.ericsson.bos.so.security.mtls.client.postgres;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * Spring Application Event Listener to convert the mounted postgresql client key
 * from PEM to PKCS format prior to the ApplicationContext being initialized.
 * The client key will then be available in PKCS8 format at the time the datasource is configured.
 * If the client key is converted then the datasource url properties are overriden with the value set
 * to the location of the converted client cert.
 * A watcher is also started to monitor changes to the original key file.
 * Any changes will result in the conversion being re-executed.
 * <p>
 * This listener is triggered via spring factory, META-INF/spring.factories.
 * </p>
 */
public class PostgresClientKeyApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final String TLS_ENABLED_PROP = "security.tls.enabled";
    private static final String[] JDBC_URL_PROPS = {"spring.datasource.url", "spring.flyway.url"};

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresClientKeyApplicationListener.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        final boolean tlsEnabled = event.getEnvironment().getProperty(TLS_ENABLED_PROP, Boolean.class);
        if (tlsEnabled) {
            final String dataSourceUrl = event.getEnvironment().getProperty(JDBC_URL_PROPS[0]);
            if (dataSourceUrl != null && !dataSourceUrl.isEmpty()) {
                final JdbcUrl jdbcUrl = new JdbcUrl(dataSourceUrl);
                final Optional<String> sslKeyFilePath = jdbcUrl.getSslKey();
                if (sslKeyFilePath.isPresent()) {
                    convertClientKey(jdbcUrl, sslKeyFilePath.get(), event.getEnvironment());
                }
            }
        }
    }

    private void convertClientKey(final JdbcUrl jdbcUrl, final String sslKeyFilePath, final ConfigurableEnvironment environment) {
        try {
            final String pkcs8KeyFilePath = PostgresClientKeyConverter.convertClientKeyFromPemToPkcs8(sslKeyFilePath);
            jdbcUrl.setSslKey(pkcs8KeyFilePath);
             // Create new propertySource to override original datasource url properties
            final Properties props = new Properties();
            Arrays.stream(JDBC_URL_PROPS).forEach(prop -> props.put(prop, jdbcUrl.toString()));
            environment.getPropertySources().addFirst(new PropertiesPropertySource("mtlsDbProperties", props));
            final PostgresClientKeyWatcher watcher = new PostgresClientKeyWatcher();
            watcher.accept(new File(sslKeyFilePath));
        } catch (final Exception e) {
            LOGGER.error("Error converting postgresql client key to pkcs8", e);
        }
    }
}