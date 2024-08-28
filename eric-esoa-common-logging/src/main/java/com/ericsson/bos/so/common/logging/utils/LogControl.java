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
package com.ericsson.bos.so.common.logging.utils;

import java.util.List;

/**
 * Data class for easy conversion of logcontrol.json to object through ObjectMapper.
 */
public class LogControl {
    public String container;
    public String severity;

    public List customFilters;

    /**
     * LogControl constructor.
     */
    public LogControl() {
    }

    /**
     * LogControl constructor.
     *
     * @param container The container name.
     * @param severity  The severity/log level.
     * @param customFilters -
     */
    public LogControl(final String container, final String severity, final List<String> customFilters) {
        this.container = container;
        this.severity = severity;
        this.customFilters = customFilters;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(final String container) {
        this.container = container;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(final String severity) {
        this.severity = severity;
    }

    public List getCustomFilters() {
        return customFilters;
    }

    public void setCustomFilters(final List customFilters) {
        this.customFilters = customFilters;
    }
}
