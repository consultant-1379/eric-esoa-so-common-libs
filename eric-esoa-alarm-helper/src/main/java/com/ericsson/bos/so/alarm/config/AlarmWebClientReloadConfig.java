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
package com.ericsson.bos.so.alarm.config;

import com.ericsson.bos.so.security.mtls.config.ConnectionConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * This class is not needed to be exclusively called by the client.
 * This class will be called by the library internally and keep things ready for
 * its clients
 */
@Configuration
public class AlarmWebClientReloadConfig {

    public static final String ALARM_WEB_CLIENT_QUALIFIER = "ALARM_WEB_CLIENT";

    @Autowired
    private ConnectionConfiguration connectionConfiguration;

    /**
     * Creates WebClient Builder bean with "ALARM_SIP_TLS_WEB_CLIENT" qualifier.
     * @return WebClient
     */
    @Bean
    @Qualifier(ALARM_WEB_CLIENT_QUALIFIER)
    public WebClient.Builder alarmSipTlsWebClient() {
        final HttpClient client = HttpClient.create()
                .responseTimeout(Duration.ofMillis(connectionConfiguration.getReadTimeoutMsec()));
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(client));
    }
}
