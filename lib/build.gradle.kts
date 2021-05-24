import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    `maven-publish`
}

dependencies {
    implementation(project(":main"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("MinecraftMediaLibrary")
                description.set("A Spigot library used to play media")
                url.set("https://github.com/MinecraftMediaLibrary/MinecraftMediaLibrary")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("pulsebeat02")
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
                    connection.set("scm:git:git://github.com/MinecraftMediaLibrary/MinecraftMediaLibrary.git")
                    developerConnection.set("scm:git:git://github.com/MinecraftMediaLibrary/MinecraftMediaLibrary.git")
                    url.set("https://github.com/MinecraftMediaLibrary/MinecraftMediaLibrary")
                }
            }
        }
    }

/*
      repositories {
        maven {
            url = uri(
                if (version.toString().endsWith("SNAPSHOT"))
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                else
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            )
            val ossUsername: String by project
            val ossPassword: String by project
            credentials {
                username = ossUsername
                password = ossPassword
            }
        }
    }
*/

}

tasks.withType<ShadowJar> {
    relocate("uk.co.caprica.vlcj", "com.github.pulsebeat02.vlcj")
    relocate("uk.co.caprica.vlcj.binding", "com.github.pulsebeat02.vlcj.binding")
    relocate("uk.co.caprica.nativestreams", "com.github.pulsebeat02.vlcj.nativestreams")
    relocate("com.github.kiulian.downloader", "com.github.pulsebeat02.youtube")
    relocate("ws.schild.jave", "com.github.pulsebeat02.jave")
    relocate("org.apache.commons.compress", "com.github.pulsebeat02.compress")
    relocate("org.rauschig.jarchivelib", "com.github.pulsebeat02.jarchivelib")
    relocate("org.tukaani.xz", "com.github.pulsebeat02.xz")
    relocate("org.ow2.asm", "com.github.pulsebeat02.asm")
    relocate("org.ow2.asm.commons", "com.github.pulsebeat02.asm.commons")
    relocate("org.apache.commons.io", "org.bukkit.craftbukkit.libs.org.apache.commons.io")
    relocate("com.wrapper.spotify", "com.github.pulsebeat02.spotify")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}