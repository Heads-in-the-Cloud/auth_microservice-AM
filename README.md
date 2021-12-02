# Auth API Repository

This repository holds the source code for the Auth API built in Spring Boot.

The sole use of this Microservice is to handle authentication issuing via JWT tokens and credential checking against a MySQL db.

The program is intended to run in a Docker container. If not using Docker or docker-compose to run the code, several environment variables must be defined for the database connection:
* DB_URL_SPRING (mysql url endpoint for Spring use)
* DB_USERNAME (mysql admin account username)
* DB_PASSWORD (mysql admin account password)
These variables must also be passed as environment variables to any Docker container created manually.

## Docker

This code is compiled into a Docker image held at amattsonsm/auth-api.

Port 8443 must be open on the container-side.

## API

Current Endpoints:
```sh
/login   (POST)
```
