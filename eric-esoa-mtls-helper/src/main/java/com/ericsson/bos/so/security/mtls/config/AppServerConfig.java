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

import com.ericsson.bos.so.security.mtls.server.TomcatSSLConnectorCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class is not needed to be exclusively called by the client.
 * This class will be called by the library internally and keep things ready for
 * its clients
 */
@Configuration
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
public class AppServerConfig {
    @Autowired
    private TomcatSSLConnectorCustomizer tomcatSSLConnectorCustomizer;

    /**
     * Creates connector for tomcat to listen to https connection.
     * @return ServletWebServerFactory
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        final TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addConnectorCustomizers(tomcatSSLConnectorCustomizer);
        return tomcat;
    }
}
