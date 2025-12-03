#!/bin/bash

# Assume redis is up and running
cd persistence-framework
mvn clean install
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=target/annotations-1.0-SNAPSHOT.jar
cd ..

cd microservices
mvn clean install
mvn exec:java &
cd ..

sleep 20 # Wait for microservices to start

cd main-app
mvn clean install
mvn exec:java
cd ..
