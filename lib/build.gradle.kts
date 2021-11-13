plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    `maven-publish`
    signing
}

dependencies {
    "implementation"(project(":main"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks["shadowJar"])
            pom {
                name.set("EzMediaCore")
                description.set("A Spigot library used to play media")
                url.set("https://github.com/MinecraftMediaLibrary/EzMediaCore")
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
            url = if (version.toString()
                    .endsWith("SNAPSHOT")
            ) uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") else uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username = System.getenv("OSSRH_USER")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
    signing {
        val publishing: PublishingExtension by project
        useInMemoryPgpKeys(
            System.getenv("SIGNING_KEY") ?: return@signing,
            System.getenv("SIGNING_PASSWORD") ?: return@signing
        )
        sign(publishing.publications)
    }
}

task<Wrapper>("wrapper") {
    gradleVersion = "7.1.1"
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}