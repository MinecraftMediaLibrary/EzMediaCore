plugins {
    id("com.github.johnrengelman.shadow") version "7.1.0"
    `maven-publish`
    signing
}

dependencies {
    implementation(project(":main"))
}

tasks {
    publish {
        dependsOn(clean)
        dependsOn(build)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks["shadowJar"])
            groupId = "io.github.pulsebeat02"
            artifactId = "ezmediacore"
            version = "v1.0.0-ALPHA"
            pom {
                name.set("EzMediaCore")
                description.set("A professional Bukkit library used to play media")
                url.set("https://github.com/MinecraftMediaLibrary/EzMediaCore")
                inceptionYear.set("2021")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("PulseBeat02")
                        name.set("Brandon Li")
                    }
                    developer {
                        id.set("emilyy-dev")
                        name.set("Emily")
                    }
                    developer {
                        id.set("Conclure")
                        name.set("Conclure")
                    }
                    developer {
                        id.set("itxfrosty")
                        name.set("itxfrosty")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/MinecraftMediaLibrary/EzMediaCore.git")
                    developerConnection.set("scm:git:https://github.com/MinecraftMediaLibrary/EzMediaCore.git")
                    url.set("https://github.com/MinecraftMediaLibrary/EzMediaCore")
                }
            }
        }
    }
    repositories {
        maven {
            setUrl("https://pulsebeat02.jfrog.io/artifactory/pulse-gradle-release-local/")
            credentials {
                username = ""
                password = ""
            }
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}