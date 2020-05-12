FROM openjdk:13-buster
ENV SERVER_PORT=8080
EXPOSE 8080
RUN apt-get update && apt-get install -y openssl && rm -rf /var/lib/apt/lists/*
WORKDIR /data
COPY authentication-server-core/target/*.jar /app/service.jar
ENTRYPOINT ["java", "-jar", "/app/service.jar"]