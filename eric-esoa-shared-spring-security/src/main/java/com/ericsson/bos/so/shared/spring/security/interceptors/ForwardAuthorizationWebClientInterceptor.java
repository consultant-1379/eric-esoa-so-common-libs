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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

import static com.ericsson.bos.so.shared.spring.security.utils.Contants.AUTHORIZATION_REFRESH;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TOKEN;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TOKEN_REFRESH;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/***
 *  WebClient interceptor to add Bearer Authorization.
 */
public class ForwardAuthorizationWebClientInterceptor implements ExchangeFilterFunction {

    /***
     * Exchange filter to add access and refresh tokens
     *
     * @param request
     * @param next
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public Mono<ClientResponse> filter(final ClientRequest request, final ExchangeFunction next) {
        ClientRequest clientRequest = ClientRequest.from(request)
                .build();
        final Optional <Authentication> authentication = Optional.ofNullable(SecurityContextHolder
                .getContext().getAuthentication());
        if (authentication.isPresent()) {
            final Map<String, String> details = (Map<String, String>) authentication.get().getDetails();
            final Optional<String> accessToken = Optional.ofNullable(details.get(TOKEN));
            final Optional<String> refreshToken = Optional.ofNullable(details.get(TOKEN_REFRESH));

            if (accessToken.isPresent()) {
                clientRequest = buildClientRequest(clientRequest, AUTHORIZATION, "Bearer " + accessToken.get());
            }

            if (refreshToken.isPresent()) {
                clientRequest = buildClientRequest(clientRequest, AUTHORIZATION_REFRESH, refreshToken.get());
            }
        }

        return next.exchange(clientRequest);
    }

    private ClientRequest buildClientRequest(final ClientRequest clientRequest, final String header, final String value) {
        return ClientRequest.from(clientRequest)
                .header(header, value)
                .build();
    }
}
