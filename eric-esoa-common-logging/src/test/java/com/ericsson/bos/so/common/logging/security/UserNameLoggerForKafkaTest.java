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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * UserNameLoggerFromKafkaTest
 */
class UserNameLoggerForKafkaTest {

    private static final String ESOA_ADMIN = "esoa-admin";
    private static final String UNKNOWN = "Unknown";

    /**
     * logUserName
     *
     */
    @Test
    void logUserName() {
        UserNameLoggerForKafka.logUserName(ESOA_ADMIN);
        assertEquals(ESOA_ADMIN, MDC.get(MDC_USERNAME_KEY));
    }

    /**
     * logUserNameWithUnknown
     *
     */
    @Test
    void logUserNameWithUnknown() {
        UserNameLoggerForKafka.logUserName(UNKNOWN);
        assertEquals(UNKNOWN, MDC.get(MDC_USERNAME_KEY));
    }

    /**
     * logUserNameEmptyUser
     *
     */
    @Test
    void logUserNameEmptyUser() {
        UserNameLoggerForKafka.logUserName("");
        assertTrue(MDC.get(MDC_USERNAME_KEY).isEmpty());
    }

    /**
     * removeUserNameKey
     *
     */
    @Test
    void removeUserNameKey() {
        UserNameLoggerForKafka.removeUserNameFromMDC();;
        assertNull(MDC.get(MDC_USERNAME_KEY));
    }

}