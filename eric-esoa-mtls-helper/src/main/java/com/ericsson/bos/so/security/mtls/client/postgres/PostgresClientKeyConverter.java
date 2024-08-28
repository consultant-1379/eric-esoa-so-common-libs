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
package com.ericsson.bos.so.security.mtls.client.postgres;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert the postgres client key file from PEM to PKCS8 format required by the postgresql driver.
 */
class PostgresClientKeyConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresClientKeyConverter.class);

    private PostgresClientKeyConverter() {
    }

    /**
     * Convert the postgres client key file from PEM to PKCS8 format.
     * The converted file is stored in directory '${java.io.tmpdir}/pg-client-key' with the same filename
     * as the original file.
     *
     * @param clientKeyFile path to the postgres client key file
     * @return the path to the converted pkcs8 key file
     * @throws IOException         if error read or writing the key file
     */
    static String convertClientKeyFromPemToPkcs8(final String clientKeyFile) throws IOException {
        final Path clientKeyPath = Paths.get(clientKeyFile);
        LOGGER.info("Convert postgres client key to pkcs8 format: {}", clientKeyPath);
        final String clientKeyFileContents = new String(Files.readAllBytes(clientKeyPath));
        final byte[] privateKeyPkcs8 = Base64.getDecoder().decode(getPrivateKeyFromPEM(clientKeyFileContents));
        final File pkcs8File = Paths.get(System.getProperty("java.io.tmpdir"), "pg-client-key", clientKeyPath.toFile().getName()).toFile();
        if (!pkcs8File.getParentFile().exists()) {
            pkcs8File.getParentFile().mkdirs();
        }
        Files.write(pkcs8File.toPath(), privateKeyPkcs8);
        LOGGER.info("Converted postgres client key to pkcs8 format: {}", pkcs8File.getPath());
        return pkcs8File.getPath();
    }

    private static String getPrivateKeyFromPEM(final String clientKeyFileContents) {
        return clientKeyFileContents
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("\n", "")
                .replace("\r", "")
                .replace("-----END PRIVATE KEY-----", "");
    }
}