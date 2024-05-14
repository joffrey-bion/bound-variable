plugins {
    kotlin("jvm") version "2.0.0-RC2"
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass = "org.hildan.boundvariable.decryptor.CodexDecryptorKt"
}

dependencies {
    implementation(project(":universal-machine"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.test {
    useJUnitPlatform()
}