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

import com.ericsson.bos.so.common.logging.utils.PropertySourceUtils;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * LoggingWebFluxPostProcessor
 */
@ConditionalOnWebApplication(
    type = Type.REACTIVE
)
@ConditionalOnClass(name = "org.springframework.cloud.sleuth.instrument.web.TraceWebFilter")
@ConditionalOnBean(type = "org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration")
public class LoggingWebFluxPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(final ConfigurableEnvironment environment,
                                       final SpringApplication application) {
        final Map<String, Object> properties = new HashMap<>();
        if (application.getWebApplicationType().toString().equals(Type.REACTIVE.toString())) {
            properties.put(CommonLoggingConstants.WEBFLUX_KEYS_PROPERTY_NAME, CommonLoggingConstants.MDC_KEYS_PROPERTY_VALUE);
        }
        if (application.getWebApplicationType().toString().equals(Type.SERVLET.toString())) {
            properties.put(CommonLoggingConstants.MDC_KEYS_PROPERTY_NAME, CommonLoggingConstants.MDC_KEYS_PROPERTY_VALUE);
        }
        properties.put(CommonLoggingConstants.PROPAGATION_KEYS_PROPERTY_NAME, CommonLoggingConstants.PROPAGATION_KEYS_PROPERTY_VALUE);
        PropertySourceUtils.putPropertySource(environment.getPropertySources(), properties, CommonLoggingConstants.PROPERTY_SOURCE_NAME);
    }
}
