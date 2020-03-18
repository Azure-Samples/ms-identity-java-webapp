// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.msalwebsample;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Object containing configuration data for the application. Spring will automatically wire the
 * values by grabbing them from application.properties file
 */
@Component
@ConfigurationProperties("aad")
class BasicConfiguration {

    private String clientId;
    private String authority;
    private String redirectUriSignin;
    private String redirectUriGraph;
    private String secretKey;
    private String msGraphEndpointHost;

    public String getAuthority(){
        if (!authority.endsWith("/")) {
            authority += "/";
        }
        return authority;
    }

    public String getClientId() {
        return clientId;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUriSignin() {
        return redirectUriSignin;
    }

    public void setRedirectUriSignin(String redirectUriSignin) {
        this.redirectUriSignin = redirectUriSignin;
    }

    public String getRedirectUriGraph() {
        return redirectUriGraph;
    }

    public void setRedirectUriGraph(String redirectUriGraph) {
        this.redirectUriGraph = redirectUriGraph;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setMsGraphEndpointHost(String msGraphEndpointHost) {
        this.msGraphEndpointHost = msGraphEndpointHost;
    }

    public String getMsGraphEndpointHost(){
        return msGraphEndpointHost;
    }
}