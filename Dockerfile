FROM maven:3.6.3-jdk-8-slim AS build
LABEL description="Wire Don-Bot"
LABEL project="wire-bot:don"

WORKDIR /app

# download dependencies
COPY pom.xml ./
RUN mvn verify --fail-never -U

# build
COPY src ./src
RUN mvn package -DskipTests=true

# runtime stage
FROM wirebot/runtime:1.1.1

WORKDIR /opt/don

# Copy configuration
COPY don.yaml /etc/don/

# Copy built target
COPY --from=build /app/target/don.jar /opt/don/

# create version file
ARG release_version=development
ENV RELEASE_FILE_PATH=/etc/don/release.txt
RUN echo $release_version > $RELEASE_FILE_PATH

EXPOSE  8080 8081 8082
ENTRYPOINT ["java","-javaagent:/opt/wire/lib/prometheus-agent.jar=8082:/opt/wire/lib/metrics.yaml", "-jar", "don.jar", "server", "/etc/don/don.yaml"]
