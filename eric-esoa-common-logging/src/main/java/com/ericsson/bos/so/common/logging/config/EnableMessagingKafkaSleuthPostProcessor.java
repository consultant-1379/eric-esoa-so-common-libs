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
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * EnableMessagingKafkaSleuthPostProcessor
 */
@ConditionalOnClass(name = "org.springframework.kafka.annotation.EnableKafka")
public class EnableMessagingKafkaSleuthPostProcessor implements EnvironmentPostProcessor {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EnableMessagingKafkaSleuthPostProcessor.class);

    private static final String KAFKA_SLEUTH_PROPERTY_NAME = "spring.sleuth.messaging.kafka.enabled";
    private static final String KAFKA_SLEUTH_PROPERTY_VALUE = "true";

    @Override
    public void postProcessEnvironment(final ConfigurableEnvironment environment,
        final SpringApplication application) {
        LOGGER.debug("EnableMessagingKafkaSleuthPostProcessor  postProcessEnvironment has been executed");
        final Map<String, Object> properties = Collections
                .singletonMap(KAFKA_SLEUTH_PROPERTY_NAME, KAFKA_SLEUTH_PROPERTY_VALUE);
        PropertySourceUtils
                .putPropertySource(environment.getPropertySources(), properties, CommonLoggingConstants.PROPERTY_SOURCE_NAME);

    }
}
