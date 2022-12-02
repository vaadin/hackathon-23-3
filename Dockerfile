FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
COPY agent.properties agent.properties
COPY vaadin-opentelemetry-*.jar vaadin-opentelemetry.jar
#COPY ~/.vaadin/proKey ~/.vaadin/proKey #DOES NOT WORK
EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:/vaadin-opentelemetry.jar", "-Dotel.javaagent.configuration-file=/agent.properties", "-jar", "/app.jar"]