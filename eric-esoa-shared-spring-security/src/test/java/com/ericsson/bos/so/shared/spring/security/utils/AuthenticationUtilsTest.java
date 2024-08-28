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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * AuthenticationUtilsTest - test utility class
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AuthenticationUtilsTest {

    private static final String FUNC_USER = "funcuser";

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    /**
     * getUserName - test to get the username from the security context
     *
     * @throws Exception - exception
     */
    @Test
    public void getUserName() throws Exception {
        final Optional<String> expected = Optional.of(FUNC_USER);
        final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(FUNC_USER, null);
        final Authentication authentication = authenticationConfiguration.getAuthenticationManager()
                .authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        final Optional<String> actual = AuthenticationUtils.getUserName();
        assertThat(actual).isEqualTo(expected);
    }
}