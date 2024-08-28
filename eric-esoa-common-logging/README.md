# ESOA SO Common Logging Library

## Description
**Common ESOA SO Logging Library**, implemented by Ericsson, is based on [Logback library](http://logback.qos.ch/) and  [Logstash Logback Encoder](https://github.com/logstash/logstash-logback-encoder) stack.

The library has been developed for simplifying the following JSON ADP format version 1.0.0. It adds  log correlation id and user name to logs, for tracing Spring Boot-based microservices.

In order to get an idea of where the ESOA SO common logging library comes into the Logging Stack, it is used by the Log Producer.

The Log Producer is any microservice (i.e. pods) that produces logs.

The non ADP microservices (i.e. product specific microservices) will import the ESOA SO common logging library and use this library to standardize their logs so the logs can be easily processed by Log Shipper and the rest of the ADP logging stack. 

## Contributing to ESOA SO Common Logging Library
This document describes how to contribute artifacts for the **ESOA SO Common Logging Library**

## Gerrit Project Details
**ESOA SO Common Logging Library** artifacts are stored in the following Gerrit Project: [ESOA/ESOA-Parent/com.ericsson.bos.so/eric-esoa-so-common-libs](https://gerrit.ericsson.se/#/projects/ESOA/ESOA-Parent/com.ericsson.bos.so/eric-esoa-so-common-libs)
## Artifacts

## Contribution Workflow
1. The **contributor** updates the artifact in the local repository and documentation if necessary.
1. The **contributor** pushes the update to Gerrit for review.
1. The **contributor** invites the **vortex (group)** (mandatory) and **other relevant parties** (optional) to the Gerrit review, and makes no further changes to the document until it is reviewed.

## Documentation
- **[Developer Documentation]** : https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/ESO/ESOA+SO+Common+Logging+Library)

## ```SecurityLogger``` class

Security events must be logged to a security log. To mark a log message in the JSON ADP format as a security event log, the "```facility```" field must be set to the value "```log audit```".
The [```SecurityLogger``` utility class](./src/main/java/com/ericsson/bos/so/common/logging/security/SecurityLogger.java) provides the ```withFacility(Runnable)``` method to make it easy and transparent to set this field.

To use the ```withFacility``` method in a microservice that consumes the ESOA SO Common Logging Library, pass it your normal log statements, as a zero-argument lambda expression, at the point where you wish to log the security event.
All log statements in the lambda expression will include the ```"facility": "log audit"``` field. For example:
* Single log statement:
```java
SecurityLogger.withFacility(() ->
    LOGGER.warn("A security event occurred!")
);
```
* Multiple statements:
```java
SecurityLogger.withFacility(() -> {
    LOGGER.error("Exception caught:", caughtException);
    if(caughtException instanceof ConnectivityLostException) {
        var exceptionMessage = caughtException.getMessage();
        LOGGER.error("Security event - lost connectivity:", exceptionMessage);
    }
});
```

## logcontrol.json

This library runs thread for monitoring of /logcontrol/logcontrol.json file on a subject of changes.

Example contents of logcontrol.json:
[{"container": "test", "severity": "ERROR", "customFilters": "com.ericsson.bos.so"}]

**container** -- DR-D1114-051 describes this value as follows.
The control configuration file controls log severity level of containers within a single service, and each service maintains its own control file. 

Only one logcontrol.json file is monitored at the moment.

**severity** -- the log level to set for Logback

**customFilters** -- currently only one package name is expected here.

## Q&A

Q: Why the library is compiled for Java 17?

A: Some microservices, such as api-gateway, still use Java 17 (at compile time and/or at runtime JRE 1.17.x). Multi-Release JARs are complicated to maintain.
