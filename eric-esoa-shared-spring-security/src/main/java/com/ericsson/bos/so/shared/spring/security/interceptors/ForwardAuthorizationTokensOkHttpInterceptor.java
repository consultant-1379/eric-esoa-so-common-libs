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

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.ericsson.bos.so.shared.spring.security.utils.Contants.AUTHORIZATION_REFRESH;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TOKEN;
import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TOKEN_REFRESH;

/**
 * OkHttp interceptor to add Bearer Authorization.
 */
public class ForwardAuthorizationTokensOkHttpInterceptor implements Interceptor {

    /**
     *
     * @param chain - okhttp chain
     * @return Response - okhttp response
     * @throws IOException - throws IOException
     */
    @Override
    @SuppressWarnings("unchecked")
    public Response intercept(Chain chain) throws IOException {
        final Headers.Builder headersBuilder = new Headers.Builder();
        final Optional <Authentication> authentication = Optional.ofNullable(SecurityContextHolder
                .getContext().getAuthentication());
        if (authentication.isPresent()) {
            final Map<String, String> details = (Map<String, String>) authentication.get().getDetails();
            final Optional<String> accessToken = Optional.ofNullable(details.get(TOKEN));
            final Optional<String> refreshToken = Optional.ofNullable(details.get(TOKEN_REFRESH));

            accessToken.ifPresent(token -> headersBuilder.add(HttpHeaders.AUTHORIZATION, "Bearer " + token));
            refreshToken.ifPresent(tokenRefresh -> headersBuilder.add(AUTHORIZATION_REFRESH, tokenRefresh));

            final Headers headers = headersBuilder.build();

            if (headers.size() > 0) {
                final Request request = chain.request().newBuilder().headers(headers).build();
                return chain.proceed(request);
            }
        }

        return chain.proceed(chain.request());
    }
}
