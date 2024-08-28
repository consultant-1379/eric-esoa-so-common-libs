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

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * This class is not needed to be exclusively called by the client.
 * This class will be called by the library internally and keep things ready for its clients
 * This is a Common Class for all security Configuration
 */
@Component
@Getter
public class AlarmConfiguration {

    @Value("${security.systemMonitoring.keystore.keyAlias}")
    private String keyAlias;

    @Value("${security.systemMonitoring.keystore.path}")
    private String keystorepath;

    @Value("${security.systemMonitoring.keystore.storePass}")
    private String storePass;

    @Value("${security.systemMonitoring.keystore.tlsCertDirectory}")
    private String tlsCertDirectory;

    @Value("${security.systemMonitoring.keystore.tlsCertFile}")
    private String keyStoreTlsCertFile;

    @Value("${security.systemMonitoring.keystore.tlsKeyFile}")
    private String keyStoreTlsKeyFile;

    @Value("${security.systemMonitoring.kubernetesNamespace}")
    private String kubernetesNamespace;

    @Value("${security.systemMonitoring.expiration:600}")
    private String alarmExpiration;

    @Value("${security.systemMonitoring.faultManagement.address:eric-fh-alarm-handler}")
    private String alarmAddress;

    @Value("${security.systemMonitoring.faultManagement.apiPath:/alarm-handler/v1/fault-indications}")
    private String alarmPath;

    @Value("${security.systemMonitoring.faultManagement.port:6006}")
    private String alarmPort;

    @Value("${security.systemMonitoring.faultManagement.scheme:https}")
    private String alarmProtocol;

    @Value("${spring.application.name}")
    private String client;

    /**
     * @return keyStoreTlsCertFile appended with the File directory path
     */
    public String getKeyStoreTlsCertFile() {
        return tlsCertDirectory + keyStoreTlsCertFile;
    }

    /**
     * @return keyStoreTlsKeyFile appended with the File directory path
     */
    public String getKeyStoreTlsKeyFile() {
        return tlsCertDirectory + keyStoreTlsKeyFile;
    }

    /**
     * @return storePass decoded from base64 encoded string
     */
    public String getStorePass() {
        return decodePassword(storePass);
    }

    private String decodePassword(final String pass) {
        return new String(Base64.getDecoder().decode(pass), StandardCharsets.UTF_8);
    }

}
