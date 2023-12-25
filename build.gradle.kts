plugins {
    kotlin("jvm") version "2.0.0-Beta1"
    kotlin("plugin.serialization") version "2.0.0-Beta1"
    `maven-publish`
}

group = "smartexposed"
version = "0.7"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

kotlin {
    jvmToolchain(8)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "smartExposedKt"
            version = project.version.toString()
            groupId = "smartexposed"

            from(components["kotlin"])
        }
    }
}