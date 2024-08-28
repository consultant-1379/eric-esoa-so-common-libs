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

import com.ericsson.bos.so.common.logging.security.extractor.UserNameExtractor;
import jakarta.servlet.http.HttpServletRequest;

/**
 * JwtUserNameExtractorStrategy
 */
public class JwtUserNameExtractorStrategy implements UserNameExtractStrategy {
    private final UserNameExtractor userNameExtractorFromJwt;

    /**
     * JwtUserNameExtractorStrategy constructor
     *
     * @param userNameExtractorFromJwt -
     */
    public JwtUserNameExtractorStrategy(final UserNameExtractor userNameExtractorFromJwt) {
        this.userNameExtractorFromJwt = userNameExtractorFromJwt;
    }

    @Override
    public String extract(final HttpServletRequest request) {
        return userNameExtractorFromJwt.extract(request);
    }
}
