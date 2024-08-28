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
package com.ericsson.bos.so.alarm.util;

import com.ericsson.bos.so.alarm.config.AlarmConfiguration;
import com.ericsson.bos.so.alarm.model.Alarm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.net.*;
import java.rmi.ConnectIOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * This class is used to build Alarm data.
 */
@Slf4j
@Component
public class AlarmUtil {

    private static final String ALARM_NAME = "connectionEstablishmentError";
    private static final String SERVICE = "eric-esoa-so";
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.ssssss'Z'";
    private static final String SERVICE_NAME = "serviceName";
    private static final String FAULT_NAME = "faultName";
    private static final String FAULTY_RESOURCE = "faultyResource";
    private static final String DESCRIPTION = "description";
    private static final String EXPIRATION = "expiration";
    private static final String CREATED_AT = "createdAt";
    private static final String DEPLOYMENT_NAMESPACE = "/deploymentNamespace";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String EQUALS = "=";
    private static final String SPECIFIC_PROBLEM = "Connection could not be established";
    private static final String CATEGORY = "CommunicationsAlarm";
    private static final String SEVERITY = "Major";

    @Autowired
    private AlarmConfiguration alarmConfiguration;

    /**
     * This method is used to build Alarm request body.
     *
     * @param alarm alarm entity
     * @return alarm request body in string format
     */
    public static String buildAlarmRequestBody(final Alarm alarm) {
        final Map<String, Object> alarmMap = buildAlarmBody(alarm);
        try {
            return new ObjectMapper().writeValueAsString(alarmMap);
        } catch (JsonProcessingException exception) {
            log.error("Error while parsing the Alarm request", exception);
            return null;
        }
    }

    /**
     * The below method is used to get the host name of the server from the uri.
     *
     * @param uri the url through which the client is communicating with the server
     * @return the server name
     */
    public static String resolveHostNameFromURI(String uri) {
        final URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException malformedURLException) {
            throw new RuntimeException(malformedURLException);
        }
        return url.getHost();
    }

    /**
     * The below method is used to create Alarm to post when SIP/TLS communication has failed.
     *
     * @param uri the url through which the client is communicating with the server
     * @return the Alarm entity
     */
    public Alarm createAlarmData(String uri) {
        final String server = resolveHostNameFromURI(uri);
        final String description = buildDescription(alarmConfiguration.getClient(), server);
        final String faultyResource = generateFaultyResource(alarmConfiguration.getClient(), server);
        final int expiration = Integer.parseInt(alarmConfiguration.getAlarmExpiration());
        final Alarm alarm = new Alarm();
        alarm.setEventTime(getCurrentTimeStamp());
        alarm.setServiceName(SERVICE);
        alarm.setExpiration(expiration);
        alarm.setSpecificProblem(SPECIFIC_PROBLEM);
        alarm.setCategory(CATEGORY);
        alarm.setDescription(description);
        alarm.setProbableCause(22);
        alarm.setVendor(356);
        alarm.setFaultName(ALARM_NAME);
        alarm.setSeverity(SEVERITY);
        alarm.setFaultyUrlResource(faultyResource);
        return alarm;
    }

    /**
     * Gets current time stamp.
     *
     * @return the current time stamp
     */
    public static String getCurrentTimeStamp() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
        final Date now = new Date();
        return dateFormat.format(now);
    }

    /**
     * Is retry exception boolean.
     *
     * @param webClientRequestException
     *         the webClientRequestException
     * @return the boolean
     */
    public static boolean isRetryException(final WebClientRequestException webClientRequestException) {
        boolean isRetryException = false;

        if (webClientRequestException.getCause() instanceof TimeoutException
                || webClientRequestException.getCause() instanceof ConnectException
                || webClientRequestException.getCause() instanceof ConnectIOException) {
            isRetryException = true;
        }
        if (webClientRequestException.getCause() instanceof SocketTimeoutException
                || webClientRequestException.getCause() instanceof UnknownHostException) {
            isRetryException = true;
        }
        return isRetryException;
    }

    private String buildDescription(String client, String server) {
        return "An error occurred during the establishment of connection between " +
                client + " and " + server;
    }

    private String generateFaultyResource(
            final String client,
            final String server) {
        final Map<String, String> params = new LinkedHashMap<>();
        params.put(SOURCE, client);
        params.put(TARGET, server);
        return String.join(EQUALS, DEPLOYMENT_NAMESPACE, alarmConfiguration.getKubernetesNamespace())
                .concat("/" + ALARM_NAME)
                .concat(params
                        .entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + EQUALS + entry.getValue())
                        .collect(Collectors.joining("][", "[", "]")));
    }

    private static Map<String, Object> buildAlarmBody(final Alarm alarm) {
        final Map<String, Object> alarmMap = new HashMap<>();
        alarmMap.put(SERVICE_NAME, alarm.getServiceName());
        alarmMap.put(FAULT_NAME, alarm.getFaultName());
        alarmMap.put(FAULTY_RESOURCE, alarm.getFaultyUrlResource());
        alarmMap.put(DESCRIPTION, alarm.getDescription());
        alarmMap.put(CREATED_AT, alarm.getEventTime());
        alarmMap.put(EXPIRATION, alarm.getExpiration());
        return alarmMap;
    }
}

