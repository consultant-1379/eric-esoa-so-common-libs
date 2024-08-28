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

import com.ericsson.bos.so.alarm.client.AlarmTlsWebClientReloadClient;
import com.ericsson.bos.so.security.mtls.MtlsConfigurationReloadersRegister;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


/**
 * This class is used to initialize the registration of client/services required Mtls configuration at
 * application startup time
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
public class AlarmMtlsConfigurationReloadStartupConfig {

    @Autowired
    private AlarmTlsWebClientReloadClient alarmTlsWebClientReloadClient;

    /**
     * This method is used to initialize the registration of client/services required Mtls configuration.
     */
    @PostConstruct
    public void init() {
        log.info("Mtls Alarm register services configuration started.");
        MtlsConfigurationReloadersRegister.getInstance().register(alarmTlsWebClientReloadClient);
        log.info("Registration of {} services/clients successful.", MtlsConfigurationReloadersRegister.getInstance().
                getMtlsConfigurationReloaders().size());
    }
}
