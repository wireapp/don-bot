#!/usr/bin/env bash
docker build -t $DOCKER_USERNAME/don-bot:1.3.0 .
docker push $DOCKER_USERNAME/don-bot
kubectl delete pod -l name=don -n prod
kubectl get pods -l name=don  -n prod
