FROM maven:3.6-jdk-13
WORKDIR /build
ADD . ./
RUN mvn -s .docker/settings.xml --batch-mode -P r3ktm8 clean package

FROM openjdk:12
ENV SERVER_PORT=8080
EXPOSE 8080
WORKDIR /data
COPY --from=0 /build/authentication-server-core/target/*.jar /app/service.jar
ENTRYPOINT ["java", "-jar", "/app/service.jar"]