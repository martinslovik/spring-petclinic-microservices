# Applying Actor Model to Increase Microservice Resilience

This project contains an implementation of the thesis experiment for increasing the resilience of `customers-service REST API` microservice with application of the actor model and various resilience patterns.
The following are the instructions of how to run the experiment with the corresponding fault injection scenarios.
Although the project supports docker containers, the experiment can be run without docker, and we will use these minimal requirements to run it successfully.

## Prerequisites
* Java 17
* Maven
* Python 3

## Versions
* Spring Boot 3.0.9
* Chaos Monkey 3.0.2
* Akka 2.5.9

## Installation
Perform the following steps from the project root directory.

1. Start the `config-server`
```
cd spring-petclinic-config-server
mvn clean install
mvn spring-boot:run
```
2. Start the `discovery-server` on a separate terminal window
```
cd spring-petclinic-discovery-server
mvn clean install
mvn spring-boot:run
```
3. Start the `customers-service` on a separate terminal window
```
cd spring-petclinic-customers-service
mvn clean install
mvn spring-boot:run
```

## Performing a request
1. Open `http://localhost:8761/` in your browser to see the Eureka dashboard.
2. Find the port where the `customers-service` is running in the dashboard.
3. Open `http://localhost:<PORT>/swagger-ui/index.html` in your browser to see the OpenAPI dashboard.
4. Perform a request to the `customers-service` using the OpenAPI dashboard.

## Running the experiment
Perform the following steps from the project root directory.

### Locust
1. Start Locust
```
cd spring-petclinic-customers-service/test
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
locust
```
2. Open `http://localhost:8089/` in your browser to see the Locust dashboard.
3. Set the number of users, spawn rate and `customers-service REST API` url in the dashboard.

### Chaos Monkey
1. On the new terminal window from the project root directory:
```
cd scripts/chaos/
```
In this directory, you can find all the assault configurations for the Chaos Monkey.

## Running the fault injection scenarios from the thesis
The following are the instructions of how to run the fault injection scenarios from the thesis.
You can observe each scenario in Locust dashboard and the logs of the `customers-service`.
To check if the Chaos Monkey is correctly configured, open your browser on `http://localhost:<PORT>/actuator/chaosmonkey` and check the response.

### FS1: Exception Assault on the OwnerActor
Ensure the locust scenario is running and then run the following command from the `scripts/chaos/` directory:
```
./call_chaos.sh <PORT> customers attacks_enable_exception watcher_enable_service
```
To stop the assault, run the following command:
```
./call_chaos.sh <PORT> customers attacks_disable watcher_disable
```

### FS2: Exception Assault on the OwnerRepository
Ensure the locust scenario is running and then run the following command from the `scripts/chaos/` directory:
```
./call_chaos.sh <PORT> customers attacks_enable_exception watcher_enable_repository
```
To stop the assault, run the following command:
```
./call_chaos.sh <PORT> customers attacks_disable watcher_disable
```

### FS3: Latency Assault on the OwnerRepository
Ensure the locust scenario is running and then run the following command from the `scripts/chaos/` directory:
```
./call_chaos.sh <PORT> customers attacks_enable_latency watcher_enable_repository
```
To stop the assault, run the following command:
```
./call_chaos.sh <PORT> customers attacks_disable watcher_disable
```
