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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.JsonTestersAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * CommonLoggingConfigTest
 */
public class CommonLoggingConfigTest {
    public static final String MDC_LOG_COMMON_WEB_FLUX_FILTER = "MDCLogCommonWebFluxFilter";

    public static final String MDC_LOG_COMMON_WEB_MVC_FILTER = "MDCLogCommonWebMvcFilter";

    static {
        System.setProperty("ericsson.tracing.enabled", "true");
        System.setProperty("ericsson.tracing.propagator.type", "b3");
        System.setProperty("ericsson.tracing.exporter.endpoint", "http://eric-dst-sminchiator:4317");
        System.setProperty("ericsson.tracing.sampler.jaeger-remote.endpoint", "http://eric-dst-sbudellator:14333");
        System.setProperty("SERVICE_ID", "known_service");
    }

    /**
     * kafkaApplication
     */
    @Test
    void kafkaApplication() {
        final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(CommonLoggingConfig.class))
                .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
                .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
                .withConfiguration(AutoConfigurations.of(JsonTestersAutoConfiguration.class))
                .withConfiguration(AutoConfigurations.of(KafkaStreamsConfiguration.class))
                .withConfiguration(AutoConfigurations.of(KafkaAutoConfiguration.class));
        contextRunner.run(
            (context) -> {
                assertThat(context.getBean("kafkaListenerContainerFactory")).isNotNull();
                assertThat(catchThrowable(() -> context.getBean(MDC_LOG_COMMON_WEB_MVC_FILTER))).isExactlyInstanceOf(
                    NoSuchBeanDefinitionException.class);
                assertThat(catchThrowable(() -> context.getBean(MDC_LOG_COMMON_WEB_FLUX_FILTER))).isExactlyInstanceOf(
                    NoSuchBeanDefinitionException.class);
            }
        );
    }
}
