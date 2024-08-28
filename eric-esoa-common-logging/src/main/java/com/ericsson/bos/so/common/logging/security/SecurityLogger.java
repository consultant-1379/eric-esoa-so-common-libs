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

import org.slf4j.MDC;

/**
 * Utility class to help with logging security events. Provides wrapper methods to make it easy and transparent to mark log statements with the
 * required fields in the ADP JSON format.
 * <br/>
 * <br/>
 * See the "SecurityLogger" section in <a href="../../../../../../../../../../README.md">README.md</a> for more information.
 */
public class SecurityLogger {

    private SecurityLogger() {
    }

    /**
     * Runs {@code securityLoggable}. All logs logged during this run will have the ADP log format's "{@code facility}" field
     * set to "{@code log audit}" to mark them as security event logs.
     *
     * @param securityLoggable zero-argument lambda expression containing statements to be run with the "{@code facility}" field set
     */
    public static void withFacility(final Runnable securityLoggable) {
        MDC.put("facility", "log audit");
        try {
            securityLoggable.run();
        } finally {
            MDC.remove("facility");
        }
    }
}
