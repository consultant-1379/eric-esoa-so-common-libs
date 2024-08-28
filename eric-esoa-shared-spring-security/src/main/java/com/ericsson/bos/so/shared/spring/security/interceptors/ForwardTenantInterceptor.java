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


import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_DEFAULT;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_HEADER;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_NAME;

/**
 * RestTemplate interceptor forwarding tenant header to the
 * HTTP request.
 */

public class ForwardTenantInterceptor implements ClientHttpRequestInterceptor {

    /**
     *
     * @param request - request that was made
     * @param body - body of the request
     * @param execution - ClientHttpRequestExecution
     * @return ClientHttpResponse - ClientHttpResponse
     * @throws IOException - throws IOException
     */
    @SuppressWarnings("unchecked")
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        final Optional <Authentication> authentication = Optional.ofNullable(SecurityContextHolder
                .getContext().getAuthentication());
        if (authentication.isPresent()) {
            final Map<String, String> details = (Map<String, String>) authentication.get().getDetails();
            final Optional<String> tenant = Optional.ofNullable(details.get(TENANT_NAME));

            tenant.ifPresent(name -> request.getHeaders().add(TENANT_HEADER, name));
        } else {
            request.getHeaders().add(TENANT_HEADER, TENANT_DEFAULT);
        }

        return execution.execute(request, body);
    }
}
