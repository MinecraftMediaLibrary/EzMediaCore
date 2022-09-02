import org.ajoberstar.grgit.Grgit

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {

    setOf(
            "io.github.pulsebeat02:emc-dependency-management:v1.0.0",
            "io.github.pulsebeat02:emc-installers:v1.1.0",
            "io.github.pulsebeat02:native-library-loader:v1.0.2"
    ).forEach {
        implementation(it)
    }

    setOf("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT").forEach {
        compileOnlyApi(it)
    }

    setOf(
            "me.friwi:jcefmaven:100.0.14.3"
    ).forEach {
        testImplementation(it)
    }

    // PROVIDED DEPENDENCIES / TEST DEPENDENCIES
    setOf(
            "io.netty:netty-all:4.1.80.Final",
            "com.mojang:authlib:1.5.26",
            "com.google.guava:guava:31.1-jre",
            "com.mpatric:mp3agic:0.9.1",
            "com.github.kevinsawicki:http-request:6.0"
    ).forEach {
        compileOnly(it)
        testImplementation(it)
    }

    // MAIN DEPENDENCIES
    setOf(
            "uk.co.caprica:vlcj:4.7.3",
            "uk.co.caprica:vlcj-natives:4.7.0",
            "com.github.sealedtx:java-youtube-downloader:3.0.2",
            "com.alibaba:fastjson:2.0.12",
            "net.java.dev.jna:jna:5.12.1",
            "net.java.dev.jna:jna-platform:5.12.1",
            "se.michaelthelin.spotify:spotify-web-api-java:7.2.0",
            "com.github.kokorin.jaffree:jaffree:2022.06.03",
            "org.jcodec:jcodec:0.2.5",
            "com.github.ben-manes.caffeine:caffeine:3.1.1",
            "it.unimi.dsi:fastutil:8.5.8",
            "com.fasterxml.jackson.core:jackson-core:2.13.3",
            "org.apache.httpcomponents.client5:httpclient5:5.2-beta1",
            "com.neovisionaries:nv-i18n:1.29",
    ).forEach {
        compileOnly(it)
        testImplementation(it)
    }

    // PROJECT DEPENDENCIES
    setOf(
            project(":api"),
            project(":v1_18_R2"),
            project(":v1_19_R1")
    ).forEach {
        api(it)
    }
}

tasks {

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    withType<Test> {
        exclude("**/*")
        useJUnitPlatform()
    }

    shadowJar {
        val base = "io.github.pulsebeat02.ezmediacore.lib"
        relocate("uk.co.caprica", "$base.caprica")
        relocate("com.github.kiulian", "$base.kiulian")
        relocate("se.michaelthelin", "$base.michaelthelin")
        relocate("com.github.kokorin", "$base.kokorin")
        relocate("org.jcodec", "$base.jcodec")
        relocate("com.github.benmanes", "$base.benmanes")
        relocate("it.unimi.dsi", "$base.dsi")
        relocate("com.alibaba", "$base.alibaba")
        relocate("net.sourceforge.jaad.aac", "$base.sourceforge")
        relocate("com.fasterxml", "$base.fasterxml")
        relocate("org.apache", "$base.apache")
        relocate("com.neovisionaries", "$base.neovisionaries")
        minimize()
    }


    register("compileGoCode") {
        doLast {

            val goFolder = rootProject.file("go-natives")
            if (goFolder.exists()) {
                goFolder.deleteRecursively()
            }

            val xgoFolder = rootProject.file("xgo")
            if (xgoFolder.exists()) {
                xgoFolder.deleteRecursively()
            }

            Grgit.clone {
                dir = goFolder
                uri = "https://github.com/MinecraftMediaLibrary/EzMediaCore-Native-Go.git"
            }

            Grgit.clone {
                dir = xgoFolder
                uri = "https://github.com/techknowlogick/xgo.git"
            }
        }
    }
}