FROM openjdk:17-jdk-slim
COPY target/telemedicine-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app.jar"]
