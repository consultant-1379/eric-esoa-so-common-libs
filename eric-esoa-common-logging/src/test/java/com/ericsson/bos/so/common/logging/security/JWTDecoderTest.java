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
package com.ericsson.bos.so.common.logging.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * You can decode JWT tokens from the test here: https://jwt.io/
 * secret: common-logging
 */
class JWTDecoderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * getClaimsTokenNull
     */
    @Test
    void getClaimsTokenNull() {
        final Map<String, Object> actual = JWTDecoder.getClaims(null, objectMapper, false);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    /**
     * getClaimsTokenEmptyString
     */
    @Test
    void getClaimsTokenEmptyString() {
        final Map<String, Object> actual = JWTDecoder.getClaims("", objectMapper, false);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    /**
     * getClaimsTokenWithoutSignature
     */
    @Test
    void getClaimsTokenWithoutSignature() {
        final Map<String, Object> actual = JWTDecoder.getClaims(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcmVmZXJyZWRfbmFtZSI6InRlc3QifQ", objectMapper, false);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    /**
     * getClaimsTokenExtraClause
     */
    @Test
    void getClaimsTokenExtraClause() {
        final Map<String, Object> actual = JWTDecoder.getClaims(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcmVmZXJyZWRfbmFtZSI6InRlc3QifQ.Spegx-54IWdeLxbGxTQy-awDNaB7MOf7Dt_4i3kUzpU."
                        + "Spegx-54IWdeLxbGxTQy-awDNaB7MOf7Dt_4i3kUzpU",
                objectMapper, false);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    /**
     * getClaimsToken
     */
    @Test
    void getClaimsToken() {
        final Map<String, Object> actual = JWTDecoder.getClaims(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcmVmZXJyZWRfbmFtZSI6InRlc3QifQ.Spegx-54IWdeLxbGxTQy-awDNaB7MOf7Dt_4i3kUzpU",
                objectMapper, false);
        assertNotNull(actual);
        assertEquals("test", actual.get("preferred_name"));
    }

}