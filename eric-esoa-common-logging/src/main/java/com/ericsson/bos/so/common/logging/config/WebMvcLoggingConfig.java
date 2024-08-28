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
package com.ericsson.bos.so.common.logging.config;

import com.ericsson.bos.so.common.logging.security.extractor.JwtUserNameExtractor;
import com.ericsson.bos.so.common.logging.security.extractor.SecurityContextUserNameExtractor;
import com.ericsson.bos.so.common.logging.security.extractor.UserNameExtractor;
import com.ericsson.bos.so.common.logging.security.strategy.JwtUserNameExtractorStrategy;
import com.ericsson.bos.so.common.logging.security.strategy.SecurityContextAndJwtUserNameExtractorStrategy;
import com.ericsson.bos.so.common.logging.security.strategy.UserNameExtractStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * WebMvcLoggingConfig
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebMvcLoggingConfig {

    /**
     * userNameExtractorFromJwt
     *
     * @param objectMapper -
     * @param environment -
     * @return UserNameExtractor
     */
    @Bean
    public UserNameExtractor userNameExtractorFromJwt(final ObjectMapper objectMapper, final Environment environment) {
        return new JwtUserNameExtractor(objectMapper, environment);
    }

    /**
     * userNameExtractorFromSecurityContext
     *
     * @return UserNameExtractor
     */
    @Bean
    @ConditionalOnBean(type = "org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder")
    public UserNameExtractor userNameExtractorFromSecurityContext() {
        return new SecurityContextUserNameExtractor();
    }

    /**
     * securityContextAndJwtUserNameExtractorStrategy
     *
     * @param objectMapper -
     * @param environment -
     * @return UserNameExtractStrategy
     */
    @Bean
    @ConditionalOnBean(type = "org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder")
    public UserNameExtractStrategy securityContextAndJwtUserNameExtractorStrategy(final ObjectMapper objectMapper, final Environment environment) {
        //TODO(eankinn) find a better way to set up a strategy and define extractors.
        // Probably in the library there is no need to take data from SecurityContext
        return new SecurityContextAndJwtUserNameExtractorStrategy(
                userNameExtractorFromSecurityContext(), userNameExtractorFromJwt(objectMapper, environment));
    }

    /**
     * jwtUserNameExtractorStrategy
     *
     * @param objectMapper -
     * @param environment -
     * @return UserNameExtractStrategy
     */
    @Bean
    @ConditionalOnMissingBean(UserNameExtractStrategy.class)
    public UserNameExtractStrategy jwtUserNameExtractorStrategy(final ObjectMapper objectMapper, final Environment environment) {
        return new JwtUserNameExtractorStrategy(userNameExtractorFromJwt(objectMapper, environment));
    }
}
