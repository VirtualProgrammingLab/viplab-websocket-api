FROM maven:3.6.3-jdk-11 AS builder

COPY ./ /src

WORKDIR /src

RUN mvn package

FROM open-liberty:kernel-java8-openj9

COPY --from=builder --chown=1001:0  /src/websocket-api-impl/target/websocket-api.war /config/dropins/
COPY --from=builder --chown=1001:0  /src/websocket-api-impl/src/main/liberty/config/server.xml /config/

RUN configure.sh
