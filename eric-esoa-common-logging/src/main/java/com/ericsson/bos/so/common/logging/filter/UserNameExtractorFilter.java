/*******************************************************************************
 * COPYRIGHT Ericsson 2024
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
package com.ericsson.bos.so.common.logging.filter;

import static com.ericsson.bos.so.common.logging.config.CommonLoggingConstants.BEARER_SCHEME;
import static com.ericsson.bos.so.common.logging.config.CommonLoggingConstants.MDC_USERNAME_KEY;
import static com.ericsson.bos.so.common.logging.config.CommonLoggingConstants.PREFERRED_USERNAME;

import java.io.IOException;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ericsson.bos.so.common.logging.security.JWTDecoder;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * UserNameExtractorFilter
 */
@Component
public class UserNameExtractorFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserNameExtractorFilter.class);

    private final ObjectMapper objectMapper;

    /**
     * UserNameExtractorFilter constructor.
     *
     * @param objectMapper
     *            -
     */
    public UserNameExtractorFilter(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private String getUserName(String authorizationHeader) {
        String userName = "";
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_SCHEME)) {
                final String jwtToken = authorizationHeader.substring(7);
                LOGGER.debug("call JWTDecoder.getUsernameFromJWTToken() in UserNameExtractorFilter with the next list of param:jwtToken = {}",
                        jwtToken);
                userName = JWTDecoder.getUsernameFromJWTToken(jwtToken, objectMapper, PREFERRED_USERNAME, false);
            } else {
                LOGGER.debug("No authorization header start with Bearer");
            }
        } catch (Exception exception) {
            LOGGER.warn("Could not get userName from JWT: {}", exception.getMessage());
        }
        LOGGER.debug("Put username to MDC in UserNameExtractorFilter = {}", userName);
        return userName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        LOGGER.debug("AuthorizationHeader header in doFilterInternal: {}", authorizationHeader);
        final String userName = getUserName(authorizationHeader);
        try {
            MDC.put(MDC_USERNAME_KEY, !Strings.isEmpty(userName) ? userName : "");
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_USERNAME_KEY);
        }
    }
}