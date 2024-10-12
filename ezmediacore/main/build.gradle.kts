plugins {
    id("com.gradleup.shadow") version "8.3.3"
}

dependencies {

    implementation(project(":ezmediacore:v1_19_R2"))
    implementation(project(":ezmediacore:nms-api"))

    // Project dependencies
    compileOnly("org.bytedeco:javacv-platform:1.5.10")
    compileOnly("org.jsoup:jsoup:1.18.1")
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.1.2")
    compileOnly("uk.co.caprica:vlcj:4.8.2")
    compileOnly("uk.co.caprica:vlcj-natives:4.8.1")
    compileOnly("com.github.sealedtx:java-youtube-downloader:3.2.6")
    compileOnly("net.java.dev.jna:jna:5.13.0")
    compileOnly("net.java.dev.jna:jna-platform:5.13.0")
    compileOnly("se.michaelthelin.spotify:spotify-web-api-java:7.3.0")
    compileOnly("com.github.kokorin.jaffree:jaffree:2022.06.03")
    compileOnly("org.jcodec:jcodec:0.2.5")

    // Provided dependencies
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.1.87.Final")
    compileOnly("com.mojang:authlib:3.16.29")
    compileOnly("com.google.guava:guava:31.1-jre")
    compileOnly("it.unimi.dsi:fastutil:8.5.11")

    testImplementation("uk.co.caprica:vlcj:4.8.2")
    testImplementation("uk.co.caprica:vlcj-natives:4.8.1")
    testImplementation("com.github.kokorin.jaffree:jaffree:2022.06.03")
}

tasks {

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filteringCharset = "UTF-8"
    }

    assemble {
        dependsOn(":ezmediacore::v1_19_R2:reobfJar")
        dependsOn("shadowJar")
    }

    build {
        dependsOn("spotlessApply")
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
}