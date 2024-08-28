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
import java.util.Map;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * PropertySourceUtils
 */
public class PropertySourceUtils {

    /**
     * putPropertySource constructor.
     *
     * @param propertySources -
     * @param map -
     * @param propertySourcesName -
     */
    public static void putPropertySource(final MutablePropertySources propertySources,
                                         final Map<String, Object> map, final String propertySourcesName) {

        updateAppSleuthKeys(propertySources, map);

        if (!propertySources.contains(propertySourcesName)) {
            propertySources.addFirst(new MapPropertySource(propertySourcesName, map));
        } else {
            updatePropertySource(propertySources.get(propertySourcesName), map);
        }
    }

    private static void updatePropertySource(final PropertySource propertySource,
                                             final Map<String, Object> map) {

        if (propertySource instanceof MapPropertySource) {
            final MapPropertySource ps = (MapPropertySource) propertySource;
            for (String key : map.keySet()) {
                if (!ps.containsProperty(key)) {
                    ps.getSource().put(key, map.get(key));
                }
            }
        }
    }

    private static void updateAppSleuthKeys(final MutablePropertySources propertySources,
                                            final Map<String, Object> map) {

        for (PropertySource source : propertySources) {
            if (source instanceof MapPropertySource) {
                final MapPropertySource ps = (MapPropertySource) source;
                //Merge propagation keys from all application properties of active profiles and logging library
                if (ps.containsProperty(CommonLoggingConstants.PROPAGATION_KEYS_PROPERTY_NAME)) {
                    updateSleuthKey(ps, map, CommonLoggingConstants.PROPAGATION_KEYS_PROPERTY_NAME);
                }

                //Merge MDC keys from all application properties of active profiles and logging library
                if (ps.containsProperty(CommonLoggingConstants.MDC_KEYS_PROPERTY_NAME)) {
                    updateSleuthKey(ps, map, CommonLoggingConstants.MDC_KEYS_PROPERTY_NAME);
                }
            }
        }
    }

    private static void updateSleuthKey(final MapPropertySource ps, final Map<String, Object> map, final String key) {
        final String appValue = ps.getSource().get(key).toString();
        if (map.get(key) != null && !map.get(key).toString().contains(appValue)) {
            map.merge(key, appValue,
                (oldValue, newValue) -> String.format("%s, %s", oldValue, newValue));
        }
    }
}