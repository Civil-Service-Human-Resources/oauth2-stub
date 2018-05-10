# Identity Service  
## Overview
The Identity Service uses [Spring Security OAuth](http://projects.spring.io/spring-security-oauth/) to utilise OAuth components. We are using with custom Details, Providers and Services in this implementation.

## Getting Started
### Environment Variables
You will need to add the following env variables to run code locally, or to run the test suite.

| VARIABLE | DESCRIPTION | DEFAULT |
|--|--|--|
|GOV_NOTIFY_API_KEY | Api key for Gov Notify|NO|
|GOV_NOTIFY_INVITE_TEMPLATE_ID|Template ID for invite emails|NO|
|GOV_NOTIFY_RESET_TEMPLATE_ID|Template ID for password reset emails|NO|
|GOV_NOTIFY_RESET_SUCCESSFUL_TEMPLATE_ID|Template ID for successful password reset emails|NO|
|INVITE_SIGNUP_URL|The External Url to be sent for invites|NO|
|RESET_URL|The External Url to be sent for password reset|NO|
|DATASOURCE|The Datasource connectiong string|NO|
|PASSWORD_PATTERN|The Regex pattern to apply password policy on|YES|

### Build
Build the application using Gradle ```./gradlew build```  

Run the project with Gradle or ```./gradlew bootRun``` import project into IntelliJ and Run Application.  

The application has two interfaces, a user interface for standard login to LPG, and a management for admin tasks. When the application runs on localhost, these components will run on individual ports:
e.g.  
* OAUTH2 - `localhost:8080/oauth/*`  
* MANAGEMENT - `localhost:8081/management/login`  


### Using the application
The user can log in to the management portal to perform CRUD operations on Roles, Identities as well as Inviting new users.

With the standard user interface, this can be used for logging in and resetting a password.

## Deployment

## Requirements

A Backing storage solution is required. 

Currently MySQL is chosen for the small footprint and Free plans. 

For setting up and linking the storage solution please see - https://docs.cloud.service.gov.uk/#set-up-a-mysql-service


## Usage

### Requesting an access token

`https://{SERVER_URL}:{SERVER_PORT}/oauth/authorize?response_type=token&client_id={CLIENT_ID}&redirect_uri={SERVICE_URI}`

This will redirect you to a login page which will, on successful authentication direct you back to your service with a token.

### Retrieving identity information from an access token

```
curl -X GET \
  https://{SERVER URL:SERVER PORT}/identity?access_token={accessToken} \
  -H 'Cache-Control: no-cache' \
```

Will respond like

```
{
    "username": "user@domain.com",
    "uid": "3c706a70-3fff-4e7b-ae7f-102c1d46f569",
    "roles": [
        "USER"
    ]
}
```
