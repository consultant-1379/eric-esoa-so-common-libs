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
package com.ericsson.bos.so.security.mtls.client.kafka;

import com.ericsson.bos.so.security.mtls.MtlsConfigurationReloader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * This class is not needed to be exclusively called by the client.
 * This class will be called by the library internally and keep things ready for
 * its clients to reload the producer and consumer configs based on certificate change
 */
@Component
@Slf4j
@ConditionalOnExpression("${security.tls.enabled} == true and !T(org.springframework.util.StringUtils).isEmpty('${spring.kafka.bootstrap-servers:}')")
public class KafkaClientReloadConfig implements MtlsConfigurationReloader {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    /**
     *
     * reload ProducerConfig and consumerConfig when ever there is a change in the certificate
     */
    @Override
    public void reload() {
        reloadProducerConfig();
        reloadConsumerConfig();
    }

    /**
     *
     * reloadProducerConfig when ever there is a change in the certificate
     */
    private void reloadProducerConfig() {
        log.info("KafkaTemplate ProducerFactory reset is invoked.");
        kafkaTemplate.getProducerFactory().reset();
        log.info("KafkaTemplate ProducerFactory reset was successful.");
    }

    /**
     *
     * reloadConsumerConfig when ever there is a change in the certificate
     */
    private void reloadConsumerConfig() {
        try {
            log.info("KafkaListenerEndpointRegistry is invoked to reload consumer configuration.");
            kafkaListenerEndpointRegistry.stop();
            kafkaListenerEndpointRegistry.start();
            log.info("KafkaListenerEndpointRegistry reloading of consumer configuration was successful.");
        } catch (Exception exception) {
            log.error("Failed in reload Consumer Config messageListenerContainer: {} ", exception.getMessage());
        }
    }
}
