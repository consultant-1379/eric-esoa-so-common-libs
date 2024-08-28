/*******************************************************************************
 * COPYRIGHT Ericsson 2024
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
package com.ericsson.bos.so.common.logging.filter;

import static com.ericsson.bos.so.common.logging.config.CommonLoggingConstants.MDC_USERNAME_KEY;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * UserNameExtractorFilterTest
 */
class UserNameExtractorFilterTest {

    private HttpServletRequest httpRequest;
    private Authentication authentication;
    private SecurityContext securityContext;
    private HttpServletResponse httpResponse;
    private FilterChain filterChain;
    @Autowired
    private UserNameExtractorFilter userNameExtractorFilter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * setUp
     */
    @BeforeEach
    public void setUp() {
        httpResponse = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        httpRequest = mock(HttpServletRequest.class);
    }


    /**
     * doFilterInternal
     *
     * @throws ServletException
     * @throws IOException
     */
    @Test
    void doFilterInternal() throws ServletException, IOException {
        final String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJPNk9"
                + "kWGp0RGVqdVAxWWhfY2xCLV9FT04zSDZHLUhDSHVRaFdETDVTU0FrIn0."
                + "eyJleHAiOjE3MTMzNTM3OTgsImlhdCI6MTcxMzM1Mjg5OCwianRpIjoiNjQ0NDQyMD"
                + "gtMzU4Yy00ZDAyLTllYTMtY2Y1MzY3YzViYWVjIiwiaXNzIjoiaHR0cHM6Ly"
                + "9lcmljLXNlYy1hY2Nlc3MtbWdtdC5hdGxhbnRpYy1oYXJ0MTA2LmV3cy5naWMuZXJ"
                + "pY3Nzb24uc2UvYXV0aC9yZWFsbXMvbWFzdGVyIiwiYXVkIjpbIkF1dGhvcml6Y"
                + "XRpb25DbGllbnQiLCJhY2NvdW50Il0sInN1YiI6IjFhMzgzMTUyLTVlYTctNDU4Ny"
                + "04MTRiLTBhNDA3Yzk2YzA1NiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImF1d"
                + "GhuLXByb3h5Iiwic2Vzc2lvbl9zdGF0ZSI6ImQ1OWUyMDdiLTU5ZjMtNDhkZi1iZ"
                + "TEzLWFkMDQxMjM0NWRhNSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8"
                + "vKiIsImh0dHBzOi8vKiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiQ0VSVC1"
                + "BRE1JTiIsImRlZmF1bHQtcm9sZXMtbWFzdGVyIiwib2ZmbGluZV9hY2Nl"
                + "c3MiLCJqbXhfY2xpZW50IiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNl"
                + "X2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW"
                + "50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY2"
                + "9wZSI6ImVzb2Etcm9sZS1tYXBwaW5nIHByb2ZpbGUgZW1haWwiLCJzaWQiO"
                + "iJkNTllMjA3Yi01OWYzLTQ4ZGYtYmUxMy1hZDA0MTIzNDVkYTUiLCJpYW1fcm9s"
                + "ZXMiOlsiQ1NSLWxldmVsMSIsIkVDTV9CU1NfQlVTSU5FU1NfQ09ORklHVVJB"
                + "VElPTl9FTkciLCJFQ01fQlNTX0RFRkFVTFRfR1JPVVAiLCJFQ01fQlNTX0VESVRf"
                + "REVGQVVMVF9HUk9VUCIsIkVDTV9CU1NfTUFSS0VUSU5HX01BTkFHRVIiLC"
                + "JFQ01fQlNTX01BUktFVElOR19NQU5BR0VSX0pSIiwiRUNNX0JTU19QUk9EVUNUX"
                + "01BTkFHRVIiLCJFQ01fQlNTX1RNRl9WSUVXT05MWV9HUk9VUCIsIkVDTV9CU"
                + "1NfVklFV19ERUZBVUxUX0dST1VQIiwiRUNNX1VzZXJQcm9maWxlQWRtaW5pc3Ry"
                + "YXRvcnMiLCJFQ01fVklFV09OTFlfR1JPVVAiLCJFT0NfUENfQURBUFRFUl9HU"
                + "k9VUCIsIkVPQ19Vc2VyUHJvZmlsZUFkbWluaXN0cmF0b3JzIiwiRVNPQV9TdWJze"
                + "XN0ZW1BZG1pbiIsIkVTT0FfU3Vic3lzdGVtVmlld2VyIiwiR0FTX1VzZXIiLC"
                + "JMb2dWaWV3ZXIiLCJQQ19BRE1JTiIsIlNPRGVzaWduZXIiLCJTT1Byb3ZpZGVyQW"
                + "RtaW4iLCJTT1JlYWRPbmx5IiwiU09Vc2VyIiwiU09fT25ib2FyZGluZ0FkbWlu"
                + "IiwiU09fT25ib2FyZGluZ1ZpZXdlciIsIlVzZXIgUHJvZmlsZSBBZG1pbmlzdHJhd"
                + "G9ycyIsImVyaWMtYm9zLWRyOnJlYWRlciIsImVyaWMtYm9zLWRyOndyaXRlciI"
                + "sImVyaWMtYnNzLWJhbS1jbS1hZ2VudDpyZWFkZXIiLCJlcmljLWJzcy1iYW0tY20t"
                + "YWdlbnQ6d3JpdGVyIiwiZXJpYy1ic3MtYmFtLWNtLXN0b3JlOnJlYWRlciIsImV"
                + "yaWMtYnNzLWJhbS1jbS1zdG9yZTp3cml0ZXIiLCJlcmljLWJzcy1iYW0tZmF1bHQt"
                + "bWFuYWdlbWVudC1ndWk6YWRtaW4iLCJlcmljLWJzcy1iYW0tZmF1bHQtbWFuYWd"
                + "lbWVudC1ndWk6dXNlciIsImVyaWMtYnNzLWJhbS1mdW5jdGlvbi1jb250cm9sbGVy"
                + "OnJlYWRlciIsImVyaWMtYnNzLWJhbS1mdW5jdGlvbi1jb250cm9sbGVyOndyaXRl"
                + "ciIsImVyaWMtYnNzLWJhbS10b3BvbG9neS1tYW5hZ2VyLXN5bmM6cmVhZGVyIiwiZX"
                + "JpYy1ic3MtYmFtLXRvcG9sb2d5LW1hbmFnZXI6cmVhZGVyIiwiZXJpYy1ic3MtYm"
                + "FtLXRvcG9sb2d5LW1hbmFnZXI6d3JpdGVyIiwiZXJpYy1ic3MtZ3VpLWFnZ3JlZ2F0"
                + "b3I6dXNlciIsImVyaWMtYnNzLXVpLXNldHRpbmdzLW1hbmFnZXI6YWRtaW4iLCJl"
                + "cmljLWJzcy11aS1zZXR0aW5ncy1tYW5hZ2VyOnVzZXIiLCJlcmljLWNtLW1lZGlhdG"
                + "9yLWRkYzp3cml0ZXIiLCJlcmljLWNtLW1lZGlhdG9yOnJlYWRlciIsImVyaWMtY20t"
                + "bWVkaWF0b3I6d3JpdGVyIiwiZXJpYy1jdHJsLWJybzpyZWFkZXIiLCJlcmljLWN0cm"
                + "wtYnJvOndyaXRlciIsImVyaWMtZGF0YS1zZWFyY2gtZW5naW5lOnJlYWRlciIsImVyaW"
                + "MtZmgtYWxhcm0taGFuZGxlcjpyZWFkZXIiLCJlcmljLW9kY2EtZGlhZ25vc3RpYy1kY"
                + "XRhLWNvbGxlY3Rvcjp3cml0ZXIiLCJlcmljLXBtLXNlcnZlcjpyZWFkZXIiLCJlcm"
                + "ljLXNlYy1jZXJ0bTpyZWFkZXIiLCJlcmljLXNlYy1jZXJ0bTp3cml0ZXIiLCJlcmljL"
                + "XNlY3JldC1tb25pdG9yOnJlYWRlciIsImVyaWMtc2VjcmV0LW1vbml0b3I6d3JpdG"
                + "VyIl0sImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoiRVNPQSBTdXBlclVzZXIiL"
                + "CJwcmVmZXJyZWRfdXNlcm5hbWUiOiJlc29hLWFkbWluIiwiZ2l2ZW5fbmFtZSI6I"
                + "kVTT0EiLCJmYW1pbHlfbmFtZSI6IlN1cGVyVXNlciIsImVtYWlsIjoiZXNvYUBleGFt"
                + "cGxlLmNvbSJ9.YBcDKhTI5bEgwoE-LzUhN35WAxTuQfWqcwEHLJh-NPA9n-81cs"
                + "AS6UANwmSnYuc7noZeMMqlGaox_L80aevZh0Jgqt-BI2nzOZGMHAzQYJBmWE83ESBJ4"
                + "f9xRDPUQQmnyBSCZ13KUorsqbzrM4fvrjnBJFYNnPl4eroaXWg4zBJlhBJkrG0fl"
                + "AoQIVuAh2OItfTZilXsGNHwpEuhxyIBPBYaSDpNblpyvYVEuto6-d2N7QeJ5QjuQiC"
                + "9LD6iI4pVWvrlGOl3iahi-K5d9k2OvXAiLCLwvZqMslmf3aif_ZDAlHTzl_HJ8E9s"
                + "81nUkbx6QJBUoHzkyn0uEed4RiKFCw";
        Mockito.when(httpRequest.getHeader("Authorization")).thenReturn("Bearer " + token);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        userNameExtractorFilter = new UserNameExtractorFilter(objectMapper);
        userNameExtractorFilter.doFilterInternal(httpRequest, httpResponse, filterChain);
        assertNull(MDC.get(MDC_USERNAME_KEY));
    }

    /**
     * doFilterInternalWithoutUserName
     *
     * @throws ServletException
     * @throws IOException
     */
    @Test
    void doFilterInternalWithoutUserName() throws ServletException, IOException {
        final String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJPNk9";
        Mockito.when(httpRequest.getHeader("Authorization")).thenReturn("Bearer " + token);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        userNameExtractorFilter = new UserNameExtractorFilter(objectMapper);
        userNameExtractorFilter.doFilterInternal(httpRequest, httpResponse, filterChain);
        assertNull(MDC.get(MDC_USERNAME_KEY));
    }

    /**
     *
     * doFilterInternalWithoutAuthorization
     *
     * @throws ServletException
     * @throws IOException
     */
    @Test
    void doFilterInternalWithoutAuthorization() throws ServletException, IOException {
        userNameExtractorFilter = new UserNameExtractorFilter(objectMapper);
        userNameExtractorFilter.doFilterInternal(httpRequest, httpResponse, filterChain);
        assertNull(MDC.get(MDC_USERNAME_KEY));
    }

}