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

import com.google.common.io.Resources;
import kotlin.text.Charsets;
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

import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TOKEN;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * ForwardAuthorizationTokensRestTemplateInterceptorTest
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class ForwardAuthorizationTokensRestTemplateInterceptorTest {

    private RestTemplate restTemplate;

    private ClientHttpRequestFactory requestFactory;

    private ClientHttpRequest request;

    private ClientHttpResponse response;

    private String tokenValue;

    /**
     * setup - setup for tests
     *
     * @throws IOException - throws IOException
     */
    @BeforeEach
    public void setup() throws IOException {
        final UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("test", "test");
        final Map<String, Object> details = new HashMap<>();
        tokenValue = Resources.toString(
                Resources.getResource("token.txt"), Charsets.UTF_8);

        details.put(TOKEN, tokenValue);
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
     * testForwardToken
     *
     * @throws IOException - throws IOException
     *
     * @throws URISyntaxException - throws URISyntaxException
     */
    @Test
    public void testForwardToken() throws IOException, URISyntaxException {
        final ForwardAuthorizationTokensRestTemplateInterceptor interceptor =
                new ForwardAuthorizationTokensRestTemplateInterceptor();

        final String url = "http://test.com";

        final HttpHeaders requestHeaders = new HttpHeaders();
        given(requestFactory.createRequest(new URI(url), HttpMethod.POST)).willReturn(request);
        given(request.getHeaders()).willReturn(requestHeaders);

        given(request.execute()).willReturn(response);
        given(response.getStatusCode()).willReturn(HttpStatus.OK);
        given(response.getRawStatusCode()).willReturn(HttpStatus.OK.value());

        restTemplate.setInterceptors(Collections.singletonList(interceptor));

        restTemplate.exchange(url, HttpMethod.POST, null, Void.class);

        final List<String> authorization = requestHeaders.get(HttpHeaders.AUTHORIZATION);
        assertNotNull(authorization);
        assertLinesMatch(authorization, Collections.singletonList("Bearer " + tokenValue));

    }

    /**
     * testForwardNoToken
     *
     * @throws IOException - throws IOException
     *
     * @throws URISyntaxException - throws URISyntaxException
     */
    @Test
    public void testForwardNoToken() throws IOException, URISyntaxException {
        SecurityContextHolder.getContext().setAuthentication(null);
        final ForwardAuthorizationTokensRestTemplateInterceptor interceptor =
                new ForwardAuthorizationTokensRestTemplateInterceptor();

        final String url = "http://test.com";

        final HttpHeaders requestHeaders = new HttpHeaders();
        given(requestFactory.createRequest(new URI(url), HttpMethod.POST)).willReturn(request);
        given(request.getHeaders()).willReturn(requestHeaders);

        given(request.execute()).willReturn(response);
        given(response.getStatusCode()).willReturn(HttpStatus.OK);
        given(response.getRawStatusCode()).willReturn(HttpStatus.OK.value());

        restTemplate.setInterceptors(Collections.singletonList(interceptor));

        restTemplate.exchange(url, HttpMethod.POST, null, Void.class);

        final List<String> authorization = requestHeaders.get(HttpHeaders.AUTHORIZATION);
        assertNull(authorization);
    }

}