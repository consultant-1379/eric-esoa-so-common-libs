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
package com.ericsson.bos.so.shared.spring.security.interceptors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_DEFAULT;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_HEADER;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_NAME;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * ForwardTenantInterceptorTest
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class ForwardTenantInterceptorTest {

    private RestTemplate restTemplate;

    private ClientHttpRequestFactory requestFactory;

    private ClientHttpRequest request;

    private ClientHttpResponse response;

    private String tenantValue;

    /**
     * setup - setup for tests
     */
    @BeforeEach
    public void setup() {
        final UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("test", "test");
        final Map<String, Object> details = new HashMap<>();
        tenantValue = "tenant1";
        details.put(TENANT_NAME, tenantValue);
        authentication.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(
                authentication);

        requestFactory = mock(ClientHttpRequestFactory.class);
        request = mock(ClientHttpRequest.class);
        response = mock(ClientHttpResponse.class);

        restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory);
    }

    /**
     * testForwardTenant
     *
     * @throws IOException - throws IOException
     * @throws URISyntaxException - throws URISyntaxException
     */
    @Test
    public void testForwardTenant() throws IOException, URISyntaxException {
        final ForwardTenantInterceptor interceptor =
                new ForwardTenantInterceptor();

        final String url = "http://test.com";

        final HttpHeaders requestHeaders = new HttpHeaders();
        given(requestFactory.createRequest(new URI(url), HttpMethod.POST)).willReturn(request);
        given(request.getHeaders()).willReturn(requestHeaders);

        given(request.execute()).willReturn(response);
        given(response.getStatusCode()).willReturn(HttpStatus.OK);
        given(response.getRawStatusCode()).willReturn(HttpStatus.OK.value());

        restTemplate.setInterceptors(Collections.singletonList(interceptor));

        restTemplate.exchange(url, HttpMethod.POST, null, Void.class);

        final List<String> tenant = requestHeaders.get(TENANT_HEADER);
        assertNotNull(tenant);
        assertLinesMatch(tenant, Collections.singletonList(tenantValue));

    }

    /**
     * testForwardNoTenant
     *
     * @throws IOException - throws IOException
     * @throws URISyntaxException - throws URISyntaxException
     */
    @Test
    public void testForwardNoTenant() throws IOException, URISyntaxException {
        SecurityContextHolder.getContext().setAuthentication(null);
        final ForwardTenantInterceptor interceptor =
                new ForwardTenantInterceptor();

        final String url = "http://test.com";

        final HttpHeaders requestHeaders = new HttpHeaders();
        given(requestFactory.createRequest(new URI(url), HttpMethod.POST)).willReturn(request);
        given(request.getHeaders()).willReturn(requestHeaders);

        given(request.execute()).willReturn(response);
        given(response.getStatusCode()).willReturn(HttpStatus.OK);
        given(response.getRawStatusCode()).willReturn(HttpStatus.OK.value());

        restTemplate.setInterceptors(Collections.singletonList(interceptor));

        restTemplate.exchange(url, HttpMethod.POST, null, Void.class);

        final List<String> tenant = requestHeaders.get(TENANT_HEADER);
        assertNotNull(tenant);
        assertLinesMatch(tenant, Collections.singletonList(TENANT_DEFAULT));

    }

}