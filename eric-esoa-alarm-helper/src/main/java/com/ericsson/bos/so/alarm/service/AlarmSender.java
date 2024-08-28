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
package com.ericsson.bos.so.alarm.service;

import com.ericsson.bos.so.alarm.config.AlarmConfiguration;
import com.ericsson.bos.so.alarm.config.AlarmWebClientReloadConfig;
import com.ericsson.bos.so.alarm.exception.AlarmRetryException;
import com.ericsson.bos.so.alarm.model.Alarm;
import com.ericsson.bos.so.alarm.util.AlarmUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;
import java.util.Optional;

/**
 * This class will be called by the clients to post an Alarm.
 */
@Slf4j
@Service
public class AlarmSender {

    private static final String POST_ALARM_CONNECTION_FAILED_ERROR =
            "Connection failure when sending alarm to eric-fh-alarm-handler alarm server";
    @Autowired
    @Qualifier(AlarmWebClientReloadConfig.ALARM_WEB_CLIENT_QUALIFIER)
    private WebClient.Builder alarmSipTlsWebClient;

    @Autowired
    private AlarmConfiguration alarmConfiguration;

    @Autowired
    private AlarmUtil alarmUtil;

    /**
     * This method is called by the clients to post an alarm in case of failure of secure communication with the server.
     *
     * @param uri the URL which was used for secure communication by the client
     */
    @Retryable(
            maxAttemptsExpression = "#{${security.systemMonitoring.faultManagement.retry}}",
            backoff = @Backoff(delayExpression = "#{${security.systemMonitoring.faultManagement.delay}}"),
            retryFor = AlarmRetryException.class)
    public void postAlarm(final String uri) {
        final Alarm alarmObject = alarmUtil.createAlarmData(uri);
        final String requestObj = AlarmUtil.buildAlarmRequestBody(alarmObject);
        try {
            if (Objects.nonNull(requestObj)) {
                log.info("Posting Alarm for secure communication failure betweeen client: {}  and server: {} ",
                        alarmConfiguration.getClient(), AlarmUtil.resolveHostNameFromURI(uri));
                postForObject(buildAlarmUrl(), requestObj, String.class);
                log.info("Posted Alarm: {}", alarmUtil.createAlarmData(uri));
            }
        } catch (final WebClientResponseException webClientResponseException) {
            if (isHttpTimeout(webClientResponseException.getStatusCode())) {
                log.error("Encountered Timeout Exception while trying to post alarm again");
                throw new AlarmRetryException(POST_ALARM_CONNECTION_FAILED_ERROR, webClientResponseException);
            } else {
                log.warn("Failed to post alarm!, reason: {}", webClientResponseException.getMessage(), webClientResponseException);
            }

        } catch (final WebClientRequestException webClientRequestException) {
            if (AlarmUtil.isRetryException(webClientRequestException)) {
                log.error("Encountered WebClientRequest Exception while trying to post alarm again");
                throw new AlarmRetryException(POST_ALARM_CONNECTION_FAILED_ERROR, webClientRequestException);
            } else {
                log.warn("Failed to post alarm!, reason: {}", webClientRequestException.getMessage(), webClientRequestException);
            }
        }
    }

    /**
     * This method is called by the clients to post a custom alarm with secure communication.
     *
     * @param alarm the alarm object
     */
    @Retryable(
            maxAttemptsExpression = "#{${security.systemMonitoring.faultManagement.retry}}",
            backoff = @Backoff(delayExpression = "#{${security.systemMonitoring.faultManagement.delay}}"),
            retryFor = AlarmRetryException.class)
    public void postAlarm(final Alarm alarm, final String threadName){
        final String requestObj = AlarmUtil.buildAlarmRequestBody(alarm);
        try {
            if (Objects.nonNull(requestObj)) {
                postForObject(buildAlarmUrl(), requestObj, String.class);
                log.info("Posted Alarm: {}", alarm);
            }
        } catch (final WebClientResponseException webClientResponseException) {
            if (isHttpTimeout(webClientResponseException.getStatusCode())) {
                log.error(
                        "Encountered Timeout Exception for thread : {}, trying to post alarm again",
                        threadName);
                throw new AlarmRetryException(POST_ALARM_CONNECTION_FAILED_ERROR, webClientResponseException);
            } else {
                log.warn("Failed to post alarm for thread :{}", threadName, webClientResponseException);
            }

        } catch (final WebClientRequestException webClientRequestException) {
            if (AlarmUtil.isRetryException(webClientRequestException)) {
                log.error(
                        "Encountered WebClientRequest Exception for thread : {}, trying to post alarm again",
                        threadName);
                throw new AlarmRetryException(POST_ALARM_CONNECTION_FAILED_ERROR, webClientRequestException);
            } else {
                log.warn("Failed to post alarm for thread :{}", threadName, webClientRequestException);
            }
        }
    }

    private String buildAlarmUrl() {
        return UriComponentsBuilder.newInstance()
                .scheme(alarmConfiguration.getAlarmProtocol())
                .port(alarmConfiguration.getAlarmPort())
                .host(alarmConfiguration.getAlarmAddress())
                .path(alarmConfiguration.getAlarmPath())
                .build()
                .toString();
    }

    private <T, R> Optional<T> postForObject(
            final String uri, final R request, final Class<T> responseType) {
        log.info("Processing Alarm request -> requestUrl: '{}', requestBody: '{}'", uri, request);

        final T response = alarmSipTlsWebClient.build()
                .post()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(responseType)
                .block();
        log.debug("Alarm request processed successfully");
        return Optional.ofNullable(response);
    }

    private boolean isHttpTimeout(final HttpStatusCode httpStatusCode) {
        return httpStatusCode == HttpStatus.REQUEST_TIMEOUT
                || httpStatusCode == HttpStatus.SERVICE_UNAVAILABLE
                || httpStatusCode == HttpStatus.GATEWAY_TIMEOUT;
    }
}

