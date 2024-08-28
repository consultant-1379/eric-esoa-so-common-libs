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

import com.ericsson.bos.so.security.mtls.config.SecurityConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * This class is needed to be exclusively called by the client to
 * load the SSL configurations
 */
@Component
@Slf4j
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${spring.kafka.bootstrap-servers:}')")
public class KafkaSSLConfiguration {
    @Autowired
    private SecurityConfiguration securityConfiguration;

    /**
     * Microservices using Kafka clients (Kafka Producer and Kafka Consumer) on SSL should call this method by passing
     * their existing configurations.This method will update the existing configuration by adding SSL configuration.
     * This method will NOT provide any additional configurations for clients except SSL. So clients are responsible
     * for providing additional configurations (than SSL) like ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
     * ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
     * ,ProducerConfig.ACKS_CONFIG etc (for Kafka Producer) and ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
     * ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG etc (for Kafka Consumer).
     * Example usage in client service:
     * @Autowired
     * KafkaSSLConfiguration kafkaSSLConfiguration;
     * if(securityTlsEnabled) {
     *   return kafkaSSLConfiguration.getSSLConfiguration(props);
     * }
     *
     * @param clientConfigs kafka client existing configurations
     * @return existing kafka client configurations along with SSL configuration
     */
    public Map<String, Object> updateClientConfigWithSSL(final Map<String, Object> clientConfigs) {

        clientConfigs.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, securityConfiguration.getKafkaKeyStoreLocation());
        clientConfigs.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, securityConfiguration.decodePassword(
                securityConfiguration.getKafkaKeyStorePassword()));
        clientConfigs.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, securityConfiguration.getKafkaTrustStoreLocation());
        clientConfigs.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, securityConfiguration.decodePassword(
                securityConfiguration.getKafkaTrustStorePassword()));
        clientConfigs.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, securityConfiguration.getKafkaSecurityProtocol());

        return clientConfigs;
    }
}