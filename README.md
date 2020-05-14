# cessda.cmv.server

## Endpoints
 * [REST API](http://localhost:8080/)
 * [HTTP GET /hello-world](http://localhost:8080/hello-world)
 * [HTTP GET /actuator](http://localhost:8080/actuator)
 * [HTTP GET /actuator/health](http://localhost:8080/actuator/health)
 * [HTTP GET /actuator/logfile](http://localhost:8080/actuator/logfile)

## Getting started as developer

* Execute tests and run the application

```
# Execute all tests locally with default config
mvn clean test

# Run the app locally with default config and pre-populated database
mvn clean spring-boot:run
# Browse to http://localhost:8080 
# Stop the app with Ctrl+C
```

* Create and run service environment with docker-compose

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
