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
package com.ericsson.bos.so.shared.spring.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ApplicationServer - used for testing
 */
@SpringBootApplication
public class ApplicationServer {

    /**
     * Start spring application
     *
     * @param args - string[] args for main class
     */
    public static void main(final String[] args) {
        SpringApplication.run(ApplicationServer.class, args);
    }
}
