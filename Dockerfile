FROM wire/bots.runtime:latest

COPY target/don.jar      /opt/don/don.jar

WORKDIR /opt/don

