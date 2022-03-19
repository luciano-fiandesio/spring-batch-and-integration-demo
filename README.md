# Sample Spring Batch + Spring Integration app

A simple Spring Boot app to showcase a basic integration between Spring Integration
and Spring Batch.

The app parses a CSV file (see `sample-data` folder) and emit a Kafka message for each line in the file

# How to use

Start the app and paste the file in `sample-data` folder in the `data/inbound` folder.

The app expects a Kafka cluster to run on port 29092 
(please use the included docker compose file to spin a Kafka instance).

