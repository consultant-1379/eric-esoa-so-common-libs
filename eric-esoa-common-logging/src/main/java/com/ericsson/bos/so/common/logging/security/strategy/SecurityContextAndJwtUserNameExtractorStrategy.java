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
import org.springframework.util.StringUtils;

/**
 * SecurityContextAndJwtUserNameExtractorStrategy
 */
public class SecurityContextAndJwtUserNameExtractorStrategy implements UserNameExtractStrategy {

    private final UserNameExtractor userNameExtractorFromSecurityContext;
    private final UserNameExtractor userNameExtractorFromJwt;

    /**
     * SecurityContextAndJwtUserNameExtractorStrategy constructor
     *
     * TODO(eankinn) find a better way to set up a strategy and define extractors.
     *     Probably in the library there is no need to take data from SecurityContext
     *
     * @param userNameExtractorFromSecurityContext -
     * @param userNameExtractorFromJwt -
     */
    public SecurityContextAndJwtUserNameExtractorStrategy(final UserNameExtractor userNameExtractorFromSecurityContext,
                                                          final UserNameExtractor userNameExtractorFromJwt) {
        this.userNameExtractorFromSecurityContext = userNameExtractorFromSecurityContext;
        this.userNameExtractorFromJwt = userNameExtractorFromJwt;
    }

    @Override
    public String extract(final HttpServletRequest request) {
        final String username = userNameExtractorFromSecurityContext.extract(request);
        return StringUtils.isEmpty(username) ? userNameExtractorFromJwt.extract(request) : username;
    }
}
