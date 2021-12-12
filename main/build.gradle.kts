plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencies {

    setOf("io.github.pulsebeat02:emc-dependency-management:v1.0.0").forEach {
        implementation(it)
    }

    setOf("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT").forEach {
        compileOnlyApi(it)
    }

    //////// TESTING ////////

    setOf("org.junit.jupiter:junit-jupiter-engine:5.4.2").forEach {
        testRuntimeOnly(it);
    }

    setOf(
        "org.junit.jupiter:junit-jupiter-api:5.4.2",
        "com.github.seeseemelk:MockBukkit-v1.17:1.7.0",
        "org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT"
    ).forEach {
        testImplementation(it)
    }

    /////// PROVIDED ////////

    setOf(
        "io.netty:netty-all:4.1.70.Final",
        "com.mojang:authlib:1.5.26",
        "com.google.guava:guava:30.1.1-jre",
        "com.mpatric:mp3agic:0.9.1",
        "com.github.kevinsawicki:http-request:6.0",
    ).forEach {
        "compileOnly"(it)
        testImplementation(it)
    }

    ///////// MAIN DEPS /////////
    setOf(
        "uk.co.caprica:vlcj:4.7.1",
        "uk.co.caprica:vlcj-natives:4.5.0",
        "com.github.sealedtx:java-youtube-downloader:3.0.2",
        "com.alibaba:fastjson:1.2.78",
        "net.java.dev.jna:jna:5.10.0",
        "net.java.dev.jna:jna-platform:5.10.0",
        "se.michaelthelin.spotify:spotify-web-api-java:7.0.0",
        "com.github.kokorin.jaffree:jaffree:2021.11.06",
        "org.jcodec:jcodec:0.2.5",
        "com.github.ben-manes.caffeine:caffeine:3.0.5",
        "io.github.pulsebeat02:emc-installers:v1.0.1",
        "it.unimi.dsi:fastutil:8.5.6",
        "com.fasterxml.jackson.core:jackson-core:2.13.0",
        "org.apache.httpcomponents.client5:httpclient5:5.2-alpha1",
        "com.neovisionaries:nv-i18n:1.29"
    ).forEach {
        compileOnly(it)
        testImplementation(it)
    }

    setOf(
        project(":api"),
        project(":v1_16_R3"),
        project(":v1_17_R1"),
        project(":v1_18_R1")
    ).forEach {
        api(it)
    }
}

tasks {
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
    }
}