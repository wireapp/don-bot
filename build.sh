#!/usr/bin/env bash
mvn package -DskipTests=true -Dmaven.javadoc.skip=true
docker build -t $DOCKER_USERNAME/don-bot:latest .
docker push $DOCKER_USERNAME/don-bot
kubectl delete pod -l name=don
kubectl get pods -l name=don
