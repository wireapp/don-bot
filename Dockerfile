FROM docker.io/maven AS build-env

WORKDIR /app

COPY pom.xml ./

RUN mvn verify --fail-never -U

COPY . ./

RUN mvn -Dmaven.test.skip=true package

FROM dejankovacevic/bots.runtime:2.10.3

COPY --from=build-env /app/target/don.jar /opt/don/

COPY don.yaml /etc/don/don.yaml

# create version file
ARG release_version=development
ENV RELEASE_FILE_PATH=/opt/don/release.txt
RUN echo $release_version > $RELEASE_FILE_PATH

WORKDIR /opt/don

EXPOSE  8080 8081 8082

ENTRYPOINT ["java", "-jar", "don.jar", "server", "/etc/don/don.yaml"]

