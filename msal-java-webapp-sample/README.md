---
page_type: sample
languages:
- java
- powershell
- html
products:
- azure-active-directory
description: "This sample demonstrates a Java web application calling a Microsoft Graph that is secured using Azure Active Directory."
urlFragment: ms-identity-java-webapp
---

# A Java Web application that signs in users with the Microsoft identity platform and calls Microsoft Graph

## About this sample

> This sample is also available as a quickstart for the Microsoft identity platform: [Quickstart: Add sign-in with Microsoft to a Java web app](https://docs.microsoft.com/azure/active-directory/develop/quickstart-v2-java-webapp)

### Overview

This sample demonstrates a Java web application calling a Microsoft Graph that is secured using Azure Active Directory.

1. The Java web application uses the Microsoft Authentication Library for Java (MSAL4J) to obtain an:

   - Id Token from Azure Active Directory (Azure AD) to sign in an user
   - Access token that is used as a bearer token when calling the Microsoft Graph to get basic information of the signed-in user.

     ![Topology](./ReadmeFiles/Java-WebApp-Diagram.png)

> For a java web app sample using Spring Security framework take a look at [Spring Security webapp sample](https://github.com/Azure-Samples/ms-identity-java-webapp/tree/master/spring-security-web-app).
>
> For a java web app sample using MSAL with Azure AD B2C take a look at [MSAL B2C webapp sample](https://github.com/Azure-Samples/ms-identity-java-webapp/tree/master/msal-b2c-web-sample).

### Scenario

This sample shows how to build a Java web app that uses OpenId Connect to sign in/ sign out an user and to get access to the Microsoft Graph using MSAL4J. For more information about how the protocols work in this scenario and other scenarios, see [Authentication Scenarios for Azure AD](https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-authentication-scenarios).

## How to run this sample

To run this sample, you'll need:

- Working installation of Java and Maven
- An Internet connection
- An Azure Active Directory (Azure AD) tenant. For more information on how to get an Azure AD tenant, see [How to get an Azure AD tenant](https://azure.microsoft.com/en-us/documentation/articles/active-directory-howto-tenant/)
- A user account in your Azure AD tenant.

### Step 1: Download Java (8 and above) for your platform

To successfully use this sample, you need a working installation of [Java](https://openjdk.java.net/install/) and [Maven](https://maven.apache.org/).

### Step 2:  Clone or download this repository

From your shell or command line:

- `git clone https://github.com/Azure-Samples/ms-identity-java-webapp.git`

### Step 3:  Register the sample with your Azure Active Directory tenant

To register these projects, you can:

- either follow the steps in the paragraphs below
- or use PowerShell scripts that:
  - **automatically** create for you the Azure AD applications and related objects (passwords, permissions, dependencies)

If you want to use this automation, read the instructions in [App Creation Scripts](./AppCreationScripts/AppCreationScripts.md). Please note that the configuration of your code (Step 4) still needs to be done manually.

#### First step: choose the Azure AD tenant where you want to create your applications

As a first step you'll need to:

1. Sign in to the [Azure portal](https://portal.azure.com).
1. On the top bar, click on your account, and then on **Switch Directory**.
1. Once the *Directory + subscription* pane opens, choose the Active Directory tenant where you wish to register your application, from the *Favorites* or *All Directories* list.
1. Click on **All services** in the portal menu, and choose **Azure Active Directory**.

> In the next steps, you might need the tenant name (or directory name) or the tenant ID (or directory ID). These are presented in the **Properties** of the Azure Active Directory window respectively as *Name* and *Directory ID*

#### Register the app (java-webapp)

1. In the  **Azure Active Directory** pane, click on **App registrations** and choose **New registration**.
1. Enter a friendly name for the application, for example 'java-webapp', select **Accounts in any organizational directory and personal Microsoft Accounts (e.g. Skype, Xbox, Outlook.com)**.
1. Click **Register** to register the application.
1. On the application **Overview** page:
    - copy **Application (client) ID**
    - copy **Directory (tenant) ID**
    - You'll need both of these values later to configure the project, so copy them in a safe place.
1. On the application **Authentication** page, under **Redirect URIs**, select **Web**. You will need to enter two different redirect URIs: one for the sign-in page, and one for the graph page. For both, you should use the same host and port number, then followed by "/msal4jsample/secure/aad" for the sign-in page and "msal4jsample/graph/me" for the user info page.
   By default, the sample uses:

    - `https://localhost:8443/msal4jsample/secure/aad`.
    - `https://localhost:8443/msal4jsample/graph/me`

    Click on **save**.

1. On the application menu, choose **Certificates & Secrets** and click on `New client secret` in the **Client Secrets** section:

   - Type a key description (for instance `app secret`),
   - Select a key duration of either **In 1 year**, **In 2 years**, or **Never Expires**.
   - The key value will display when you select **Add**. Copy the its value in a safe place.
   - You'll need this key later to configure the project. This key value will not be displayed again, nor retrievable by any other means, so record it as soon as it is visible from the Azure portal.

### Step 4:  Configure the sample to use your Azure AD tenant

Open `application.properties` in the src/main/resources folder. Fill in with your tenant and app registration information noted in registration step. Replace *Enter_the_Tenant_Info_Here* with the **Tenant Id**, *Enter_the_Application_Id_here* with the **Application Id** and *Enter_the_Client_Secret_Here* with the **key value** noted.

If you did not use the  default redirect URIs, then you'll have to update `aad.redirectUriSignin` and `aad.redirectUriGraph` as well with the registered redirect URIs.
> You can use any host and port number, but the path must stay the same (/msal4jsample/secure/aad and /msal4jsample/graph/me) as these are mapped to the controllers that will process the requests.

In order to use https with localhost fill in server.ssl.key properties.  
Use keytool utility (included in JRE) if you want to generate self-signed certificate.

```
Example:  
keytool -genkeypair -alias testCert -keyalg RSA -storetype PKCS12 -keystore keystore.p12 -storepass password

server.ssl.key-store-type=PKCS12  
server.ssl.key-store=classpath:keystore.p12  
server.ssl.key-store-password=password  
server.ssl.key-alias=testCert
```

### Step 5: Run the application

To run the project, you can either:

Run it directly from your IDE by using the embedded spring boot server or package it to a WAR file using [maven](https://maven.apache.org/plugins/maven-war-plugin/usage.html) and deploy it a J2EE container solution such as [Apache Tomcat](http://tomcat.apache.org/).

#### Running from IDE

If you running you web application from an IDE, click on **run**, then navigate to the home page of the project. For this sample, the standard home page URL is <https://localhost:8443>

#### Packaging and deploying to container

If you would like to deploy the web sample to Tomcat, you will need to make a couple of changes to the source code.

1. Open ms-identity-java-webapp/pom.xml
    - Under `<name>msal-web-sample</name>` add `<packaging>war</packaging>`

2. Open ms-identity-java-webapp/src/main/java/com.microsoft.azure.msalwebsample/MsalWebSampleApplication

    - Delete all source code and replace with the following:

   ```Java
    package com.microsoft.azure.msalwebsample;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.boot.builder.SpringApplicationBuilder;
    import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

    @SpringBootApplication
    public class MsalWebSampleApplication extends SpringBootServletInitializer {

     public static void main(String[] args) {
      SpringApplication.run(MsalWebSampleApplication.class, args);
     }

     @Override
     protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
      return builder.sources(MsalWebSampleApplication.class);
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
    - This will generate a `msal-web-sample-0.1.0.war` file in your /targets directory.
    - Rename this file to `msal4jsample.war`
    - Deploy this war file using Tomcat or any other J2EE container solution.
        - To deploy, copy the msal4jsample.war file to the `/webapps/` directory in your Tomcat installation, and then start the Tomcat server.

5. Once deployed, go to https://localhost:8443/msal4jsample in your browser


### You're done

Click on "Login" to start the process of logging in. Once logged in, you'll see the account information for the user that is logged in. You'll then have the option to "Sign out" or to "Show User Info", which will display the basic information of the signed-in user.

- Developers who wish to gain good familiarity of programming for Microsoft Graph are advised to go through the [An introduction to Microsoft Graph for developers](https://www.youtube.com/watch?v=EBbnpFdB92A) recorded session.

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
