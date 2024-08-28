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
package com.ericsson.bos.so.common.logging.security.extractor;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SecurityContextUserNameExtractor
 */
@Slf4j
public class SecurityContextUserNameExtractor implements UserNameExtractor {

    @Override
    public String extract(final HttpServletRequest request) {
        try {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return Optional.ofNullable(authentication)
                    .filter(auth -> auth instanceof UsernamePasswordAuthenticationToken)
                    .map(Authentication::getName)
                    .orElse("");
        } catch (Exception e) {
            log.warn("Could not extract userName from Spring Context: {}", e.getMessage());
        }
        return null;
    }
}
