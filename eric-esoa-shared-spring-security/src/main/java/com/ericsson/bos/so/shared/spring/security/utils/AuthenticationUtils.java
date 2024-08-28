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
package com.ericsson.bos.so.shared.spring.security.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;

import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_NAME;

/**
   AuthenticationUtils - provides utility to get information
 */
public final class AuthenticationUtils {

    private AuthenticationUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * getUserName
     *
     * @return optional string username
     */
    public static Optional<String> getUserName() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Optional.ofNullable(authentication)
                .filter(auth -> auth instanceof UsernamePasswordAuthenticationToken)
                .map(Authentication::getName);
    }

    /**
     * getTenant
     *
     * @return optional string tenant
     */
    @SuppressWarnings("unchecked")
    public static Optional<String> getTenant() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Object token = SecurityContextHolder.getContext().getAuthentication().getDetails();
        final Map<String, Object> details = objectMapper.convertValue(token, Map.class);
        return Optional.ofNullable(details.get(TENANT_NAME).toString());
    }
}
