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

import com.ericsson.bos.so.shared.spring.security.utils.AuthenticationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_HEADER;

/**
 * TenantFilterTest - test
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class TenantFilterTest {

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    /**
     * testGetTenant - get the tenant header value
     * @throws Exception - exception for mocks
     */
    @Test
    public void testGetTenant() throws Exception {
        final MockFilterChain filterchain = new MockFilterChain();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(HttpHeaders.AUTHORIZATION, "");

        final JwtBasicAuthenticationFilter jwtBasicAuthenticationFilter = new JwtBasicAuthenticationFilter(
                authenticationConfiguration.getAuthenticationManager());

        jwtBasicAuthenticationFilter.doFilterInternal(request, response, filterchain);

        final MockFilterChain tenantFilterchain = new MockFilterChain();
        final MockHttpServletRequest tenantRequest = new MockHttpServletRequest();
        final MockHttpServletResponse tenantResponse = new MockHttpServletResponse();

        final String tenantValue = "tenant1";
        tenantRequest.addHeader(TENANT_HEADER, tenantValue);

        final TenantFilter tenantFilter = new TenantFilter();
        tenantFilter.doFilterInternal(tenantRequest, tenantResponse, tenantFilterchain);

        final Optional<String> tenant = AuthenticationUtils.getTenant();

        assertThat(tenant.isPresent()).isTrue();
        assertThat(tenant.get()).isEqualTo(tenantValue);
    }

    /**
     * testSettingDefaultTenant - if not tenant header is set then use default value
     *
     * @throws Exception - exception for mocks
     */
    @Test
    public void testSettingDefaultTenant() throws Exception {
        final MockFilterChain filterchain = new MockFilterChain();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(HttpHeaders.AUTHORIZATION, "");

        final JwtBasicAuthenticationFilter jwtBasicAuthenticationFilter = new JwtBasicAuthenticationFilter(
                authenticationConfiguration.getAuthenticationManager());

        jwtBasicAuthenticationFilter.doFilterInternal(request, response, filterchain);

        final MockFilterChain tenantFilterchain = new MockFilterChain();
        final MockHttpServletRequest tenantRequest = new MockHttpServletRequest();
        final MockHttpServletResponse tenantResponse = new MockHttpServletResponse();

        final String tenantValue = "unknown";

        final TenantFilter tenantFilter = new TenantFilter();
        tenantFilter.doFilterInternal(tenantRequest, tenantResponse, tenantFilterchain);

        final Optional<String> tenant = AuthenticationUtils.getTenant();

        assertThat(tenant.isPresent()).isTrue();
        assertThat(tenant.get()).isEqualTo(tenantValue);
    }

}