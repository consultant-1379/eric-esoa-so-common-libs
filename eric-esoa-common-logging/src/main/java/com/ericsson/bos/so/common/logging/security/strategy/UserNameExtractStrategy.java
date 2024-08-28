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
package com.ericsson.bos.so.common.logging.security.strategy;

import jakarta.servlet.http.HttpServletRequest;

/**
 * UserNameExtractStrategy
 */
public interface UserNameExtractStrategy {
    /**
     * extract
     *
     * @param request -
     * @return String
     */
    String extract(HttpServletRequest request);
}
