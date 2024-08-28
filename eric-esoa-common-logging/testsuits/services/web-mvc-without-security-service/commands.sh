#!/usr/bin/env bash
#
# COPYRIGHT Ericsson 2023
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

#Use this script to forward logs from output to file. This file will be later analyzed by acceptance tests
exec java -cp app:app/lib/* com.example.logging.WebMvcWithOutSecurityApplication > logs/web-mvc-without-security-logging.log 2> logs/web-mvc-without-security-error-logging.log