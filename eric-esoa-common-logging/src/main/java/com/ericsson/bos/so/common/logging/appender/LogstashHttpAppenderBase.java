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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.ericsson.bos.so.common.logging.utils.LogControlWatcher;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

/**
* LogstashHttpAppender base class
*/
public abstract class LogstashHttpAppenderBase extends AppenderBase<ILoggingEvent> {
    protected PoolingHttpClientConnectionManager connManager;
    protected LoggingEventCompositeJsonEncoder encoder;
    protected String destination;
    protected long reconnectionDelay = 10;
    protected long connectionTimeout = 5;
    protected boolean needReload;
    private final String scheme;

    /**
     * Class constructor.
     *
     * @param scheme url scheme
     */
    public LogstashHttpAppenderBase(final String scheme) {
        this.scheme = scheme;
    }

    @Override
    public synchronized void stop() {
        super.stop();
        if (connManager != null) {
            connManager.close();
            connManager = null;
        }
        addInfo("stop");
    }

    @Override
    public void append(final ILoggingEvent event) {
        while (true) {
            if (!isStarted() || Thread.currentThread().isInterrupted()) {
                addWarn("shutdown in progress");
                break;
            }
            if (httpAppend(event)) {
                return;
            } else {
                addWarn("append failed, event timestamp " + event.getTimeStamp());
            }
            if (needReload) {
                this.stop();
                this.start();
            }
            pause();
        }
    }

    private Boolean httpAppend(final ILoggingEvent event) {
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .setConnectionRequestTimeout(connectionTimeout, TimeUnit.SECONDS)
                .build();
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connManager)
                .setConnectionManagerShared(true)
                .disableAutomaticRetries()
                .build()) {
            final String jsonBody = new String(encoder.encode(event), StandardCharsets.UTF_8);
            final HttpEntity stringEntity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
            final HttpPost httpPost = new HttpPost(scheme.concat("://").concat(destination));
            httpPost.setEntity(stringEntity);
            try (CloseableHttpResponse response = client.execute(httpPost)) {
                EntityUtils.consume(response.getEntity());
                final int responseCode = response.getCode();;
                return responseCode >= 200 && responseCode <= 299;
            } catch (Exception e) {
                addToLog("response error: ", e);
                if (e instanceof javax.net.ssl.SSLException) {
                    // Many things can go wrong about SSL/TLS. This flag signals the appender to stop/start
                    // itself. This means the SSLContext will be rebuilt from scratch and whatever is in the
                    // keyStore/trustStore reloaded. If the keyStore/trustStore are kept up-to-date then certificate
                    // renewal will work fine.
                    needReload = true;
                }
            }
        } catch (Exception e) {
            addToLog("client error: ", e);
            needReload = true;
        }
        return false;
    }

    private void pause() {
        try {
            TimeUnit.SECONDS.sleep(reconnectionDelay);
        } catch (Exception e) {
            addToLog("error: ", e);
        }
    }

    private void addToLog(final String msg, final Exception e) {
        final Level currentLogLevel = LogControlWatcher.getCurrentSeverityLevel();
        if (currentLogLevel.levelInt != Level.INFO.levelInt &&
            currentLogLevel.levelInt != Level.OFF.levelInt) {
            addWarn(msg, e);
        } else {
            addWarn(String.format("%s%s", msg, e.toString()));
        }
    }

    public void setEncoder(final LoggingEventCompositeJsonEncoder encoder) {
        this.encoder = encoder;
    }

    public void setDestination(final String destination) {
        this.destination = destination;
    }

    public void setConnectionTimeout(final long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setReconnectionDelay(final long reconnectionDelay) {
        this.reconnectionDelay = reconnectionDelay;
    }
}
