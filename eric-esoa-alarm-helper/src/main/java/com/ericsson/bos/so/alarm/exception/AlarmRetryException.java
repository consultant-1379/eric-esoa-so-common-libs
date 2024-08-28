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
package com.ericsson.bos.so.alarm.exception;

/**
 * Alarm retry exception.
 */
public class AlarmRetryException extends RuntimeException {

    /**
     * Instantiates a new Alarm retry exception.
     */
    public AlarmRetryException() {
        super();
    }

    /**
     * Instantiates a new Alarm retry exception.
     *
     * @param reason
     *         the reason
     * @param exception
     *         the exception
     */
    public AlarmRetryException(final String reason, final Exception exception) {
        super(reason, exception);
    }
}

