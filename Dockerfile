FROM openjdk:12
ENV SERVER_PORT=8080
EXPOSE 8080
WORKDIR /data
COPY authentication-server-core/target/authentication-server-core*jar /app/service.jar
ENTRYPOINT ["java", "-jar", "/app/service.jar"]