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

import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_DEFAULT;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_HEADER;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TENANT_NAME;

/***
 * WebClient interceptor to tenant header
 */
public class ForwardTenantWebClientInterceptor implements ExchangeFilterFunction {

    /***
     * Exchange filter to add the tenants
     *
     * @param request
     * @param next
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        ClientRequest clientRequest = ClientRequest.from(request)
                .build();
        final Optional <Authentication> authentication = Optional.ofNullable(SecurityContextHolder
                .getContext().getAuthentication());
        if (authentication.isPresent()) {
            final Map<String, String> details = (Map<String, String>) authentication.get().getDetails();
            final Optional<String> tenant = Optional.ofNullable(details.get(TENANT_NAME));

            if (tenant.isPresent()) {
                clientRequest = ClientRequest.from(clientRequest).header(TENANT_HEADER, tenant.get()).build();
            }
        } else {
            clientRequest = ClientRequest.from(clientRequest).header(TENANT_HEADER, TENANT_DEFAULT).build();
        }

        return next.exchange(clientRequest);
    }
}
