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

```
curl -X POST \
  https://{SERVER URL:SERVER PORT}/oauth/token \
  -H 'Authorization: {BASIC AUTH}' \
  -H 'Cache-Control: no-cache' \
  -H 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  -F grant_type=password \
  -F username={EMAIL} \
  -F password={PASSWORD}
```

Will respond like

```
{
    "access_token": "RggaIog375oZDYZbYnSZUP4g0UMBZ31KyHti0F2jZr",
    "token_type": "Bearer",
    "expires_in": 1440
}
```

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