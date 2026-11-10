# fint-flyt-mapping-service

Maps instances in FLYT to the format of the destination. It consumes `instance-registered` and
`instance-requested-for-retry`, maps the instance with the active configuration of the integration,
and publishes `instance-mapped`. On failure it publishes `instance-error`. See
[docs/architecture.md](docs/architecture.md).

## intgr-conf-service

The configuration, mapping and value convertings come from `/api/intern-klient` in
fint-flyt-intgr-conf-service, with a token from the OAuth client `fint-flyt-intgr-conf-oauth2-client`.

- A 404 gives an error event, the same as a missing value.
- Other errors and timeouts make the Kafka consumer retry the record with exponential backoff.
  When the retries run out, it publishes `GENERAL_SYSTEM_ERROR`.
- Each overlay sets `novari.flyt.mapping-service.intgr-conf-client.base-url`, because the context
  path of intgr-conf-service differs per environment (`/afk-no`, `/beta/afk-no`).

## Running locally

1. `docker compose up -d` starts Kafka on `localhost:9092`. Add `--profile tools` for Kafdrop on
   http://localhost:19000.
2. Start fint-flyt-intgr-conf-service on `localhost:8095`.
3. `./gradlew bootRun --args='--spring.profiles.active=local-staging'`

Calls to intgr-conf-service need real values in `fint.sso.client-id` and `fint.sso.client-secret`.

## Build

Run `./gradlew check`. `test` does not run ktlint.

## Dependency locking

Every configuration is locked in `gradle.lockfile`. Dependabot updates the lockfile itself; if two of
its pull requests conflict, comment `@dependabot recreate`. After any manual change to dependencies,
versions or constraints, run `./gradlew dependencies --write-locks` and commit the lockfile. Gradle
plugins are not locked and are pinned in the `plugins` block.
