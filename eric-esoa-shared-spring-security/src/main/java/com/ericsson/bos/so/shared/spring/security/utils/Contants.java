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
package com.ericsson.bos.so.shared.spring.security.utils;

/**
 * Calls to store contstants
 */
public class Contants {

    public static final String AUTHORIZATION_REFRESH = "Authorization-Refresh";

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String TOKEN = "token";

    public static final String DECRYPTED_TOKEN = "token_info";

    public static final String TOKEN_REFRESH = "refresh_token";

    public static final String TENANT_NAME= "tenant_name";

    public static final String TENANT_HEADER = "X-Tenant-Id";

    public static final String TENANT_DEFAULT = "unknown";

    /**
     * Constants constructor
     */
    private Contants() {
        // disable constructor
    }


}
