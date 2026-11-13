plugins {
    kotlin("jvm") version "2.4.21"
    `java-library`
    `maven-publish`

    id("com.diffplug.spotless") version "8.10.3"
}

group = "io.github.duartqx"
version = providers.gradleProperty("version").orElse("SNAPSHOT")

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
    explicitApi()
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin { ktfmt() }
    kotlinGradle { ktlint() }
}
