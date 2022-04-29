# aurora_frontend

A vaadin implementation of the frontend.

# Local Development

## Prerequisites
The [Maven](https://www.baeldung.com/install-maven-on-windows-linux-mac) build tool must be installed on the host system, which also requires [Java JDK v11](https://www.digitalocean.com/community/tutorials/how-to-install-java-with-apt-on-ubuntu-18-04).

## Running locally
To start a local development server run `mvn spring-boot:run`. This starts the server and is accessible at localhost:8080.

# Deployment

## Prerequisites
Docker and must be present on the host machine.

## Running the server
The docker image registry.gitlab.com/ele-optics/aurora_frontend:latest should be used with the configured port exposed (default 8080).
