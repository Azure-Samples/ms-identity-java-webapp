---
page_type: sample
languages:
- python
- html
products:
- azure-active-directory
description: "This sample demonstrates a Python web application calling a web api that is secured using Azure Active Directory."
urlFragment: ms-identity-python-webapp
---
# A Java web appplication with Azure AD B2C

### Overview

This sample shows how to build a Java web application that signs in users with Azure AD B2C using MSAL Java. It assumes you have some familiarity with Azure AD B2C. If you'd like to learn all that B2C has to offer, start with our documentation at aka.ms/aadb2c.

The app is a basic web application that performs three functions: sign-in, sign-up, and sign-out. It is intended to help get you started with Azure AD B2C, giving you the necessary tools to execute Azure AD B2C policies & securely identify users in your application.

This sample covers the following:

* Update the application in Azure AD B2C
* Configure the sample to use the application
* Enable authentication in a web application using Azure AD B2C
* Access a web API using access token


## Prerequisites

1. [Create an Azure Active Directory B2C tenant](https://docs.microsoft.com/en-us/azure/active-directory-b2c/tutorial-create-tenant)
1. [Register an application in Azure Active Directory B2C](https://docs.microsoft.com/en-us/azure/active-directory-b2c/tutorial-register-applications).
1. [Create user flows in Azure Active Directory B2C](https://docs.microsoft.com/en-us/azure/active-directory-b2c/tutorial-create-user-flows)
1. Working installation of [Java](https://openjdk.java.net/install/) 8 or above and [Maven](https://maven.apache.org/)


## Update the application

In the tutorial that you completed as part of the prerequisites, you [added a web application in Azure AD B2C](https://docs.microsoft.com/azure/active-directory-b2c/tutorial-register-applications).
To enable communication with the sample in this tutorial, you need to add a redirect URI to that application in Azure AD B2C.

* Modify an existing or add a new **Reply URL**,  `http://localhost:8080/msal4jsample/secure/aad`.
 Later we will set this sample to match what you register here.
* On the properties page, record the application ID that you'll use when you configure the web application.
* Also generate a key (client secret) for your web application. Record the key that you'll use when you configure this sample.


## Configure the sample

### Step 1:  Clone or download this repository

From your shell or command line:

```Shell
git clone https://github.com/Azure-Samples/ms-identity-java-webapp.git
```

Go to msal-b2c-web-sample folder

- `cd msal-b2c-web-sample`


### Step 2:  Configure the sample to use your Azure AD B2C tenant

In the steps below, "ClientID" is the same as "Application ID" or "AppId".

#### Configure the pythonwebapp project

> Note: if you used the setup scripts, the changes below may have been applied for you

1. Use the `app_config_b2c.py` template to replace `app_config.py`.
1. Open the (now replaced) `app_config.py` file

   * Update the value of `b2c_tenant` with the name of the Azure AD B2C tenant that you created.
     For example, replace `fabrikamb2c` with `contoso`.
   * Replace the value of `CLIENT_ID` with the application ID that you recorded.
   * Replace the value of `CLIENT_SECRET` with the key that you recorded.
   * Replace the value of `signupsignin_user_flow` with `b2c_1_signupsignin1`.
   * Replace the value of `editprofile_user_flow` with `b2c_1_profileediting1`.
   * Replace the value of `resetpassword_user_flow` with `b2c_1_passwordreset1`.
   * Replace the value of `REDIRECT_PATH` with the path part you set up in **Reply URL**.
     For example, `/getAToken`. It will be used by this sample app to form
     an absolute URL which matches your full **Reply URL**.
   * You do not have to configure the `ENDPOINT` and `SCOPE` right now

### Step 5: Run the application

To run the project, you can either:

Run it directly from your IDE by using the embedded spring boot server or package it to a WAR file using [maven](https://maven.apache.org/plugins/maven-war-plugin/usage.html) and deploy it a J2EE container solution such as [Apache Tomcat](http://tomcat.apache.org/).

#### Running from IDE

If you running you web application from an IDE, click on **run**, then navigate to the home page of the project. For this sample, the standard home page URL is <http://localhost:8080>

#### Packaging and deploying to container

If you would like to deploy the web sample to Tomcat, you will need to make a couple of changes to the source code.

1. Open msal-b2c-web-sample/pom.xml
    - Under `<name>msal-web-sample</name>` add `<packaging>war</packaging>`
    - Add dependency:

         ```xml
         <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-tomcat</artifactId>
          <scope>provided</scope>
         </dependency>
         ```

2. Open msal-b2c-web-sample/src/main/java/com.microsoft.azure.msalwebsample/MsalB2CWebSampleApplication

    - Delete all source code and replace with the following:

   ```Java
    package com.microsoft.azure.msalwebsample;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.boot.builder.SpringApplicationBuilder;
    import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

    @SpringBootApplication
    public class MsalB2CWebSampleApplication extends SpringBootServletInitializer {

     public static void main(String[] args) {
      SpringApplication.run(MsalB2CWebSampleApplication.class, args);
     }

     @Override
     protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
      return builder.sources(MsalB2CWebSampleApplication.class);
     }
    }
   ```

3. Open a command prompt, go to the root folder of the project, and run `mvn package`
    - This will generate a `msal-b2c-web-sample-0.1.0.war` file in your /targets directory. 
    - Rename this file to `ROOT.war`
    - Deploy this war file using Tomcat or any other J2EE container solution. 
        - To deploy on Tomcat container, copy the .war file to the webapps folder under your Tomcat installation and then start the Tomcat server.

This WAR will automatically be hosted at `http://<yourserverhost>:<yourserverport>/`
    - Tomcats default port is 8080. This can be changed by
        - Going to tomcat/conf/server.xml
        - Search "Connector Port"
        - Replace "8080" with your desired port number

Example: `http://localhost:8080/`

### You're done

Click on "Login" to start the process of logging in. Once logged in, you'll see the account information for the user that is logged in. You'll then have the option to "Sign out" or to "Show User Info", which will display the basic information of the signed-in user.


## Access a web API

This sample itself does not act as a web API.
Here we assume you already have your web API up and running elsewhere in your B2C tenant,
with a specific endpoint, protected by a specific scope,
and your sample app is already granted permission to access that web API.

Now you can configure this sample to access that web API.

1. Open the (now replaced) `app_config.py` file
   * Replace the value of `ENDPOINT` with the actual endpoint of your web API.
   * Replace the value of `SCOPE` with a list of the actual scopes of your web API.
     For example, write them as `["demo.read", "demo.write"]`.

Now, re-run your web app sample, and you will find a new link showed up,
and you can access the web API using Azure Active Directory B2C.

## Community Help and Support

Use [Stack Overflow](http://stackoverflow.com/questions/tagged/adal) to get support from the community.
Ask your questions on Stack Overflow first and browse existing issues to see if someone has asked your question before.
Make sure that your questions or comments are tagged with [`msal` `Java`].

If you find a bug in the sample, please raise the issue on [GitHub Issues](https://github.com/Azure-Samples/ms-identity-java-webapp/issues).

To provide a recommendation, visit the following [User Voice page](https://feedback.azure.com/forums/169401-azure-active-directory).

## Contributing

If you'd like to contribute to this sample, see [CONTRIBUTING.MD](https://github.com/Azure-Samples/ms-identity-java-webapp/blob/master/CONTRIBUTING.md).

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information, see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## More information

For more information, see MSAL4J [conceptual documentation](https://github.com/AzureAD/azure-activedirectory-library-for-java/wiki)

For more information about web apps scenarios on the Microsoft identity platform see [Scenario: Web app that signs in users](https://docs.microsoft.com/en-us/azure/active-directory/develop/scenario-web-app-sign-user-overview) and [Scenario: Web app that calls web APIs](https://docs.microsoft.com/en-us/azure/active-directory/develop/scenario-web-app-call-api-overview)

For more information about how OAuth 2.0 protocols work in this scenario and other scenarios, see [Authentication Scenarios for Azure AD](http://go.microsoft.com/fwlink/?LinkId=394414).

