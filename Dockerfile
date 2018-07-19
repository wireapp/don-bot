FROM dejankovacevic/bots.runtime:2.10.0

COPY target/don.jar      /opt/don/don.jar
COPY don.yaml            /etc/don/don.yaml

WORKDIR /opt/don

