import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    `maven-publish`
    signing
}

dependencies {
    implementation(project(":main"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks["shadowJar"])
            pom {
                name.set("EpicMediaLib")
                description.set("A Spigot library used to play media")
                url.set("https://github.com/MinecraftMediaLibrary/EpicMediaLib")
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
                    connection.set("scm:git:https://github.com/MinecraftMediaLibrary/EpicMediaLib.git")
                    developerConnection.set("scm:git:https://github.com/MinecraftMediaLibrary/EpicMediaLib.git")
                    url.set("https://github.com/MinecraftMediaLibrary/EpicMediaLib")
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

tasks.withType<ShadowJar> {
    relocate("uk.co.caprica.vlcj", "io.github.pulsebeat02.epicmedialib.lib.vlcj")
    relocate("uk.co.caprica.vlcj.binding", "io.github.pulsebeat02.epicmedialib.lib.vlcj.binding")
    relocate("uk.co.caprica.nativestreams", "io.github.pulsebeat02.epicmedialib.lib.vlcj.nativestreams")
    relocate("com.github.kiulian.downloader", "io.github.pulsebeat02.epicmedialib.lib.youtube")
    relocate("ws.schild.jave", "io.github.pulsebeat02.epicmedialib.lib.jave")
    relocate("org.apache.commons.compress", "io.github.pulsebeat02.epicmedialib.lib.compress")
    relocate("org.rauschig.jarchivelib", "io.github.pulsebeat02.epicmedialib.lib.jarchivelib")
    relocate("org.tukaani.xz", "io.github.pulsebeat02.epicmedialib.lib.xz")
    relocate("org.apache.commons.io", "org.bukkit.craftbukkit.libs.org.apache.commons.io")
    relocate("com.wrapper.spotify", "io.github.pulsebeat02.epicmedialib.lib.spotify")
    relocate("com.github.kokorin", "io.github.pulsebeat02.epicmedialib.lib.kokorin")
    relocate("io.github.slimjar", "io.github.pulsebeat02.epicmedialib.lib.slimjar")
    relocate("org.jcodec", "io.github.pulsebeat02.epicmedialib.lib.jcodec")
}

task<Wrapper>("wrapper") {
    gradleVersion = "7.1.1";
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}