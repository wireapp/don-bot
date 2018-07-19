#!/usr/bin/env bash
mvn package -DskipTests=true -Dmaven.javadoc.skip=true
docker build -t dejankovacevic/don-bot:latest .
docker push dejankovacevic/don-bot
kubectl delete pod -l name=don -n prod
kubectl get pods -l name=don -n prod
