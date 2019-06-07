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
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.microsoft.aad.msal4j.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserController {

    @Autowired
    BasicConfiguration configuration;

    @RequestMapping("/")
    public String homepage(){
        return "index";
    }

    @RequestMapping("/graph/users")
    public ModelAndView getUsersFromGraph(ModelMap model, HttpServletRequest httpRequest) {

        HttpSession session = httpRequest.getSession();
        IAuthenticationResult result = (IAuthenticationResult) session.getAttribute(AuthHelper.PRINCIPAL_SESSION_NAME);
        ModelAndView mav;
        if (result == null) {
            mav = new ModelAndView("error");
            mav.addObject("error", new Exception("AuthenticationResult not found in session."));
        } else {
            setUserInfoAndTenant(model, session);

            try {
                String userData = getUserNamesFromGraph(result.accessToken());

                mav = new ModelAndView("users");
                mav.addObject("users", userData);
                mav.addObject("id_token", result.idToken());
                return mav;
            } catch (Exception e) {
                mav = new ModelAndView("error");
                mav.addObject("error", e);
            }
        }
        return mav;
    }

    private String getUserNamesFromGraph(String accessToken) throws Exception {
        // Microsoft Graph users endpoint
        URL url = new URL("https://graph.microsoft.com/v1.0/users");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set the appropriate header fields in the request header.
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        String response = HttpClientHelper.getResponseStringFromConn(conn);

        int responseCode = conn.getResponseCode();
        if(responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException(response);
        }

        JSONObject responseObject = HttpClientHelper.processResponse(responseCode, response);
        JSONArray users = JSONHelper.fetchDirectoryObjectJSONArray(responseObject);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < users.length(); i++) {
            JSONObject thisUserJSONObject = users.optJSONObject(i);
            User user = new User();
            JSONHelper.convertJSONObjectToDirectoryObject(thisUserJSONObject, user);
            builder.append(user.getUserPrincipalName());
            builder.append("<br/>");
        }
        return builder.toString();
    }

    private void setUserInfoAndTenant(
            ModelMap model,
            HttpSession session){
        String tenant = session.getServletContext().getInitParameter("tenant");
        model.addAttribute("tenant", tenant);
    }
}
