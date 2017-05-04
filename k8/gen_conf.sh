#!/bin/bash

NAME="don-config"

kubectl delete configmap $NAME
kubectl create configmap $NAME --from-file=../conf