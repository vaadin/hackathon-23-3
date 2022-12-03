FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dvaadin.proKey=YOUR_EMAIL/YOUR_PRO_KEY", "-jar", "/app.jar"]
