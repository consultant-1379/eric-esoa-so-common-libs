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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * JdbcUrlTest.
 */
public class JdbcUrlTest {

    private static final String JDBC_URL_BASE = "jdbc:postgresql:localhost:5432/db";

    /**
     * Test jdbc url in valid format and with parameters can be read successfully.
     */
    @ParameterizedTest
    @ValueSource(strings = {
        JDBC_URL_BASE + "?sslkey=/tmp/pg.pem",
        JDBC_URL_BASE + "?sslrootcert=/tmp/ca.crt&sslkey=/tmp/pg.pem",
        JDBC_URL_BASE + "?sslrootcert=/tmp/ca.crt&sslkey=/tmp/pg.pem&sslcert=/tmp/pg.crt"
    })
    void testReadJdbcUrlWithParameters(String param) {
        final JdbcUrl jdbcUrl = new JdbcUrl(param);
        assertEquals("/tmp/pg.pem", jdbcUrl.getSslKey().orElse(null));
        assertEquals(param, jdbcUrl.toString());
    }

    /**
     * Test jdbc url in valid format and without parameters can be read successfully.
     */
    @Test
    void testReadJdbcUrlWithNoParameters() {
        final JdbcUrl jdbcUrl = new JdbcUrl(JDBC_URL_BASE);
        assertEquals(JDBC_URL_BASE, jdbcUrl.toString());
    }

    /**
     * Test empty sslKey values is returned when no set in the jdbc url parameters.
     */
    @Test
    void testReadSslKeyReturnsEmptyWhenSslKeyMissing() {
        final JdbcUrl jdbcUrl = new JdbcUrl(JDBC_URL_BASE + "?sslrootcert=/tmp/ca.crt");
        assertTrue(jdbcUrl.getSslKey().isEmpty());
    }

    /**
     * Test update sslkey values is returned in jdbc url string after update.
     */
    @Test
    void testUpdatedJdbcUrlReturnedAfterSettingSslKey() {
        final JdbcUrl jdbcUrl = new JdbcUrl(JDBC_URL_BASE + "?sslrootcert=/tmp/ca.crt&sslkey=/tmp/pg.pem");
        jdbcUrl.setSslKey("/tmp/pg.pk8");
        assertEquals("/tmp/pg.pk8", jdbcUrl.getSslKey().orElse(null));
        assertEquals(JDBC_URL_BASE + "?sslrootcert=/tmp/ca.crt&sslkey=/tmp/pg.pk8", jdbcUrl.toString());
    }
}