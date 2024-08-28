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

/**
 * CommonLoggingConstants
 */
public final class CommonLoggingConstants {

    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";

    public static final String BASIC_SCHEME = "Basic ";

    public static final String BEARER_SCHEME = "Bearer ";

    public static final String MDC_USERNAME_KEY = "user";

    public static final String MDC_REQUEST_URL = "path";

    public static final String MDC_TRACE_ID_KEY = "traceId";

    public static final String MDC_X_B3_TRACE_ID_KEY = "X-B3-TraceId";

    public static final String[] MDC_TRACE_ID_LIST_KEY = {MDC_TRACE_ID_KEY, MDC_X_B3_TRACE_ID_KEY};

    public static final String PROPERTY_SOURCE_NAME = "ericCommonLoggingProperties";

    public static final String WEBFLUX_KEYS_PROPERTY_NAME = "spring.sleuth.log.slf4j.whitelisted-mdc-keys";

    public static final String MDC_KEYS_PROPERTY_NAME = "spring.sleuth.baggage.remote-fields";

    public static final String MDC_KEYS_PROPERTY_VALUE = "user, path";

    public static final String PROPAGATION_KEYS_PROPERTY_NAME = "spring.sleuth.baggage.remote-fields";

    public static final String PROPAGATION_KEYS_PROPERTY_VALUE = "user, path";

    public static final String PREFERRED_USERNAME = "preferred_username";

    public static final String USER_NAME = "username";

    private CommonLoggingConstants() { }
}
