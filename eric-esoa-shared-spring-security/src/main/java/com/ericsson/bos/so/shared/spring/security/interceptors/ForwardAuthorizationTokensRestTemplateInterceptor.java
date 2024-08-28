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

import static com.ericsson.bos.so.shared.spring.security.utils.Contants.AUTHORIZATION_REFRESH;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TOKEN;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TOKEN_REFRESH;

/**
 * RestTemplate interceptor forwarding Authorization tokens to the
 * HTTP request.
 */

public class ForwardAuthorizationTokensRestTemplateInterceptor implements ClientHttpRequestInterceptor {

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
            final Optional<String> accessToken = Optional.ofNullable(details.get(TOKEN));
            final Optional<String> refreshToken = Optional.ofNullable(details.get(TOKEN_REFRESH));

            accessToken.ifPresent(token -> request.getHeaders().setBearerAuth(token));
            refreshToken.ifPresent(tokenRefresh -> request.getHeaders().add(AUTHORIZATION_REFRESH, tokenRefresh));
        }
        return execution.execute(request, body);
    }
}
