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
package com.ericsson.bos.so.security.mtls.client.postgres;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Read and modify JDBC URl in the format 'jdbc:postgresql://host:port/database?properties'.
 */
class JdbcUrl {

    private static final String PROPERTIES_SEPARATOR = "?";

    private final String jdbcUrlWithoutProps;
    private final Map<String, String> jdbcProperties;

    /**
     * Jdbc url in the format 'jdbc:postgresql://host:port/database?properties'.
     * @param url the ssl params
     */
    JdbcUrl(final String url) {
        Objects.requireNonNull(url, "JDBC url cannot be null");
        String properties = null;
        if (url.contains(PROPERTIES_SEPARATOR)) {
            this.jdbcUrlWithoutProps = url.substring(0, url.lastIndexOf(PROPERTIES_SEPARATOR));
            properties = url.substring(url.lastIndexOf(PROPERTIES_SEPARATOR) + 1);
        } else {
            this.jdbcUrlWithoutProps = url;
        }
        if (Objects.isNull(properties)) {
            this.jdbcProperties = Collections.emptyMap();
        } else {
            final var keyValuePairs = properties.split("&");
            this.jdbcProperties = Arrays.stream(keyValuePairs).map(kv -> kv.split("="))
                    .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1], (x, y) -> y, LinkedHashMap::new));
        }
    }

    /**
     * Get the sslkey value.
     * @return sslkey value.
     */
    Optional<String> getSslKey() {
        return Optional.ofNullable(jdbcProperties.get("sslkey"));
    }

    /**
     * Update the sslKey value.
     * @param value sslkey value.
     */
    public void setSslKey(final String value) {
        jdbcProperties.put("sslkey", value);
    }

    @Override
    public String toString() {
        final StringBuilder jdbcUrl = new StringBuilder(jdbcUrlWithoutProps);
        if (!jdbcProperties.isEmpty()) {
            jdbcUrl.append(PROPERTIES_SEPARATOR)
                    .append(jdbcProperties.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&")));
        }
        return jdbcUrl.toString();
    }
}