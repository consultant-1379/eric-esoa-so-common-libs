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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * DisableBannerPostProcessor
 */
public class DisableBannerPostProcessor implements EnvironmentPostProcessor {

    private static final String BANNER_PROPERTY_NAME = "spring.main.banner-mode";
    private static final String BANNER_PROPERTY_VALUE = "off";

    @Override
    public void postProcessEnvironment(final ConfigurableEnvironment environment,
                                       final SpringApplication application) {
        final Map<String, Object> properties = Collections.singletonMap(BANNER_PROPERTY_NAME, BANNER_PROPERTY_VALUE);
        PropertySourceUtils.putPropertySource(environment.getPropertySources(), properties, CommonLoggingConstants.PROPERTY_SOURCE_NAME);
    }
}
