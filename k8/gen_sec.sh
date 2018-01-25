#!/bin/bash

NAME="don-knows"

AUTH_TOKEN="your_token"

kubectl delete secret $NAME
kubectl create secret generic $NAME \
    --from-literal=token=$AUTH_TOKEN