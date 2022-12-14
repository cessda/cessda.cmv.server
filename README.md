# cessda.cmv.server

[![Build Status](https://jenkins.cessda.eu/buildStatus/icon?job=cessda.cmv.server%2Fmaster)](https://jenkins.cessda.eu/job/cessda.cmv.server/job/master/)
[![Quality Gate Status](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.cmv%3Acmv-server&metric=alert_status)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.cmv%3Acmv-server)
[![Coverage](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.cmv%3Acmv-server&metric=coverage)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.cmv%3Acmv-server)
[![Code Smells](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.cmv%3Acmv-server&metric=code_smells)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.cmv%3Acmv-server)
[![Technical Debt](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.cmv%3Acmv-server&metric=sqale_index)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.cmv%3Acmv-server)
[![Security Rating](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.cmv%3Acmv-server&metric=security_rating)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.cmv%3Acmv-server)
[![Vulnerabilities](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.cmv%3Acmv-server&metric=vulnerabilities)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.cmv%3Acmv-server)
[![Bugs](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.cmv%3Acmv-server&metric=bugs)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.cmv%3Acmv-server)


## Getting started as developer

### Execute tests and run the application

```
# Execute all tests locally with default config
mvn clean test

# Run the app locally with default config and pre-populated database
mvn clean spring-boot:run
# Browse to http://localhost:8080 
# Stop the app with Ctrl+C
```

### Create and run service environment with docker-compose

```
# Package Java jar file and build docker image with required settings
mvn -DskipTests clean package docker:build -Pdocker-compose

# Create and start the environment in daemon mode (-d)
# as specified in docker-compose.yml
docker-compose -f target/docker/context/docker-compose.yml up -d

# Show all (-a) containers, service must be healthy to be available
docker ps -a

# Check out with our browser: http://localhost:8080
# Check out with our browser: http://localhost:8080/actuator

# Stop the environment
# All containers and the local network are stopped, but not deleted
docker-compose -f target/docker/context/docker-compose.yml stop

# Start the existing environment
# All containers and the local network are started
docker-compose -f target/docker/context/docker-compose.yml start

# Open a shell within the running container as specified in docker-compose.yml
# Exit the container shell again by Ctrl+C
docker exec -it $CONTAINERID /bin/sh
# Checkout filesystem within container with 'ls -la'

# Shutdown the environment
# All containers and the local network are stopped and deleted
docker-compose -f target/docker/context/docker-compose.yml down
```
