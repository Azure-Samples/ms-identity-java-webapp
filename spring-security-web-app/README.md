---
page_type: sample
languages:
- java
- html
author: ramya25
products:
- spring security
- azure-active-directory
description: "This sample demonstrates a Java web application showcasing how to use Spring security for logging in an user using OAuth2.0"
urlFragment: ms-identity-java-webapp
---

# A Java web application using Spring security which signs in users with the Microsoft identity platform

## About this sample

### Overview

This sample demonstrates a Java web application showcasing how to use spring security for logging in an user via OAuth2.0.

1. The Java web application:

   - Obtains an Id Token from Azure Active Directory (Azure AD) to sign in an user
   - uses Spring Security for logging-in a user via OAuth2.0

### Scenario

This sample shows how to build a Java web app that uses OpenId Connect to sign-in/ sign-out an user and to use spring security to sign-in user via OAuth2.0. For more information about how the protocols work in this scenario and other scenarios, see [Authentication Scenarios for Azure AD](https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-authentication-scenarios).

> Note: For a Spring web app sample signing in and authorizing access to users in an Azure AD group, refer to this [tutorial using Azure AD Spring Boot Starter](https://docs.microsoft.com/en-us/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-azure-active-directory).

## How to run this sample

To run this sample, you'll need:

- Working installation of Java and Maven
- An Azure Active Directory (Azure AD) tenant. For more information on how to get an Azure AD tenant, see [How to get an Azure AD tenant](https://azure.microsoft.com/en-us/documentation/articles/active-directory-howto-tenant/)
- One or more user accounts in your Azure AD tenant.

### Step 1: Download Java (8 and above) for your platform

To successfully use this sample, you need a working installation of [Java](https://openjdk.java.net/install/) and [Maven](https://maven.apache.org/).

### Step 2:  Clone or download this repository

From your shell or command line:

```Shell
- `git clone https://github.com/Azure-Samples/ms-identity-java-webapp.git`
```

Go to `spring-security-web-app` folder

```Shell
- `cd spring-security-web-app`
```

or download and extract the repository .zip file.

### Step 3:  Register the sample with your Azure Active Directory tenant

To register the project, you can follow the steps in the paragraphs below:

#### Choose the Azure AD tenant where you want to create your applications

As a first step you'll need to:

1. Sign in to the [Azure portal](https://portal.azure.com) using either a work or school account or a personal Microsoft account.
1. If your account is present in more than one Azure AD tenant, select your profile at the top right corner in the menu on top of the page, and then **switch directory**.
   Change your portal session to the desired Azure AD tenant.
1. In the portal menu, select the **Azure Active Directory** service, and then select **App registrations**.

> In the next steps, you might need the tenant name (or directory name) or the tenant ID (or directory ID). These are presented in the **Properties** of the Azure Active Directory window respectively as *Name* and *Directory ID*

#### Register the client app (spring-security-web-app)

1. Navigate to the Microsoft identity platform for developers [App registrations](https://go.microsoft.com/fwlink/?linkid=2083908) page.
1. Select **New registration**.
   - In the **Name** section, enter a meaningful application name that will be displayed to users of the app, for example `spring-security-web-app`.
   - In the **Supported account types** section, select **Accounts in any organizational directory**.
   - Click **Register** button at the bottom to create the application.
1. On the application **Overview** page, find the **Application (client) ID** and **Directory (tenant) ID** values and record it for later. You'll need it to configure the configuration file(s) later in your code.
1. In the Application menu blade, click on **Authentication**, under **Redirect URIs**, select **Web** and enter the redirect URL.
   By default, the sample uses:

   - `https://localhost:8443/msal4jsample/login`

    Click on **save**.

1. In the Application menu blade, click on **Certificates & Secrets** and click on `New client secret` in the **Client Secrets** section:

   - Type a key description (for instance `app secret`),
   - Select a key duration of either **In 1 year**, **In 2 years**, or **Never Expires** as per your security concerns.
   - The generated key value will be displayed when you click the **Add** button. **Copy the generated value for use in the steps later**.
     - You'll need this key later in your code's configuration files. This key value will not be displayed again, and is not retrievable by any other means, so make sure to note it from the Azure portal before navigating to any other screen or blade.

### Step 4:  Configure the sample to use your Azure AD tenant

Open `application.properties` in the src/main/resources folder. 
1. Fill in your tenant and app registration information noted in registration step. 
   - Replace `Enter_the_Tenant_Info_Here` with the **Tenant Id**
   - Replace `Enter_the_Application_Id_here` with the **Application Id**
   - Replace `Enter_the_Client_Secret_Here` with the **secret key value**

1. In order to use HTTPS on localhost, you need to set up a self-signed certificate. 
 - This terminal command will use Java's keytool utility to create a keystore called `keystore.p12` in the current directory, which is secured using the password `password`, and will create a cert with an alias of `testCert` and add it to the keystore.

`keytool -genkeypair -alias testCert -keyalg RSA -storetype PKCS12 -keystore keystore.p12 -storepass password`

 - Once you have your keystore/certificate, add its info to the SSL keystore properties in `application.properties`.
    - Replace `Enter_Key_Store_Here` with the path to the keystore.p12 file
    - Replace `Enter_Key_Store_Password_Here` and `Enter_Key_Password_Here` with the password
    - Replace `Enter_Key_Store_Type_Here` with the store type (PKCS12)
    - Replace `Enter_Key_Alias_Here` with the cert's alias

### Step 5: Run the application

To run the project, you can either:

Run it directly from your IDE by using the embedded spring boot server, or package it to a WAR file using [maven](https://maven.apache.org/plugins/maven-war-plugin/usage.html) and deploy it a J2EE container solution such as [Apache Tomcat](http://tomcat.apache.org/).

#### Running from IDE

If you are running the web application from an IDE, click on **run**, then navigate to the home page of the project. For this sample, the standard home page URL is <https://localhost:8443>, as defined in application.properties

#### Packaging and deploying to container

If you would like to deploy the web sample to Tomcat, you will need to make a couple of changes to the source code.

1. Open spring-security-web-app/pom.xml
    - Under `<name>spring-security-web-app</name>` add `<packaging>war</packaging>`

2. Open spring-security-web-app/src/main/java/com/microsoft/azure/springsecuritywebapp/SpringSecurityWebAppApplication

    - Delete all source code and replace with the following:

   ```Java
        package com.microsoft.azure.springsecuritywebapp;

        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.SpringBootApplication;
        import org.springframework.boot.builder.SpringApplicationBuilder;
        import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

        @SpringBootApplication
        public class SpringSecurityWebAppApplication extends SpringBootServletInitializer {

         public static void main(String[] args) {
          SpringApplication.run(SpringSecurityWebAppApplication.class, args);
         }

         @Override
         protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
          return builder.sources(SpringSecurityWebAppApplication.class);
         }
        }
   ```
3.   Tomcat's default HTTP port is 8080, though an HTTPS connection over port 8443 is needed. To configure this:
        - Go to tomcat/conf/server.xml
        - Search for the `<connector>` tag, and replace the existing connector with:
        ```
        <Connector
                   protocol="org.apache.coyote.http11.Http11NioProtocol"
                   port="8443" maxThreads="200"
                   scheme="https" secure="true" SSLEnabled="true"
                   keystoreFile="C:/Path/To/Keystore/File/keystore.p12" keystorePass="KeystorePassword"
                   clientAuth="false" sslProtocol="TLS"/>
        ``` 
       
4. Open a command prompt, go to the root folder of this sample (where the pom.xml file is located), and run `mvn package` to build the project
    - This will generate a `spring-security-web-app.war` file in your /targets directory.
    - Rename this file to `msal4jsample.war`
    - Deploy this war file using Tomcat or any other J2EE container solution.
        - To deploy, copy the ROOT.war file to the `/webapps/` directory in your Tomcat installation, and then start the Tomcat server.

5. Once deployed, go to https://localhost:8443/msal4jsample in your browser

### You're done

Click on "Login" to start the process of logging in. Once logged in, you'll see the name of the user who is authenticated. You'll then have the option to "Sign out".

## About the code

The relevant code for this sample is in the `AppConfiguration.java` file.

This class extends `WebSecurityConfigurerAdapter` from which the `configure` method is overridden, which allows the application to configure Springs HttpSecurity object. In the case of this sample, we configure settings for authorization of requests and logout of users"

```Java
@Configuration
@EnableOAuth2Sso
@Order(value = 0)
public class AppConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {

        String logoutUrl = env.getProperty("endSessionEndpoint") + "?post_logout_redirect_uri=" +
                URLEncoder.encode(env.getProperty("homePage"), "UTF-8");

        http.antMatcher("/**")
                .authorizeRequests()
                .antMatchers("/", "/login**", "/error**")
                    .permitAll()
                .anyRequest()
                    .authenticated()
                .and()
                    .logout()
                        .deleteCookies()
                        .invalidateHttpSession(true)
                        .logoutSuccessUrl(logoutUrl);
    }
}
```

## Community Help and Support

Use [Stack Overflow](http://stackoverflow.com/questions/tagged/msal) to get support from the community.
Ask your questions on Stack Overflow first and browse existing issues to see if someone has asked your question before.
Make sure that your questions or comments are tagged with [`Java`].

If you find a bug in the sample, please raise the issue on [GitHub Issues](https://github.com/Azure-Samples/ms-identity-java-webapp/issues).

To provide a recommendation, visit the following [User Voice page](https://feedback.azure.com/forums/169401-azure-active-directory).

## Contributing

If you'd like to contribute to this sample, see [CONTRIBUTING.MD](https://github.com/Azure-Samples/ms-identity-java-webapp/blob/master/CONTRIBUTING.md).

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information, see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## More information

For more information, see MSAL4J [conceptual documentation](https://github.com/AzureAD/azure-activedirectory-library-for-java/wiki)

For more information, see how spring security is used in [Java web api sample](https://github.com/Azure-Samples/ms-identity-java-webapi)

For more information about web apps scenarios on the Microsoft identity platform see [Scenario: Web app that signs in users](https://docs.microsoft.com/en-us/azure/active-directory/develop/scenario-web-app-sign-user-overview) and [Scenario: Web app that calls web APIs](https://docs.microsoft.com/en-us/azure/active-directory/develop/scenario-web-app-call-api-overview)

For more information about how OAuth 2.0 protocols work in this scenario and other scenarios, see [Authentication Scenarios for Azure AD](http://go.microsoft.com/fwlink/?LinkId=394414).
