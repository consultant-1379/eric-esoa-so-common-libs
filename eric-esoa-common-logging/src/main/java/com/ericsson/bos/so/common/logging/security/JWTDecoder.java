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

import ch.qos.logback.classic.Level;
import com.ericsson.bos.so.common.logging.utils.LogControlWatcher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWTDecoder
 */
public final class JWTDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTDecoder.class);

    private static final Pattern pattern = Pattern.compile("\\.");

    private JWTDecoder() {
        throw new AssertionError("The utility class must not be instantiated");
    }

    private static String decodeToJson(final String hashToken, final boolean isSkipNullToken) {
        if (hashToken == null) {
            LOGGER.trace("Token is null");
            return null;
        }
        final String encodedTokenBody = getPayload(hashToken);
        if (encodedTokenBody == null) {
            LOGGER.warn("Token is invalid: {}", hashToken);
            return null;
        }
        try {
            final byte[] decodedToken = Base64.getDecoder().decode(encodedTokenBody);
            return new String(decodedToken, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            final Level currentLogLevel = LogControlWatcher.getCurrentSeverityLevel();
            if (currentLogLevel.levelInt != Level.INFO.levelInt &&
                currentLogLevel.levelInt != Level.OFF.levelInt) {
                LOGGER.warn("Unable to decode token: {}, {}", encodedTokenBody, e.getMessage(), e);
            } else {
                LOGGER.warn("Unable to decode token: {}, {}", encodedTokenBody, e.toString());
            }
            return null;
        }
    }

    /**
     * getUsernameFromJWTToken
     *
     * @param token -
     * @param objectMapper -
     * @param payloadUsernameKey -
     * @param isSkipNullToken -
     * @return String
     */
    public static String getUsernameFromJWTToken(final String token, final ObjectMapper objectMapper, final String payloadUsernameKey,
                                                 final boolean isSkipNullToken) {
        try {
            LOGGER.trace("Token claims: {}", getClaims(token, objectMapper, isSkipNullToken));
            final Object userName = getClaims(token, objectMapper, isSkipNullToken).get(payloadUsernameKey);
            return Objects.toString(userName, "");
        } catch (Exception e) {
            LOGGER.warn("Could not get userName from JWT: {}", e.getMessage());
        }
        return "";
    }

    /**
     * getClaims
     *
     * @param hashToken -
     * @param objectMapper -
     * @param isSkipNullToken -
     * @return Map<String, Object>
     */
    public static Map<String, Object> getClaims(final String hashToken, final ObjectMapper objectMapper, final boolean isSkipNullToken) {
        try {
            final String jsonToken = decodeToJson(hashToken, isSkipNullToken);
            if (jsonToken == null) {
                return Collections.emptyMap();
            }
            return objectMapper.readValue(jsonToken, new TokenMapReference());
        } catch (IOException e) {
            final Level currentLogLevel = LogControlWatcher.getCurrentSeverityLevel();
            if (currentLogLevel.levelInt != Level.INFO.levelInt &&
                currentLogLevel.levelInt != Level.OFF.levelInt) {
                LOGGER.warn("Could not get claims: {}", e.getMessage(), e);
            } else {
                LOGGER.warn("Could not get claims: {}", e.toString());
            }
        }
        return Collections.emptyMap();
    }

    private static String getPayload(final String hashToken) {
        final String[] hashArray = pattern.split(hashToken);
        if (hashArray.length == 3) {
            return hashArray[1];
        }
        return null;
    }

    private static class TokenMapReference extends TypeReference<Map<String, Object>> {

    }
}
