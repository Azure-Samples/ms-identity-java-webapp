// Copyright (c) Microsoft Corporation.
// All rights reserved.
//
// This code is licensed under the MIT License.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.microsoft.azure.msalwebsample;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.*;

import javax.naming.ServiceUnavailableException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.microsoft.aad.msal4j.*;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthFilter implements Filter {

    private static final String STATES = "states";
    private static final String STATE = "state";
    private static final Integer STATE_TTL = 3600;
    private static final String FAILED_TO_VALIDATE_MESSAGE = "Failed to validate data received from Authorization service - ";

    private List<String> excludedUrls = Collections.singletonList("/");
    private String clientId;
    private String clientSecret;
    private String authority;
    private String redirectUri;

    @Autowired
    BasicConfiguration configuration;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            try {
                String currentUri = httpRequest.getRequestURL().toString();
                String path = httpRequest.getServletPath();
                String queryStr = httpRequest.getQueryString();
                String fullUrl = currentUri + (queryStr != null ? "?" + queryStr : "");

                // exclude home page
                if(excludedUrls.contains(path)){
                    chain.doFilter(request, response);
                    return;
                }
                // check if user has a AuthData in the session
                if (!AuthHelper.isAuthenticated(httpRequest)) {
                     if(AuthHelper.containsAuthenticationCode(httpRequest)){
                        // response should have authentication code, which will be used to acquire access token
                        processAuthenticationCodeRedirect(httpRequest, currentUri, fullUrl);
                    } else {
                         // not authenticated, redirecting to login.microsoft.com so user can authenticate
                        sendAuthRedirect(httpRequest, httpResponse);
                        return;
                    }
                }

                if (isAccessTokenExpired(httpRequest)) {
                    updateAuthDataUsingSilentFlow(httpRequest);
                }

            } catch (AuthenticationException authException) {
                // something went wrong (like expiration or revocation of token)
                // we should invalidate AuthData stored in session and redirect to Authorization server
                removePrincipalFromSession(httpRequest);
                sendAuthRedirect(httpRequest, httpResponse);
                return;
            } catch (Throwable exc) {
                httpResponse.setStatus(500);
                request.setAttribute("error", exc.getMessage());
                request.getRequestDispatcher("/error").forward(request, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig config) {
        clientId = configuration.getClientId();
        authority = configuration.getAuthority();
        clientSecret = configuration.getSecretKey();
        redirectUri = configuration.getRedirectUri();
    }

    @Override
    public void destroy() {
    }

    private boolean isAccessTokenExpired(HttpServletRequest httpRequest) {
        IAuthenticationResult result = AuthHelper.getAuthSessionObject(httpRequest);
        return result.expiresOnDate().before(new Date());
    }

    private void updateAuthDataUsingSilentFlow(HttpServletRequest httpRequest) throws Throwable {
        IAuthenticationResult authResult = getAuthResultBySilentFlow(httpRequest);
        setSessionPrincipal(httpRequest, authResult);
    }

    private void processAuthenticationCodeRedirect(HttpServletRequest httpRequest, String currentUri, String fullUrl)
            throws Throwable {

        Map<String, List<String>> params = new HashMap<>();
        for (String key : httpRequest.getParameterMap().keySet()) {
            params.put(key, Collections.singletonList(httpRequest.getParameterMap().get(key)[0]));
        }
        // validate that state in response equals to state in request
        StateData stateData = validateState(httpRequest.getSession(), params.get(STATE).get(0));

        AuthenticationResponse authResponse = AuthenticationResponseParser.parse(new URI(fullUrl), params);
        if (AuthHelper.isAuthenticationSuccessful(authResponse)) {
            AuthenticationSuccessResponse oidcResponse = (AuthenticationSuccessResponse) authResponse;
            // validate that OIDC Auth Response matches Code Flow (contains only requested artifacts)
            validateAuthRespMatchesAuthCodeFlow(oidcResponse);

            IAuthenticationResult result = getAuthResultByAuthCode(
                    httpRequest,
                    oidcResponse.getAuthorizationCode(),
                    currentUri);

            // validate nonce to prevent reply attacks (code maybe substituted to one with broader access)
            validateNonce(stateData, getClaimValueFromIdToken(result.idToken()));
            setSessionPrincipal(httpRequest, result);
        } else {
            AuthenticationErrorResponse oidcResponse = (AuthenticationErrorResponse) authResponse;
            throw new Exception(String.format("Request for auth code failed: %s - %s",
                    oidcResponse.getErrorObject().getCode(),
                    oidcResponse.getErrorObject().getDescription()));
        }
    }

    private void sendAuthRedirect(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        // state parameter to validate response from Authorization server and nonce parameter to validate idToken
        String state = UUID.randomUUID().toString();
        String nonce = UUID.randomUUID().toString();
        storeStateInSession(httpRequest.getSession(), state, nonce);

        httpResponse.setStatus(302);
        String redirectUrl = getRedirectUrl(httpRequest.getParameter("claims"), state, nonce);
        httpResponse.sendRedirect(redirectUrl);
    }


    private IAuthenticationResult getAuthResultByAuthCode(
            HttpServletRequest httpServletRequest,
            AuthorizationCode authorizationCode,
            String currentUri) throws Throwable {

        IAuthenticationResult result;
        ConfidentialClientApplication app;
        try {
            app = createClientApplication();

            String authCode = authorizationCode.getValue();
            AuthorizationCodeParameters parameters = AuthorizationCodeParameters.builder(
                    authCode,
                    new URI(currentUri)).
                    build();

            Future<IAuthenticationResult> future = app.acquireToken(parameters);

            result = future.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }

        if (result == null) {
            throw new ServiceUnavailableException("authentication result was null");
        }

        storeTokenCacheInSession(httpServletRequest, app.tokenCache().serialize());

        return result;
    }

    private IAuthenticationResult getAuthResultBySilentFlow(HttpServletRequest httpRequest) throws Throwable {

        IAuthenticationResult result =  AuthHelper.getAuthSessionObject(httpRequest);

        IAuthenticationResult updatedResult;
        ConfidentialClientApplication app;
        try {
           app = createClientApplication();

           Object tokenCache =  httpRequest.getSession().getAttribute("token_cache");
           if(tokenCache != null){
               app.tokenCache().deserialize(tokenCache.toString());
           }

            SilentParameters parameters = SilentParameters.builder(
                    Collections.singleton("https://graph.microsoft.com/.default"),
                    result.account()).build();

            CompletableFuture<IAuthenticationResult> future = app.acquireTokenSilently(parameters);

            updatedResult = future.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }

        if (updatedResult == null) {
            throw new ServiceUnavailableException("authentication result was null");
        }

        //update session with latest token cache
        storeTokenCacheInSession(httpRequest, app.tokenCache().serialize());

        return updatedResult;
    }

    private ConfidentialClientApplication createClientApplication() throws MalformedURLException {
        return ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.create(clientSecret)).
                authority(authority).
                build();
    }

    private void validateNonce(StateData stateData, String nonce) throws Exception {
        if (StringUtils.isEmpty(nonce) || !nonce.equals(stateData.getNonce())) {
            throw new Exception(FAILED_TO_VALIDATE_MESSAGE + "could not validate nonce");
        }
    }

    private String getClaimValueFromIdToken(String idToken) throws ParseException {
        return (String) JWTParser.parse(idToken).getJWTClaimsSet().getClaim("nonce");
    }

    private StateData validateState(HttpSession session, String state) throws Exception {
        if (StringUtils.isNotEmpty(state)) {
            StateData stateDataInSession = removeStateFromSession(session, state);
            if (stateDataInSession != null) {
                return stateDataInSession;
            }
        }
        throw new Exception(FAILED_TO_VALIDATE_MESSAGE + "could not validate state");
    }

    private void validateAuthRespMatchesAuthCodeFlow(AuthenticationSuccessResponse oidcResponse) throws Exception {
        if (oidcResponse.getIDToken() != null || oidcResponse.getAccessToken() != null ||
                oidcResponse.getAuthorizationCode() == null) {
            throw new Exception(FAILED_TO_VALIDATE_MESSAGE + "unexpected set of artifacts received");
        }
    }

    private void setSessionPrincipal(HttpServletRequest httpRequest, IAuthenticationResult result) {
        httpRequest.getSession().setAttribute(AuthHelper.PRINCIPAL_SESSION_NAME, result);
    }

    private void storeTokenCacheInSession(HttpServletRequest httpServletRequest, String tokenCache){
        httpServletRequest.getSession().setAttribute("token_cache", tokenCache);
    }

    private void removePrincipalFromSession(HttpServletRequest httpRequest) {
        httpRequest.getSession().removeAttribute(AuthHelper.PRINCIPAL_SESSION_NAME);
    }

    private void storeStateInSession(HttpSession session, String state, String nonce) {
        if (session.getAttribute(STATES) == null) {
            session.setAttribute(STATES, new HashMap<String, StateData>());
        }
        ((Map<String, StateData>) session.getAttribute(STATES)).put(state, new StateData(nonce, new Date()));
    }

    private StateData removeStateFromSession(HttpSession session, String state) {
        Map<String, StateData> states = (Map<String, StateData>) session.getAttribute(STATES);
        if (states != null) {
            eliminateExpiredStates(states);
            StateData stateData = states.get(state);
            if (stateData != null) {
                states.remove(state);
                return stateData;
            }
        }
        return null;
    }

    private void eliminateExpiredStates(Map<String, StateData> map) {
        Iterator<Map.Entry<String, StateData>> it = map.entrySet().iterator();

        Date currTime = new Date();
        while (it.hasNext()) {
            Map.Entry<String, StateData> entry = it.next();
            long diffInSeconds = TimeUnit.MILLISECONDS.
                    toSeconds(currTime.getTime() - entry.getValue().getExpirationDate().getTime());

            if (diffInSeconds > STATE_TTL) {
                it.remove();
            }
        }
    }

    private String getRedirectUrl(String claims, String state, String nonce)
            throws UnsupportedEncodingException {

        String redirectUrl = authority + "oauth2/v2.0/authorize?" +
                "response_type=code&" +
                "response_mode=form_post&" +
                "redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") +
                "&client_id=" + clientId +
                "&scope=" + URLEncoder.encode("openid offline_access profile https://graph.microsoft.com/.default", "UTF-8") +
                (StringUtils.isEmpty(claims) ? "" : "&claims=" + claims) +
                "&state=" + state
                + "&nonce=" + nonce;

        return redirectUrl;
    }
}
