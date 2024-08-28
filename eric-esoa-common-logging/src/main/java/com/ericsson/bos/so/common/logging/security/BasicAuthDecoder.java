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
package com.ericsson.bos.so.common.logging.security;

import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.ericsson.bos.so.common.logging.config.CommonLoggingConstants.BASIC_SCHEME;

/**
 * BasicAuthDecoder
 */

public class BasicAuthDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthDecoder.class);

    private BasicAuthDecoder() {
        throw new AssertionError("The utility class must not be instantiated");
    }

    /**
     * getUsernameFromBasicAuthToken
     *
     * @param basicAuthToken -
     * @return String
     */
    public static String getUsernameFromBasicAuthToken(final String basicAuthToken) {
        if (basicAuthToken == null) {
            LOGGER.trace("Basic Auth Token is null");
            return null;
        }
        try {
            // Decode the Basic Auth token to extract the username
            final String decodedToken = new String(Base64.getDecoder().decode(basicAuthToken.substring(BASIC_SCHEME.length())));
            final String[] parts = decodedToken.split(":");
            return parts[0];
        } catch (Exception e) {
            LOGGER.warn("Could not get userName from Basic Auth Token: {}", e.getMessage());
        }
        return null;
    }

}
