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
package com.ericsson.bos.so.common.logging.filter;

import com.ericsson.bos.so.common.logging.security.BasicAuthDecoder;
import com.ericsson.bos.so.common.logging.security.JWTDecoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.util.Arrays;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import static com.ericsson.bos.so.common.logging.config.CommonLoggingConstants.AUTHORIZATION_HEADER_NAME;
import static com.ericsson.bos.so.common.logging.config.CommonLoggingConstants.BASIC_SCHEME;
import static com.ericsson.bos.so.common.logging.config.CommonLoggingConstants.BEARER_SCHEME;
import static com.ericsson.bos.so.common.logging.config.CommonLoggingConstants.MDC_USERNAME_KEY;

/**
 * UserNameExtractorKafkaRecordFilter
 */
@Component
@ConditionalOnBean(type = "org.springframework.kafka.config.KafkaListenerContainerFactory")
public class UserNameExtractorKafkaRecordFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            UserNameExtractorKafkaRecordFilter.class);

    private final ObjectMapper objectMapper;

    private final Environment environment;

    private final Closeable doNothing = () -> {
    };

    /**
     * UserNameExtractorKafkaRecordFilter constructor.
     *
     * @param objectMapper -
     * @param environment  -
     */
    public UserNameExtractorKafkaRecordFilter(final ObjectMapper objectMapper, final Environment environment) {
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    private String getPayloadUserNameKey() {
        LOGGER.debug("PAYLOAD_USERNAME_KEY is: {}", environment.getProperty("PAYLOAD_USERNAME_KEY", "preferred_name"));
        return environment.getProperty("PAYLOAD_USERNAME_KEY", "preferred_name");
    }

    /**
     * extractUserNameFromKafkaMessage
     *
     * @param consumerRecord -
     * @return boolean
     */
    public boolean extractUserNameFromKafkaMessage(final ConsumerRecord consumerRecord) {
        try {
            LOGGER.debug("start filter for searching auth header");
            for (Header header : consumerRecord.headers()) {
                final String headerName = header.key();
                final byte[] headerValue = header.value();
                LOGGER.debug("header name: {} = {}", headerName, new String(headerValue));
                if (headerName != null && headerName
                        .equals(AUTHORIZATION_HEADER_NAME)) {
                    final String[] authHeaderValue = new String(headerValue).split(" ");
                    LOGGER.debug("auth header is found; values: {}", Arrays.toString(authHeaderValue));

                    if (authHeaderValue != null && authHeaderValue.length == 2) {
                        String userName = "";
                        if (authHeaderValue[1].contains(BASIC_SCHEME)) {
                            LOGGER.debug("call BasicAuthDecoder.getUsernameFromBasicAuthToken() in KafkaRecordFilter" +
                                    "with the next list of param:authHeaderValue[1] = {};", authHeaderValue[1]);
                            userName = BasicAuthDecoder.getUsernameFromBasicAuthToken(authHeaderValue[1]);
                        } else if (authHeaderValue[1].contains(BEARER_SCHEME)) {
                            LOGGER.debug("call JWTDecoder.getUsernameFromJWTToken() in KafkaRecordFilter" +
                                            "with the next list of param:authHeaderValue[1] = {}; payloadUserNameKey = {}",
                                    authHeaderValue[1], getPayloadUserNameKey());
                            userName = JWTDecoder.getUsernameFromJWTToken(authHeaderValue[1],
                                    objectMapper, getPayloadUserNameKey(), false);
                        }
                        LOGGER.debug("put username to MDC in KafkaRecordFilter = {}", userName);
                        putUserNameToMDC(!Strings.isEmpty(userName) ? userName : "");
                        break;
                    }
                }
            }
        } finally {
            return false;
        }
    }

    private Closeable putUserNameToMDC(final String userName) {
        try {
            if (!StringUtils.isEmpty(userName)) {
                return MDC.putCloseable(MDC_USERNAME_KEY, userName);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return doNothing;
    }
}