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
package com.ericsson.bos.so.security.mtls.model;

import lombok.Getter;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to cache ClientHttpRequestFactory.
 * Any customized RestTemplate created, same will cache for reload SSL Context for the same WebClient Builder.
 */
@Component
public class WebClientHttpConnectorFactoryCache {

    @Getter
    private static Map<WebClient.Builder, ClientHttpConnector> clientHttpConnectorMap = new HashMap<>();

    /**
     * Method to get cached ClientHttpRequestFactory.
     *
     * @return Map of RestTemplate as key and ClientHttpRequestFactory as value.
     */
    public Map<WebClient.Builder, ClientHttpConnector> getRequestFactoryMap() {
        return clientHttpConnectorMap;
    }

}
