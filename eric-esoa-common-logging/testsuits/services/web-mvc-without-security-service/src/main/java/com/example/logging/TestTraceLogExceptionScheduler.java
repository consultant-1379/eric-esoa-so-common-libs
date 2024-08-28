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
package com.example.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class TestTraceLogExceptionScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestTraceLogScheduler.class);

    @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 1000)
    public void startTraceProducer() {
        throw new IllegalArgumentException("Producing Illegal Argument Exception");
    }
}
