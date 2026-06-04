plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.mohadang"
version = "0.0.1-SNAPSHOT"
description = "demoapp"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")

    //spring wrapper/integration layer for Kafka. It provides:
    //- Spring-style configuration (@EnableKafkaStreams, @KafkaListener)
    //- Dependency management (ensures kafka-streams and kafka-clients versions are compatible)
    //- Auto-configuration for Kafka producers/consumers/streams
    //- Integration with Spring's lifecycle management
    implementation("org.springframework.kafka:spring-kafka")

    // core Kafka Streams library from Apache Kafka for building stream processing applications. It provides APIs like KStream, KTable, Topology, etc.
    implementation("org.apache.kafka:kafka-streams")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
