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

import com.ericsson.bos.so.common.logging.config.CommonLoggingConstants;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.test.context.TestPropertySource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * PropertySourceUtilsTest
 */
@SpringBootTest
@SpringBootConfiguration
@TestPropertySource(properties = {
    "spring.sleuth.log.slf4j.whitelisted-mdc-keys=FieldFromApplicationYAML",
    "spring.sleuth.propagation-keys=FieldFromApplicationYAML"
})
public class PropertySourceUtilsTest {
    public static final String TEST_KEY = "Test_key";

    @Autowired
    private ConfigurableEnvironment environment;

    /**
     * putNewPropertySource
     */
    @Test
    void putNewPropertySource() {
        //given
        final MutablePropertySources propertySources = new MutablePropertySources();
        final Map<String, Object> map = Collections.singletonMap(TEST_KEY, "Test_value");
        final String propertySourceName = CommonLoggingConstants.PROPERTY_SOURCE_NAME;

        //when
        PropertySourceUtils.putPropertySource(propertySources, map, propertySourceName);

        //then
        assertThat(propertySources.contains(propertySourceName)).isTrue();
        assertThat(propertySources.get(propertySourceName).containsProperty(TEST_KEY)).isTrue();
        assertThat(propertySources.get(propertySourceName).getProperty(TEST_KEY)).isEqualTo("Test_value");
    }

    /**
     * putExistedPropertySource
     */
    @Test
    void putExistedPropertySource() {
        //given
        final MutablePropertySources propertySources = new MutablePropertySources();
        final Map<String, Object> map = new HashMap<>();
        map.put(TEST_KEY, "Test_value");
        final String propertySourceName = CommonLoggingConstants.PROPERTY_SOURCE_NAME;

        //when
        PropertySourceUtils.putPropertySource(propertySources, map, propertySourceName);
        map.put(TEST_KEY, "Test_value_updated");
        PropertySourceUtils.putPropertySource(propertySources, map, propertySourceName);

        //then
        assertThat(propertySources.contains(propertySourceName)).isTrue();
        assertThat(propertySources.get(propertySourceName).containsProperty(TEST_KEY)).isTrue();
        assertThat(propertySources.get(propertySourceName).getProperty(TEST_KEY)).isEqualTo("Test_value_updated");
    }

    /**
     * mergePropertySource
     */
    @Test
    void mergePropertySource() {
        //given
        final MutablePropertySources propertySources = environment.getPropertySources();
        final String propertySourceName = CommonLoggingConstants.PROPERTY_SOURCE_NAME;

        //then
        assertThat(propertySources.contains(propertySourceName)).isTrue();

        assertThat(propertySources.get(propertySourceName).containsProperty(CommonLoggingConstants.PROPAGATION_KEYS_PROPERTY_NAME)).isTrue();
        assertThat(propertySources.get(propertySourceName).getProperty(
                CommonLoggingConstants.PROPAGATION_KEYS_PROPERTY_NAME)).isEqualTo("user, path");

        assertThat(propertySources.get(propertySourceName).containsProperty(CommonLoggingConstants.MDC_KEYS_PROPERTY_NAME)).isTrue();
        assertThat(propertySources.get(propertySourceName).getProperty(CommonLoggingConstants.MDC_KEYS_PROPERTY_NAME)).isEqualTo("user, path");
    }
}