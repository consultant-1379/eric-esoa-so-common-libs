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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * SecurityContextUserNameExtractorTest
 */
public class SecurityContextUserNameExtractorTest {

    private final SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    private final UserNameExtractor userNameExtractorFromSecurityContext = new SecurityContextUserNameExtractor();

    /**
     * testGetUserNameWithSecurityContextNoUserName
     */
    @Test
    void testGetUserNameWithSecurityContextNoUserName() {
        //given
        SecurityContextHolder.setContext(securityContext);

        //when
        final String userName = userNameExtractorFromSecurityContext.extract(null);

        //then
        assertThat(userName).isEmpty();
    }

    /**
     * testGetUserNameWithSecurityContextUserName
     */
    @Test
    void testGetUserNameWithSecurityContextUserName() {
        //given
        final Authentication authentication = Mockito.mock(UsernamePasswordAuthenticationToken.class);
        SecurityContextHolder.setContext(securityContext);

        //when
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test");
        final String userName = userNameExtractorFromSecurityContext.extract(null);

        //then
        assertThat(userName).isEqualTo("test");
    }
}
