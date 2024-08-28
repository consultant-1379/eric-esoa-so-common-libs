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

package com.ericsson.bos.so.common.logging.appender;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;

/**
* Logback delegates the task of writing a logging event to components called appenders.
* Appenders must implement the ch.qos.logback.core.Appender interface (start/stop/append).
* Details here: https://logback.qos.ch/manual/appenders.html
* This appender establishes a TCP connection towards a Logstash target, keeps it
* open and reuses it as much as possible. The raw message is encoded in JSON (by the
* LogstashEncoder) and become the body of the POST request (Content-Type: application/json).
* Logstash must be configured to receive this stream of data via the 'HTTP Input plugin'.
* Details here: https://www.elastic.co/guide/en/logstash/current/plugins-inputs-http.html
*/
public class LogstashHttpAppender extends LogstashHttpAppenderBase {

    /**
     * LogstashHttpAppender
     */
    public LogstashHttpAppender() {
        super("http");
    }

    @Override
    public synchronized void start() {
        if (isStarted()) {
            return;
        }
        /* https://hc.apache.org/httpcomponents-client-4.5.x/current/tutorial/html/connmgmt.html */
        connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(1);
        connManager.setDefaultMaxPerRoute(1);
        addInfo("start");
        super.start();
    }
}
