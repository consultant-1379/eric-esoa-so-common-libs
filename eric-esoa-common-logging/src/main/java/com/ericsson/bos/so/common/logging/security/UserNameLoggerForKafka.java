/*******************************************************************************
 * COPYRIGHT Ericsson 2024
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

package com.ericsson.bos.so.common.logging.security;

import static com.ericsson.bos.so.common.logging.config.CommonLoggingConstants.MDC_USERNAME_KEY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Utility class to help with logging user name. Provides wrapper methods to make it easy and transparent to mark log statements with the required
 * fields in the ADP JSON format.
 */
@Component
public class UserNameLoggerForKafka {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserNameLoggerForKafka.class);

    private UserNameLoggerForKafka() {
    }

    /**
     * All logs logged will have the ADP log format's "{@code user_name}" field
     *
     * @param userName
     */
    public static void logUserName(final String userName) {
        putUserNameToMDC(StringUtils.hasText(userName) ? userName : "");
    }

    private static void putUserNameToMDC(final String userName) {
        LOGGER.debug("Read and put the username to MDC:{}", userName);
        MDC.put(MDC_USERNAME_KEY, userName);
    }

    /**
     * removeUserNameFromMDC, remove the user key from MDC
     *
     */
    public static void removeUserNameFromMDC() {
        MDC.remove(MDC_USERNAME_KEY);
    }
}
