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
package com.ericsson.bos.so.security.mtls.exception;

public class CertificateException extends Exception {

    /**
     * Certificate exception constructor method
     *
     * @param msg       exception message
     * @param exception exception
     */
    public CertificateException(String msg, Exception exception) {
        super(msg, exception);
    }
}
