#!/bin/sh
set -e

./mvnw package -Pproduction -B
docker build --tag dodotis/hackathon-23-3:1.0 .
docker push dodotis/hackathon-23-3:1.0
docker build --tag dodotis/hackathon-23-3:2.0 .
docker push dodotis/hackathon-23-3:2.0