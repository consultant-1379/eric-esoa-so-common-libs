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
package com.ericsson.bos.so.security.mtls.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class is not needed to be exclusively called by the client.
 * This class will be called by the library internally and keep things ready for its clients
 * This is a Common Class for all optional connection related Configuration
 */
@Component
@Getter
public class ConnectionConfiguration {
    @Value("${connection.readTimeoutMsec:1000}")
    private int readTimeoutMsec;

}
