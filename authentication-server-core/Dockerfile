FROM openjdk:12-jdk

EXPOSE 8080
ENV SERVER_PORT 8080

COPY target/authentication-server-core-1.0.0-SNAPSHOT.jar /app/service.jar
WORKDIR /data

ENTRYPOINT ["java", "-jar", "/app/service.jar"]