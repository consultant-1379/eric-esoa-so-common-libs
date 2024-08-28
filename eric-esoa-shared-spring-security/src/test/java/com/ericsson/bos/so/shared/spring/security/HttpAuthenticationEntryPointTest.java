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

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

/**
 * HttpAuthenticationEntryPointTest
 */
public class HttpAuthenticationEntryPointTest {

    private static final String BAD_CREDENTIALS_MESSAGE = "Bad credentials was provided";

    private HttpAuthenticationEntryPoint httpAuthenticationEntryPoint;

    /**
     * setUp
     */
    @BeforeEach
    public void setUp() {
        httpAuthenticationEntryPoint = new HttpAuthenticationEntryPoint();
    }

    /**
     * commence - test setting unauthorized
     *
     * @throws IOException - IOException
     */
    @Test
    public void commence() throws IOException {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final AuthenticationException exception = new BadCredentialsException(BAD_CREDENTIALS_MESSAGE);

        httpAuthenticationEntryPoint.commence(request, response, exception);

        assertThat(SC_UNAUTHORIZED).isEqualTo(response.getStatus());
        assertThat(BAD_CREDENTIALS_MESSAGE).isEqualTo(response.getErrorMessage());
    }
}