FROM openjdk:17-jdk-slim

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8181

ENTRYPOINT ["java", "-jar", "app.jar","--spring.profiles.active=docker"]
