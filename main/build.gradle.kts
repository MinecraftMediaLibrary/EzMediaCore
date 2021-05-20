import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

description = "minecraftmedialibrary"

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    `maven-publish`
}

dependencies {
    compileOnlyApi("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    api("uk.co.caprica:vlcj:4.7.1")
    api("uk.co.caprica:vlcj-natives:4.1.0")
    api("uk.co.caprica:native-streams:1.0.0")
    api("com.github.sealedtx:java-youtube-downloader:2.5.2")
    api("ws.schild:jave-core:3.0.1")
    api("io.netty:netty-all:5.0.0.Alpha2")
    api("com.mojang:authlib:1.5.25")
    api("org.jetbrains:annotations:20.1.0")
    api("org.ow2.asm:asm:9.1")
    api("org.ow2.asm:asm-commons:9.1")
    api("com.github.pulsebeat02:jarchivelib:master-SNAPSHOT")
    api("org.tukaani:xz:1.0")
    api("com.alibaba:fastjson:1.2.73")
    api("net.java.dev.jna:jna:5.8.0")
    api("net.java.dev.jna:jna-platform:5.8.0")
    api("org.bytedeco:javacv-platform:1.5.5")
    api("se.michaelthelin.spotify:spotify-web-api-java:6.5.4")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("io.github.glytching:junit-extensions:2.4.0")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.31")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.4.31")

    api(project(":api"))
    api(project(":v1_16_R3"))
    api(project(":v1_16_R2"))
    api(project(":v1_16_R1"))
    api(project(":v1_15_R1"))
    api(project(":v1_14_R1"))
    api(project(":v1_13_R2"))
    //api(project(":v1_13_R1"))
    api(project(":v1_12_R1"))
    //api(project(":v1_11_R1"))
    //api(project(":v1_10_R1"))
    //api(project(":v1_9_R2"))
    //api(project(":v1_9_R1"))
    api(project(":v1_8_R3"))
    //api(project(":v1_8_R2"))
    //api(project(":v1_8_R1"))
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
