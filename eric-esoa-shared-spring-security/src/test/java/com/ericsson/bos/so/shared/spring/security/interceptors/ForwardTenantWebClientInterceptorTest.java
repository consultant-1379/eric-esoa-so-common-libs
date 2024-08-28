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

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ericsson.bos.so.shared.spring.security.interceptors.OkHttpTest.createMockResponse;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_DEFAULT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_HEADER;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_NAME;

/***
 * ForwardTenantWebClientInterceptorTest
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class ForwardTenantWebClientInterceptorTest {

    private WebClient webClient;

    private String tenantValue;

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
        tenantValue = "tenant1";
        details.put(TENANT_NAME, tenantValue);
        authentication.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(
                authentication);

        authentication.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(
                authentication);

        webClientMockServer = new MockWebServer();
        webClientMockServer.start(port);

        webClient = WebClient.builder()
                .filter(new ForwardTenantWebClientInterceptor())
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
     * testForwardWebClientTenant
     *
     * @throws InterruptedException - throws InterruptedException
     */
    @Test
    public void testForwardWebClientTenant() throws InterruptedException {
        webClientMockServer.enqueue(createMockResponse(HttpStatus.OK, ""));
        final String url = "http://localhost:" + port;
        final String jsonBody = "{\"testKey\": \"testVal\"}";

        webClient.post()
                .uri(url)
                .bodyValue(jsonBody)
                .retrieve()
                .toBodilessEntity().block();

        final RecordedRequest recordedRequest = webClientMockServer.takeRequest(1, TimeUnit.SECONDS);
        final String tenant = recordedRequest.getHeader(TENANT_HEADER);

        assertEquals(tenant, tenantValue);
    }

    /***
     * testForwardNoWebClientTenant
     *
     * @throws InterruptedException - throws InterruptedException
     */
    @Test
    public void testForwardNoWebClientTenant() throws InterruptedException {
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
        final String tenant = recordedRequest.getHeader(TENANT_HEADER);

        assertEquals(tenant, TENANT_DEFAULT);
    }

}