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
package com.ericsson.bos.so.alarm.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * The Alarm Entity Class
 */
@Data
public class Alarm {
    private String serviceName;
    private String faultName;
    private String faultyUrlResource;
    private String description;
    private Integer expiration;
    private String specificProblem;
    private Integer vendor;
    private String category;
    private Integer probableCause;
    private String eventTime;
    private String severity;

    @Setter(value = AccessLevel.NONE)
    private Map<String, Object> additionalInformation = new HashMap<>();
}

