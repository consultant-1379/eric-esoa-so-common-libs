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
package com.ericsson.bos.so.common.logging.provider;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import net.logstash.logback.composite.AbstractPatternJsonProvider;
import net.logstash.logback.pattern.AbstractJsonPatternParser;
import net.logstash.logback.pattern.LoggingEventJsonPatternParser;
import org.slf4j.LoggerFactory;

/**
 * ADPSeverityJsonProvider
 */
public final class ADPSeverityJsonProvider extends AbstractPatternJsonProvider<ILoggingEvent> {

    private static final String SEVERITY_FIELD = "severity";

    private static final String DEBUG   = "debug";
    private static final String INFO    = "info";
    private static final String WARNING = "warning";
    private static final String ERROR   = "error";

    @Override
    protected AbstractJsonPatternParser<ILoggingEvent> createParser(final JsonFactory jsonFactory) {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return new LoggingEventJsonPatternParser(context, jsonFactory);
    }

    @Override
    public void writeTo(final JsonGenerator generator, final ILoggingEvent iLoggingEvent) throws IOException {
        switch (iLoggingEvent.getLevel().levelInt) {
            case Level.ALL_INT:
            case Level.TRACE_INT:
            case Level.DEBUG_INT:
                generator.writeStringField(SEVERITY_FIELD, DEBUG);
                break;
            case Level.INFO_INT:
                generator.writeStringField(SEVERITY_FIELD, INFO);
                break;
            case Level.WARN_INT:
                generator.writeStringField(SEVERITY_FIELD, WARNING);
                break;
            case Level.ERROR_INT:
                generator.writeStringField(SEVERITY_FIELD, ERROR);
                break;
            default:
                generator.writeStringField(SEVERITY_FIELD, iLoggingEvent.getLevel().levelStr);
                break;
        }
        super.writeTo(generator, iLoggingEvent);
    }
}
