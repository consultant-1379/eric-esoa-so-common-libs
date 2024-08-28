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
package com.ericsson.bos.so.common.logging.config;


import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SkipNullTokenLoggingConfig
 */
@Configuration
@ConfigurationProperties(prefix = "logging", ignoreInvalidFields = true)
public class SkipNullTokenLoggingConfig {
    private List<String> skipNullTokenEndpoints;

    public List<String> getSkipNullTokenEndpoints() {
        return skipNullTokenEndpoints;
    }

    public void setSkipNullTokenEndpoints(final List<String> skipNullTokenEndpoints) {
        this.skipNullTokenEndpoints = skipNullTokenEndpoints;
    }
}