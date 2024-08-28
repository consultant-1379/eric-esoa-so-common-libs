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
package com.ericsson.bos.so.security.mtls.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * This class is not needed to be exclusively called by the client.
 * This class will be called by the library internally and keep things ready for
 * its clients
 */
@Configuration
@Deprecated
public class RestTemplateReloadConfig {
    public static final String SIP_TLS_QUALIFIER = "SIP_TLS_REST_TEMPLATE";

    /**
     * Creates RestTemplate bean with "SIP_TLS_REST_TEMPLATE" qualifier.
     * @return RestTemplate
     */
    @Bean
    @Qualifier(SIP_TLS_QUALIFIER)
    public RestTemplate sipTlsRestTemplate() {
        return new RestTemplate();
    }
}
