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

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * This class is not needed to be exclusively called by the client.
 * This class will be called by the library internally and keep things ready for its clients
 * This is a Common Class for all security Configuration
 */
@Component
@Getter
public class SecurityConfiguration {

    public static final String FORWARD_SLASH = "/";

    @Value("${security.keystore.tlsCertDirectory}")
    private String tlsCertDirectory;

    @Value("${security.tls.enabled}")
    private boolean securityTlsEnabled;

    @Value("${security.cryptoStoreParams.keyPass}")
    private String keyPass;

    @Value("${security.cryptoStoreParams.storePass}")
    private String storePass;

    @Value("${security.keystore.path}")
    private String keystorepath;

    @Value("${security.keystore.tlsCertFile}")
    private String keyStoreTlsCertFile;

    @Value("${security.keystore.tlsKeyFile}")
    private String keyStoreTlsKeyFile;

    @Value("${security.cryptoStoreParams.keyAlias}")
    private String keyAlias;

    @Value("${server.ssl.port}")
    private int httpsPort;

    @Value("${security.cryptoStoreParams.keyStoreType}")
    private String keyStoreType;

    @Value("${spring.kafka.security.protocol:SSL}")
    private String kafkaSecurityProtocol;

    @Value("${spring.kafka.ssl.trust-store-location:/tmp/truststore.jks}")
    private String kafkaTrustStoreLocation;

    @Value("${spring.kafka.ssl.trust-store-password:Y2hhbmdlaXQ=}")
    private String kafkaTrustStorePassword;

    @Value("${spring.kafka.ssl.key-store-location:/tmp/keystore.jks}")
    private String kafkaKeyStoreLocation;

    @Value("${spring.kafka.ssl.key-store-password:Y2hhbmdlaXQ=}")
    private String kafkaKeyStorePassword;

    @Autowired(required = false)
    private CaCertSecrets caCertSecrets;


    /**
     * @return getTrustStorePath
     */
    public String getTrustStorePath() {
        return caCertSecrets.getPath();
    }

    /**
     * @return caCertDirectories get ca cert directories
     */
    public List<String> getCaCertDirectories() {
        final List<Certificates> certificates = caCertSecrets.getCertificates();
        final List<String> caCertDirectories = new ArrayList<>();
        for (Certificates certificate : certificates) {
            caCertDirectories.add(caCertSecrets.getCaCertDirectory() + certificate.getSecretName());
        }
        return caCertDirectories;
    }

    /**
     * @return caCertFilesPath appended with the File directory path
     */
    public List<String> getCaCertFiles() {
        final List<Certificates> certificates = caCertSecrets.getCertificates();
        final List<String> caCertFiles = new ArrayList<>();
        for (Certificates certificate : certificates) {
            caCertFiles.add(caCertSecrets.getCaCertDirectory() + certificate.getSecretName() + FORWARD_SLASH + certificate.getFileName());
        }
        return caCertFiles;
    }

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

    /**
     * @return keyPass decoded from base64 encoded string
     */
    public String getKeyPass() {
        return decodePassword(keyPass);
    }

    /**
     * Method to decode the password from base64 encoded string
     *
     * @param pass encoded password
     *
     * @return password, password decoded from base64 encoded string
     */
    public String decodePassword(final String pass) {
        return new String(Base64.getDecoder().decode(pass), StandardCharsets.UTF_8);
    }

}
