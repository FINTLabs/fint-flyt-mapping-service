# FINT Skjema Mapping Service

## Running Locally

Start Kafka on `localhost:9092` with `docker compose up -d`, then run the app with `./gradlew bootRun --args='--spring.profiles.active=local-staging'`. Add `--profile tools` to also start Kafdrop on http://localhost:19000. Kafka topics are empty on every start.

## Dependency Locking

The build uses [Gradle dependency locking](https://docs.gradle.org/current/userguide/dependency_locking.html), so every build resolves exactly the same direct and transitive versions. The locked versions live in the committed `*.lockfile` files: `gradle.lockfile` for project dependencies and the others for Gradle plugins. A pull request therefore shows every transitive version change it causes.

**Automatic (Dependabot):** Dependabot updates the lockfiles in its own pull requests, so no manual step is needed. If two Dependabot pull requests conflict in a lockfile, Dependabot rebases the remaining one after the first is merged. If it does not, comment `@dependabot recreate` on the pull request.

**Manual:** Any change you make by hand that affects dependency resolution requires regenerating the lockfiles. That covers versions, `extra[...]` properties, constraints, adding or removing a dependency, and plugin versions:

```bash
./gradlew dependencies buildEnvironment --write-locks
```

Commit the updated lockfiles together with the change. If you forget, the build fails both locally and in CI with an error saying the resolved version "is not part of the dependency lock state".
