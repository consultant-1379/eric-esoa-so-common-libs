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
package com.ericsson.bos.so.security.mtls.config;



import com.ericsson.bos.so.security.mtls.client.rest.TlsRestTemplateReloadClient;
import com.ericsson.bos.so.security.mtls.client.webclient.TlsWebClientReloadClient;
import com.ericsson.bos.so.security.mtls.client.webclient.WebClientReload;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import com.ericsson.bos.so.security.mtls.MtlsConfigurationReloadersRegister;
import com.ericsson.bos.so.security.mtls.client.kafka.KafkaClientReloadConfig;
import com.ericsson.bos.so.security.mtls.client.rest.RestTemplateReloadClient;
import com.ericsson.bos.so.security.mtls.server.TomcatSSLReloadConfig;

/**
 * This class is used to initialize the registration of client/services required Mtls configuration at
 * application startup time
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
public class MtlsConfigurationReloadStartupConfig {

    @Autowired
    private TomcatSSLReloadConfig tomcatSSLReloadConfig;

    @Autowired
    private RestTemplateReloadClient restTemplateReloadClient;

    @Autowired
    private WebClientReload webClientReload;

    @Autowired(required=false)
    private KafkaClientReloadConfig kafkaClientReloadConfig;

    @Autowired
    private TlsRestTemplateReloadClient tlsRestTemplateReloadClient;

    @Autowired
    private TlsWebClientReloadClient tlsWebClientReloadClient;

    /**
     * This method is used to initialize the registration of client/services required Mtls configuration.
     */
    @PostConstruct
    public void init() {
        log.info("Mtls register services configuration started.");
        MtlsConfigurationReloadersRegister.getInstance().register(tomcatSSLReloadConfig);
        MtlsConfigurationReloadersRegister.getInstance().register(restTemplateReloadClient);
        MtlsConfigurationReloadersRegister.getInstance().register(webClientReload);
        if(kafkaClientReloadConfig != null) {
            log.info("Register KafkaClientReloadConfiguration class");
            MtlsConfigurationReloadersRegister.getInstance().register(kafkaClientReloadConfig);
        }
        MtlsConfigurationReloadersRegister.getInstance().register(tlsRestTemplateReloadClient);
        MtlsConfigurationReloadersRegister.getInstance().register(tlsWebClientReloadClient);
        log.info("Registration of {} services/clients successfully.", MtlsConfigurationReloadersRegister.getInstance().
                getMtlsConfigurationReloaders().size());
    }

}
