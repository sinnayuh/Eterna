plugins {
    id("java")
    application
    id("org.jetbrains.kotlin.jvm") version("1.9.23")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "codes.sinister"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.flyte.gg/releases")
    maven("https://repo.sinister.codes/snapshots")
}

dependencies {
    implementation("org.mongodb:mongodb-driver-sync:4.9.1")
    implementation("net.dv8tion:JDA:5.0.0")
    implementation("org.jetbrains:annotations:24.0.1")
    implementation("gg.flyte:neptune:3.1-SNAPSHOT")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.github.mlgpenguin:MathEvaluator:2.1.1")
    implementation("io.github.cdimascio:dotenv-java:2.3.2")
}

application {
    mainClass.set("codes.sinister.eterna.Main")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveFileName.set("Eterna-bot.jar")
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
}