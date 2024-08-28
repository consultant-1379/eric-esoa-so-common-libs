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
package com.ericsson.bos.so.shared.spring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_DEFAULT;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_HEADER;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_NAME;

/**
 * TenantFilter get the X-Tenant-Id value from header
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    /**
     * doFilterInternal -  handle the request and read the tenant header
     * @param request - the request sent
     * @param response - the response received
     * @param filterChain - spring filter chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        String tenant = request.getHeader(TENANT_HEADER);

        if (StringUtils.isEmpty(tenant)) {
            tenant = TENANT_DEFAULT;
        }

        final Optional<Authentication> auth = Optional.ofNullable(SecurityContextHolder
                .getContext().getAuthentication());
        if (auth.isPresent()) {
            final UsernamePasswordAuthenticationToken authentication =
                    (UsernamePasswordAuthenticationToken) auth.get();

            Object details = authentication.getDetails();
            if (details == null) {
                details = new HashMap<String, Object>();
            }
            final ObjectMapper objectMapper = new ObjectMapper();
            final Map<String, Object> updatedDetails = objectMapper.convertValue(details, Map.class);
            updatedDetails.put(TENANT_NAME, tenant);
            authentication.setDetails(updatedDetails);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
