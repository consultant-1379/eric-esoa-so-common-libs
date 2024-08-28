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
package com.ericsson.bos.so.shared.spring.security.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JwtModel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwtModel {

    @JsonProperty("preferred_username")
    private String preferredUsername;

    @JsonProperty("realm_access")
    private AccessRoles accessRoles = new AccessRoles();

    @JsonProperty("resource_access")
    private AccessRoles resourceAccess = new AccessRoles();

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    public AccessRoles getRealmAccess() {
        return accessRoles;
    }

    public void setRealmAccess(AccessRoles accessRoles) {
        this.accessRoles = accessRoles;
    }

    public AccessRoles getResourceAccess() {
        return resourceAccess;
    }

    public void setResourceAccess(AccessRoles resourceAccess) {
        this.resourceAccess = resourceAccess;
    }
}
