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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PostgresClientKeyApplicationListenerTest.
 */
@SpringBootTest(classes = PostgresClientKeyApplicationListenerTest.SslClientConverterConfig.class, properties = {
    "security.tls.enabled=true", "spring.datasource.driver-class-name=org.postgresql.Driver",
    "spring.datasource.url=jdbc:postgresql:localhost:5432/db?currentSchema=public&sslkey=${java.io.tmpdir}/client.key",
    "spring.flyway.url=jdbc:postgresql:localhost:5432/db?currentSchema=public&sslkey=${java.io.tmpdir}/client.key" })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostgresClientKeyApplicationListenerTest {

    private static final String PRIVATE_KEY_PEM = "-----BEGIN PRIVATE KEY-----\n"
            + "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgXYZkiSnTFH5bO0w3"
            + "dcj290f+rMgCrUk9NgpJfkyowhehRANCAASkX9t8Wwxyygyp9JgsHTWUKOWQ4VN/"
            + "+zhJWPQjCYyv7YTQewc21Kis2qh8xa5y6q+vnR+mat8K4LQsEMX7gkK4\n"
            + "-----END PRIVATE KEY-----";

    private static final String PRIVATE_KEY2_PEM = "-----BEGIN PRIVATE KEY-----\n"
            + "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgA7GBv6FVXSPCmC8/"
            + "1kTiRhZRGu8sjlAqDBMl12ZbYCOhRANCAATBHM9GMmQ1X3CqCR9JOXNgq4MHK/EZ"
            + "w0K4zvtdU5mQ11DOYusmXgEppD8D45+4og3kStoyAITpbb0ZoJ2/Moip\n"
            + "-----END PRIVATE KEY-----";

    private static final String PEM_KEY_FILE = Paths.get(System.getProperty("java.io.tmpdir"), "client.key").toString();

    private static final String PKSC8_KEY_FILE = Paths.get(System.getProperty("java.io.tmpdir"), "pg-client-key", "client.key").toString();

    @Autowired
    private JdbcUrlTester jdbcUrlTester;

    /**
     * Perform test setup.
     * @throws IOException if error deleting or writing the client key files.
     */
    @BeforeAll
    static void setup() throws IOException {
        Files.deleteIfExists(Path.of(PEM_KEY_FILE));
        Files.deleteIfExists(Path.of(PKSC8_KEY_FILE));
        Files.write(Path.of(PEM_KEY_FILE), PRIVATE_KEY_PEM.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Test ssl client key is successfully converted from PEM to PKCS8 format.
     * @throws IOException if error reading the pkcs8 file
     */
    @Test
    @Order(1)
    void testClientKeyIsConvertedFromPemToPkcs8 () throws IOException {
        final String pkcs8KeyFile = Paths.get(System.getProperty("java.io.tmpdir"), "pg-client-key", "client.key").toString();
        assertTrue(new File(pkcs8KeyFile).exists());
        final byte[] pkcs8KeyFileContents = Files.readAllBytes(Path.of(PKSC8_KEY_FILE));
        assertTrue(PRIVATE_KEY_PEM.contains(Base64.getEncoder().encodeToString(pkcs8KeyFileContents)));
    }

    /**
     * Test spring datasource url properties are updated after with location
     * of the converted client key file.
     */
    @Test
    @Order(2)
    void testDataSourceUrlPropertiesAreUpdated () {
        final String expectedJdbcUrl = "jdbc:postgresql:localhost:5432/db?currentSchema=public&sslkey=" + PKSC8_KEY_FILE;
        assertEquals(expectedJdbcUrl, jdbcUrlTester.getSpringDataSourceUrl());
        assertEquals(expectedJdbcUrl, jdbcUrlTester.getSpringFlywayUrl());
    }

    /**
     * Test the converted client key file is updted when the original pem file changes.
     * @throws IOException if error reading the converted file
     */
    @Test
    @Order(3)
    void testPkcs8ClientKeyIsUpdatedWhenPemKeyFileChanges () throws IOException {
        Files.write(Path.of(PEM_KEY_FILE), PRIVATE_KEY2_PEM.getBytes(StandardCharsets.UTF_8));
        Awaitility.await().pollInSameThread().pollInterval(Durations.FIVE_HUNDRED_MILLISECONDS).timeout(Durations.FIVE_SECONDS).until(() -> {
            final byte[] pkcs8KeyFileContents = Files.readAllBytes(Path.of(PKSC8_KEY_FILE));
            return PRIVATE_KEY2_PEM.contains(Base64.getEncoder().encodeToString(pkcs8KeyFileContents));
        });
    }

    /**
     * SslClientConverterConfig.
     */
    @Configuration
    static class SslClientConverterConfig {

        /**
         * Bean to test the spring jdbc url values.
         * @return
         */
        @Bean
        JdbcUrlTester jdbcUrlTester() {
            return new JdbcUrlTester();
        }
    }

    /**
     * Class to get he configured spring jdbc url values.
     */
    static class JdbcUrlTester {

        @Value("${spring.datasource.url}")
        private String springDataSourceUrl;

        @Value("${spring.flyway.url}")
        private String springFlywayUrl;

        /**
         * get the spring datasource url.
         * @return url.
         */
        public String getSpringDataSourceUrl() {
            return springDataSourceUrl;
        }

        /**
         * Get the flyway datasource url.
         * @return url.
         */
        public String getSpringFlywayUrl() {
            return springFlywayUrl;
        }
    }
}