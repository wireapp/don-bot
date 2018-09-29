FROM dejankovacevic/bots.runtime:2.10.2

COPY target/don.jar      /opt/don/don.jar
COPY don.yaml            /etc/don/don.yaml

WORKDIR /opt/don

EXPOSE  8080 8081 8082
