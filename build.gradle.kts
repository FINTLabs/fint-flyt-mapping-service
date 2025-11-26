plugins {
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("java")
    id("com.github.ben-manes.versions") version "0.52.0"
}

group = "no.novari"
version = "0.0.1-SNAPSHOT"
var fintModelVersion = "3.21.10"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.jar {
    isEnabled = false
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
    mavenLocal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("no.fint:fint-arkiv-resource-model-java:$fintModelVersion")
    implementation("no.novari:flyt-resource-server:6.0.0-rc-27")
    implementation("no.novari:kafka:5.0.0-rc-21")
    implementation("no.novari:flyt-kafka:4.0.0-rc-10")

    implementation("org.apache.commons:commons-lang3:3.18.0")

    compileOnly("org.projectlombok:lombok")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.test {
    useJUnitPlatform()
}
