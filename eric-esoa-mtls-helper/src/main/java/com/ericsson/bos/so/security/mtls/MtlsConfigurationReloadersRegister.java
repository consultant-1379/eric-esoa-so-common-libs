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
package com.ericsson.bos.so.security.mtls;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * This class is used to register/get the reloaders like TomcatSSLReloadConfig, RestTemplateReloadClient ,
 * WebClientReload and KafkaClientReloadConfig instances and provide the singleton class object of
 * MtlsReloadConfigRegister.
 */
@Component
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
public class MtlsConfigurationReloadersRegister {

    private static MtlsConfigurationReloadersRegister instance;
    private List<MtlsConfigurationReloader> mtlsConfigurationReloaders = new ArrayList<MtlsConfigurationReloader>();

    /**
     * constructor
     */
    private MtlsConfigurationReloadersRegister(){}

    /**
     * This synchronized block will provide the instance of MtlsConfigurationReloadersRegister
     * @return instance
     */
    public static synchronized MtlsConfigurationReloadersRegister getInstance() {
        if (instance == null) {
            instance = new MtlsConfigurationReloadersRegister();
        }
        return instance;
    }

    /**
     * This method is used to register the MtlsConfigurationReloader
     * @param mtlsConfigurationReloader the mtlsConfigurationReloader
     */
    public synchronized void register(MtlsConfigurationReloader mtlsConfigurationReloader) {
        mtlsConfigurationReloaders.add(mtlsConfigurationReloader);
    }

    /**
     * This method is used to get all the mtlsConfigurationReloaders
     * @return mtlsConfigurationReloaders
     */
    public List<MtlsConfigurationReloader> getMtlsConfigurationReloaders() {
        return mtlsConfigurationReloaders;
    }

}
