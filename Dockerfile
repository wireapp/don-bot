FROM wire/bots.runtime:latest

COPY target/don.jar /opt/don/don.jar
COPY don.yaml       /etc/don/don.yaml

WORKDIR /opt/don

