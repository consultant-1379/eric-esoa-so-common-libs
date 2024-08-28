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
package com.ericsson.bos.so.common.logging.security.extractor;

import com.ericsson.bos.so.common.logging.config.SkipNullTokenLoggingConfig;
import com.ericsson.bos.so.common.logging.security.JWTDecoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import static com.ericsson.bos.so.common.logging.config.CommonLoggingConstants.AUTHORIZATION_HEADER_NAME;
import static com.ericsson.bos.so.common.logging.config.CommonLoggingConstants.BEARER_SCHEME;

/**
 * JwtUserNameExtractor
 */
/**
 * JwtUserNameExtractor
 */
public class JwtUserNameExtractor implements UserNameExtractor {

    @Autowired
    private SkipNullTokenLoggingConfig skipNullTokenLoggingConfig;

    private final ObjectMapper objectMapper;
    private final String payloadUsernameKey;

    /**
     * JwtUserNameExtractor constructor.
     *
     * @param objectMapper -
     * @param environment -
     */
    public JwtUserNameExtractor(final ObjectMapper objectMapper, final Environment environment) {
        this.objectMapper = objectMapper;
        this.payloadUsernameKey = environment.getProperty("PAYLOAD_USERNAME_KEY", "preferred_name");
    }

    @Override
    public String extract(final HttpServletRequest request) {
        boolean isSkipNullToken = false;
        final List<String> skipNullTokenEndpoints = skipNullTokenLoggingConfig.getSkipNullTokenEndpoints();
        if (skipNullTokenEndpoints != null) {
            isSkipNullToken = skipNullTokenEndpoints.contains(request.getRequestURI());
        }

        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER_NAME);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_SCHEME)) {
            // Extract the jwtToken part from the header value
            final String jwtToken = authorizationHeader.substring(7);
            return JWTDecoder.getUsernameFromJWTToken(jwtToken, objectMapper,
                    payloadUsernameKey, isSkipNullToken);
        }
        return null;
    }
}