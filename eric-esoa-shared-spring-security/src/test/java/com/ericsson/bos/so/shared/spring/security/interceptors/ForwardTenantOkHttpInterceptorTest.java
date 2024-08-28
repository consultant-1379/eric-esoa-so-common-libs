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

import okhttp3.OkHttpClient;
import okhttp3.Request;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ericsson.bos.so.shared.spring.security.interceptors.OkHttpTest.createMockResponse;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_DEFAULT;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_HEADER;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ForwardTenantOkHttpInterceptorTest
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class ForwardTenantOkHttpInterceptorTest {

    private OkHttpClient client;

    private MockWebServer mockWebServer;

    private String tenantValue;

    private final int port = 8001;

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

        mockWebServer = new MockWebServer();
        mockWebServer.start(port);

        client = new OkHttpClient.Builder()
                .addInterceptor(new ForwardTenantOkHttpInterceptor())
                .build();

    }

    /**
     * tearDown
     * @throws IOException - throws IOException
     */
    @AfterEach
    public void tearDown() throws IOException {
        mockWebServer.close();
    }

    /**
     * testForwardOkHttpToken
     *
     * @throws IOException - throws IOException
     * @throws InterruptedException - throws InterruptedException
     */
    @Test
    public void testForwardOkHttpTenant() throws IOException, InterruptedException {
        mockWebServer.enqueue(createMockResponse(HttpStatus.OK, ""));

        final String url = "http://localhost:" + port;

        final Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).execute();

        final RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        final String tenant = recordedRequest.getHeader(TENANT_HEADER);

        assertEquals(tenant, tenantValue);

    }

    /**
     * testForwardNoOkHttpTenant
     *
     * @throws IOException - throws IOException
     * @throws InterruptedException - throws InterruptedException
     */
    @Test
    public void testForwardNoOkHttpTenant() throws IOException, InterruptedException {
        SecurityContextHolder.getContext().setAuthentication(null);
        mockWebServer.enqueue(createMockResponse(HttpStatus.OK, ""));

        final String url = "http://localhost:" + port;

        final Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).execute();

        final RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        final String tenant = recordedRequest.getHeader(TENANT_HEADER);

        assertEquals(tenant, TENANT_DEFAULT);

    }

}