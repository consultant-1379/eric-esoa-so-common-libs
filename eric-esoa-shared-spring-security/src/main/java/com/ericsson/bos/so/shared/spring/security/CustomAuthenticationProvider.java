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

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 *  CustomAuthenticationProvider - configures authentication
 */
public class CustomAuthenticationProvider implements AuthenticationProvider {

    /**
     * authenticate - create UsernamePasswordAuthenticationToken
     *
     * @param authentication
     * @return UsernamePasswordAuthenticationToken as Authentication
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        return new UsernamePasswordAuthenticationToken(
                authentication.getName(), authentication.getCredentials(), authentication.getAuthorities());
    }

    /**
     * supports - allows the token class to be authenticated
     *
     * @param authentication
     * @return boolean true
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
