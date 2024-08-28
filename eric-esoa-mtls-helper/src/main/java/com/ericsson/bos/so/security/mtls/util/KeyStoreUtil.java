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
package com.ericsson.bos.so.security.mtls.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import jakarta.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class is not needed to be exclusively called by the client.
 * This class will be called by the library internally and keep things ready for its clients
 */
@Component
public class KeyStoreUtil {
    private static final String X509_INSTANCE = "X.509";
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyStoreUtil.class);

    /**
     * Create a truststore from standard Certificate(s) files
     *
     * @param caCertFiles     Path of ca cert files
     * @param keyStorePass   keystore password
     * @param trustStorePath truststorePath
     * @return KeyStore instance is returned
     * @throws Exception is thrown
     */
    @Retryable(maxAttemptsExpression = "#{${security.renewCertRetry.times}}",
            backoff = @Backoff(delayExpression = "#{${security.renewCertRetry.delay}}"), include = CertificateException.class)
    public KeyStore createTrustStoreFromCACerts(final List<String> caCertFiles, final String keyStorePass,
                                                final String trustStorePath)
            throws Exception {
        LOGGER.info("Enter createTrustStoreFromCACerts");
        final List<X509Certificate> certs = new ArrayList<>();
        for(String caCertFile: caCertFiles) {
            final byte[] certBytes = parseCertificate(caCertFile);
            final CertificateFactory cf = CertificateFactory.getInstance(X509_INSTANCE);
            certs.add((X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes)));
        }
        return createTrustStore(certs, keyStorePass, trustStorePath);
    }



    /**
     * Create a truststore from standard Certificate(s) files
     *
     * @param certPath     Path of cert file
     * @param keyPath      Path of key file
     * @param alias        keystore alias
     * @param keyStorePass keystore password
     * @param keyStorePath keystore path
     * @return KeyStore instance
     * @throws Exception is thrown
     */
    @Retryable(maxAttemptsExpression = "#{${security.renewCertRetry.times}}",
            backoff = @Backoff(delayExpression = "#{${security.renewCertRetry.delay}}"), include = CertificateException.class)
    public KeyStore createKeyStoreFromCertAndPK(String certPath, String keyPath, final String alias,
                                                final String keyStorePass, final String keyStorePath) throws Exception {
        LOGGER.debug("Entered createKeyStoreFromCertAndPK");

        final File privateKeyPem = Paths.get(keyPath).toFile();
        final File certificatePem = Paths.get(certPath).toFile();

        LOGGER.debug("PK Path : {} , certificate Path : {}", privateKeyPem.getAbsolutePath(),
                certificatePem.getAbsolutePath());

        final X509Certificate[] cert = createCertificates(certificatePem);
        final KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null);
        // Import private key
        final PrivateKey key = createPrivateKey(privateKeyPem);
        keystore.setKeyEntry(alias, key, keyStorePass.toCharArray(), cert);
        saveKeyStore(keystore, keyStorePass, keyStorePath);
        LOGGER.debug("Exited createKeyStoreFromCertAndPK");
        LOGGER.info("KeyStore created Successfully ");
        return keystore;
    }

    private KeyStore createTrustStore(final List<X509Certificate> certs, final String keyStorePass,
                                      final String keyStorePath)
            throws CertificateException {
        LOGGER.debug("Enter createTrustStore");
        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            for (X509Certificate x509Certificate : certs) {
                trustStore.setCertificateEntry(String.valueOf(UUID.randomUUID()), x509Certificate);
            }
            saveKeyStore(trustStore, keyStorePass, keyStorePath);
            LOGGER.info("Truststore created Successfully ");
        } catch (Exception exception) {
            LOGGER.error("Failed to create truststore due to exception {}, Retrying Again", exception.getMessage());
            throw new CertificateException(exception);
        }
        return trustStore;
    }

    private byte[] parseCertificate(final String tlsCertPath) throws Exception {
        final byte[] certBytes;
        try (InputStream inputStream = java.nio.file.Files.newInputStream(Paths.get(tlsCertPath))) {
            certBytes = IOUtils.toByteArray(inputStream);
        } catch (Exception exception) {
            LOGGER.error("Failed to parse the certificate :: {}", exception.getMessage());
            throw new CertificateException(exception);
        }
        return certBytes;
    }

    private void saveKeyStore(final KeyStore keyStore, final String keyStorePass,
                              final String keyStorePath) throws Exception {
        LOGGER.debug("Enter saveStore");
        try (OutputStream outputStream = java.nio.file.Files.newOutputStream(Paths.get(keyStorePath))) {
            keyStore.store(outputStream, keyStorePass.toCharArray());
        } catch (Exception exception) {
            LOGGER.error("Failed to update Keystore due to exception :: {} ", exception.getMessage());
            throw new CertificateException(exception);
        }
        LOGGER.debug("Exit saveStore");
    }

    private static PrivateKey createPrivateKey(File privateKeyPem) throws Exception {
        byte[] bytes = null;
        try (BufferedReader r = new BufferedReader(new FileReader(privateKeyPem))) {
            String s = r.readLine();
            if (s == null || !s.contains("BEGIN PRIVATE KEY")) {
                throw new IllegalArgumentException("No PRIVATE KEY found");
            }
            final StringBuilder b = new StringBuilder();
            s = "";
            while (s != null) {
                if (s.contains("END PRIVATE KEY")) {
                    break;
                }
                b.append(s);
                s = r.readLine();
            }
            final String hexString = b.toString();
            bytes = DatatypeConverter.parseBase64Binary(hexString);
        } catch (FileNotFoundException f) {
            LOGGER.error(privateKeyPem + " does not exist, FileNotFoundException received ::{}", f.getMessage());
        } catch (IOException e) {
            LOGGER.error(privateKeyPem + " does not exist, IOException received::{}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Illegal Argument found while parsing certificate Exception received :: {}", e.getMessage());
        }
        return generatePrivateKeyFromDER(bytes);

    }

    private static X509Certificate[] createCertificates(File certificatePem) throws Exception {
        final List<X509Certificate> result = new ArrayList<X509Certificate>();
        try (BufferedReader r = new BufferedReader(new FileReader(certificatePem))) {
            String s = r.readLine();
            if (s == null || !s.contains("BEGIN CERTIFICATE")) {
                throw new IllegalArgumentException("No CERTIFICATE found");
            }
            StringBuilder b = new StringBuilder();
            while (s != null) {
                if (s.contains("END CERTIFICATE")) {
                    final String hexString = b.toString();
                    final byte[] bytes = DatatypeConverter.parseBase64Binary(hexString);
                    final X509Certificate cert = generateCertificateFromDER(bytes);
                    result.add(cert);
                    b = new StringBuilder();
                } else {
                    if (!s.startsWith("----")) {
                        b.append(s);
                    }
                }
                s = r.readLine();
            }
        } catch (FileNotFoundException f) {
            LOGGER.error(certificatePem + " file not found , Exception received::{}", f.getMessage());
        } catch (IOException e) {
            LOGGER.error(certificatePem + " IO Exception received :: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Illegal Argument found while parsing certificate Exception received :: {}", e.getMessage());
        }

        return result.toArray(new X509Certificate[result.size()]);
    }

    private static ECPrivateKey generatePrivateKeyFromDER(byte[] keyBytes)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        final KeyFactory factory = KeyFactory.getInstance("EC");
        return (ECPrivateKey) factory.generatePrivate(spec);
    }

    private static X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {
        final CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }

}
