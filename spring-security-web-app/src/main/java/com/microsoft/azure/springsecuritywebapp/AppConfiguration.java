// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.springsecuritywebapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.net.URLEncoder;


@Configuration
@EnableWebSecurity
@Order(value = 0)
public class AppConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private Environment env;

    @Override
    public void configure(HttpSecurity http) throws Exception {

        String logoutUrl = env.getProperty("endSessionEndpoint") + "?post_logout_redirect_uri=" +
                URLEncoder.encode(env.getProperty("homePage"), "UTF-8");

        http.antMatcher("/**")
                .authorizeRequests()
                .antMatchers("/", "/login**", "/error**")
                    .permitAll()
                .anyRequest()
                    .authenticated().and().oauth2Login()
                .and()
                    .logout()
                        .deleteCookies()
                        .invalidateHttpSession(true)
                        .logoutSuccessUrl(logoutUrl);
    }
}
