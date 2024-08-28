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

import com.ericsson.bos.so.shared.spring.security.model.JwtModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ericsson.bos.so.shared.spring.security.utils.Contants.AUTHORIZATION_REFRESH;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.DECRYPTED_TOKEN;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TOKEN;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TOKEN_PREFIX;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TOKEN_REFRESH;

/**
 * JwtBasicAuthenticationFilter read the jwt token and adds to spring security
 */
public class JwtBasicAuthenticationFilter extends BasicAuthenticationFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtBasicAuthenticationFilter.class);

    /**
     * JwtBasicAuthenticationFilter contructor
     *
     * @param authenticationManager - authenticationManager
     */
    JwtBasicAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    /**
     * doFilterInternal - handle the request and read the JWT
     * @param request - the request sent
     * @param response - the response received
     * @param filterChain - spring filter chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        final AuthenticationManager authenticationManager = getAuthenticationManager();
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        final Authentication authenticate;
        if (StringUtils.isEmpty(header) || !header.startsWith(TOKEN_PREFIX)) {
            authenticate = authenticationManager.authenticate(getAnonymousUser());
        } else {
            authenticate = authenticationManager.authenticate(getAuthentication(request));
        }

        SecurityContextHolder.getContext().setAuthentication(authenticate);
        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private static UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        final String token = request.getHeader(HttpHeaders.AUTHORIZATION).replace(TOKEN_PREFIX, "");
        if (StringUtils.isNotEmpty(token)) {
            try {

                final JWT jwt = JWTParser.parse(token);
                final JWTClaimsSet jwtClaimSet = jwt.getJWTClaimsSet();
                final ObjectMapper objectMapper = new ObjectMapper();
                final JwtModel mappedToken = objectMapper.readValue(jwtClaimSet.toString(true),
                        JwtModel.class);

                final String username = mappedToken.getPreferredUsername();

                final List<GrantedAuthority> authorities = mappedToken.getResourceAccess().getRoles()
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                if (StringUtils.isNotEmpty(username)) {
                    final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    final Map<String, Object> details = new HashMap<>();
                    details.put(DECRYPTED_TOKEN, jwtClaimSet);
                    setTokens(request, token, details);
                    usernamePasswordAuthenticationToken.setDetails(details);
                    return usernamePasswordAuthenticationToken;
                }
            } catch (Exception exception) {
                LOGGER.warn("JWT failed to parse : {} failed : {}", token, exception.getMessage());
            }
        }

        return getAnonymousUser();
    }

    private static void setTokens(HttpServletRequest request, String token, Map<String, Object> details) {
        details.put(TOKEN, token);

        final Optional<String> refreshToken = Optional.ofNullable(request.getHeader(AUTHORIZATION_REFRESH));
        refreshToken.ifPresent(item -> details.put(TOKEN_REFRESH, item));
    }

    private static UsernamePasswordAuthenticationToken getAnonymousUser() {
        final List<GrantedAuthority> authorities = new ArrayList<>();
        final Map<String, Object> details = new HashMap<>();
        final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken("Unknown", null, authorities);
        usernamePasswordAuthenticationToken.setDetails(details);
        return usernamePasswordAuthenticationToken;
    }
}
