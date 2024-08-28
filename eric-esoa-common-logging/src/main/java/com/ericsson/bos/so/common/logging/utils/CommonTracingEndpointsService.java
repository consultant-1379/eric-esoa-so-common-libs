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
package com.ericsson.bos.so.common.logging.utils;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * CommonTracingEndpointsService
 */
@Component
public class CommonTracingEndpointsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonTracingEndpointsService.class);

    private final Set<String> excludedEndpoints;

    /**
     * See details about SpEL expression as default value:
     *   https://stackoverflow.com/questions/42920606/spring-value-empty-list-as-default
     *   https://www.java2novice.com/issues/default-value-for-spring-value-annotation/
     *   https://blog.abelotech.com/posts/useful-things-spring-expression-language-spel/
     *
     * @param excludedEndpoints -
     */
    public CommonTracingEndpointsService(@Value("${ericsson.tracing.endpoints.exclude:}#{T(java.util.Collections).emptySet()}")
                                         final Set<String> excludedEndpoints) {
        this.excludedEndpoints = excludedEndpoints;
    }

    @PostConstruct
    private void init() {
        //Validation of endpoints list to exclude
        excludedEndpoints.forEach((endpoint) -> {
            if (!(endpoint.length() > 1 && endpoint.startsWith("/") && endpoint.charAt(1) != '/')) {
                LOGGER.warn(
                        "This endpoint '{}' could not be excluded because it does not match URI pattern. "
                                + "Pattern: URI must start from one leading '/'",
                        endpoint);
            }
        });
    }

    /**
     * isExcluded
     *
     * @param url -
     * @return boolean
     */
    public boolean isExcluded(final String url) {
        if (excludedEndpoints.contains(url)) {
            return true;
        }
        for (String endpoint : excludedEndpoints) {
            if (url.matches(endpoint)) {
                return true;
            }
        }
        return false;
    }

}
