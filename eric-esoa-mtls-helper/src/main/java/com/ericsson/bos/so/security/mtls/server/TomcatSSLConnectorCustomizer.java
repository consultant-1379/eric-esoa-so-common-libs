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
package com.ericsson.bos.so.security.mtls.server;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.stereotype.Component;

/**
 * This Class that helps in exposing Http11NioProtocol. The protocol is used in
 * reloading the SSL Config when host certificates are renewed.
 *
 */
@Component
@ConditionalOnProperty(value = "security.tls.enabled", havingValue = "true")
public class TomcatSSLConnectorCustomizer implements TomcatConnectorCustomizer {

    private Http11NioProtocol protocol;

    /**
     * method of TomcatConnectorCustomizer interface used to initialize the  Http11NioProtocol
     *
     * @Param Connector
     */
    @Override
    public void customize(final Connector connector) {

        final Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        if (connector.getSecure()) {
            this.protocol = protocol;
        }
    }

    protected Http11NioProtocol getProtocol() {
        return protocol;
    }

}
