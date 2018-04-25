# Oauth2 Stub Authentication Service
This is a stub service that implements Oauth2 spec.

We haven't used spring-oauth because of the raw JDBC data source management. 

It is not open to extension and removes the benefits of a sophisticated ORM solution like JPA/Hibernate

Only supported grant option is Resource Owner Credentials - Password currently.

## Build

```./gradlew build```

## Running Locally
- Import project into IntelliJ and Run Application

OR 

- ```./gradlew bootRun```

### Environment Variables
You will need to add the following env variables to run code locally, or to run test suite 
```
GOV_NOTIFY_API_KEY_DEV
GOV_NOTIFY_API_KEY_TEST
ENV_URL
GOV_NOTIFY_TEMPLATE_ID
```

# Deployment

## Requirements

A Backing storage solution is required. 

Currently MySQL is chosen for the small footprint and Free plans. 

For setting up and linking the storage solution please see.

https://docs.cloud.service.gov.uk/#set-up-a-mysql-service


## Publishing

Currently it is deployed on Cloud Foundary for CSHR.

Run the following command to deploy as a CF app.

```cf push {APPNAME} -p build/libs/oauth2-{VERSION}.jar```

Set the following environment variables to configure the application.

```cf set-env {APPNAME} DATASOURCE {JDBC URL WITH CREDENTIALS}```

### Notes

There are some extra packages and unnecessary dependencies in the gradle. They are required by CF unfortunately.

# Usage

## Requesting an access token

`https://{SERVER_URL}:{SERVER_PORT}/oauth/authorize?response_type=token&client_id={CLIENT_ID}&redirect_uri={SERVICE_URI}`

This will redirect you to a login page which will, on successful authentication direct you back to your service with a token.

## Retrieving identity information from an access token

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