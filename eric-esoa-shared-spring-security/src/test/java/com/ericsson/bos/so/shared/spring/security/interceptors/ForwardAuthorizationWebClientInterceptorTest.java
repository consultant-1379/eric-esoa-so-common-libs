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

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ericsson.bos.so.shared.spring.security.interceptors.OkHttpTest.createMockResponse;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TOKEN;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

/***
 * ForwardAuthorizationWebClientInterceptorTest
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ForwardAuthorizationWebClientInterceptorTest {

    private WebClient webClient;

    private String tokenValue;

    private MockWebServer webClientMockServer;

    private final int port = 8009;

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

        webClientMockServer = new MockWebServer();
        webClientMockServer.start(port);

        webClient = WebClient.builder()
                .filter(new ForwardAuthorizationWebClientInterceptor())
                .build();

    }

    /**
     * tearDown
     * @throws IOException - throws IOException
     */
    @AfterEach
    public void tearDown() throws IOException {
        webClientMockServer.close();
    }


    /***
     * testForwardWebClientToken
     *
     * @throws InterruptedException - throws InterruptedException
     */
    @Test
    public void testForwardWebClientToken() throws InterruptedException {
        webClientMockServer.enqueue(createMockResponse(HttpStatus.OK, ""));
        final String url = "http://localhost:" + port;
        final String jsonBody = "{\"testKey\": \"testVal\"}";

        webClient.post()
                .uri(url)
                .bodyValue(jsonBody)
                .retrieve()
                .toBodilessEntity().block();

        final RecordedRequest recordedRequest = webClientMockServer.takeRequest(1, TimeUnit.SECONDS);
        final String authorization = recordedRequest.getHeader(HttpHeaders.AUTHORIZATION);
        assertEquals(authorization, "Bearer " + tokenValue);
    }

    /**
     * testForwardNoWebClientToken
     *
     * @throws IOException - throws IOException
     *
     * @throws URISyntaxException - throws URISyntaxException
     */
    @Test
    public void testForwardNoWebClientToken() throws InterruptedException {
        SecurityContextHolder.getContext().setAuthentication(null);
        webClientMockServer.enqueue(createMockResponse(HttpStatus.OK, ""));
        final String url = "http://localhost:" + port;
        final String jsonBody = "{\"testKey\": \"testVal\"}";

        webClient.post()
                .uri(url)
                .bodyValue(jsonBody)
                .retrieve()
                .toBodilessEntity().block();

        final RecordedRequest recordedRequest = webClientMockServer.takeRequest(1, TimeUnit.SECONDS);
        final String authorization = recordedRequest.getHeader(HttpHeaders.AUTHORIZATION);
        assertNull(authorization);
    }
}
