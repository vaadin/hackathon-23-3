# Hackathon-23-3

Using Observability Kit with SigNoz – a open-source observability tool that can be run on your own infora (localhost in this case).

## Running the application

### SigNoz
Prerequisites: Docker Desktop.

The project has a docker compose to start Signoz on your local machine.

`docker-compose -f docker/clickhouse-setup/docker-compose.yaml up -d`

Then browse to http://localhost:3301/  and create an account (stored in the `data` folder, persists over restarts).

For not SigNoz is empty – let's start the application.

### Demo application

`./mwnv package -Pproduction`

then the monster 

`java -javaagent:/PATH/TO/vaadin-opentelemetry-javaagent-1.0.0.rc2.jar -Dotel.javaagent.configuration-file=/PATH/TO/agent.properties -jar /PATH/TO/hackathon-23-3-1.0-SNAPSHOT.jar`

Browse to http://localhost:8080/

### Producing data
When you click around in the application, data starts showing up in SigNoz (after a delay) under Services/Vaadin.

1. Try editing a person that starts with "J" in http://localhost:8080/master-detail -> slow, and you should be able to find which methid is slow in SigNoz.
2. Cause OptimisticLocking exception by editing the same entity in tow windows. This causes save to do NOTHING, nothing visible in the UI and it does NOT save, BUT SigNoz knows the cause – imagine a user calling you complaining "nothing works" and check "Exceptions" in SigNoz.

NOTE that it can take a few second to a minute for data to appear.

## Stopping everything
You know how to stop the Vaadin application, but SigNoz can be stopped like this:

`docker-compose -f docker/clickhouse-setup/docker-compose.yaml down -v`

