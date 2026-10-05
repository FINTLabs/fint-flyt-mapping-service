# FINT Skjema Mapping Service

## Running Locally

Start Kafka on `localhost:9092` with `docker compose up -d`, then run the app with `./gradlew bootRun --args='--spring.profiles.active=local-staging'`. Add `--profile tools` to also start Kafdrop on http://localhost:19000. Kafka topics are empty on every start.
