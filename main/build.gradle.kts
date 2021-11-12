dependencies {

    "implementation"("io.github.slimjar:slimjar:1.2.6")

    setOf("com.mpatric:mp3agic:0.9.1", "com.github.kevinsawicki:http-request:6.0").forEach {
        "testImplementation"(it)
    }

    "compileOnlyApi"("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")

    setOf(
        "uk.co.caprica:vlcj:4.7.1",
        "uk.co.caprica:vlcj-natives:4.5.0",
        "com.github.sealedtx:java-youtube-downloader:3.0.1",
        "io.netty:netty-all:5.0.0.Alpha2",
        "com.mojang:authlib:1.5.25",
        "org.tukaani:xz:1.9",
        "com.alibaba:fastjson:1.2.78",
        "net.java.dev.jna:jna:5.9.0",
        "net.java.dev.jna:jna-platform:5.9.0",
        "se.michaelthelin.spotify:spotify-web-api-java:6.5.4",
        "com.github.kokorin.jaffree:jaffree:2021.08.31",
        "com.google.guava:guava:30.1.1-jre",
        "org.apache.commons:commons-lang3:3.12.0",
        "org.apache.commons:commons-lang3:3.12.0",
        "org.jcodec:jcodec:0.2.5",
        "com.github.ben-manes.caffeine:caffeine:3.0.3",
        "com.github.MinecraftMediaLibrary:jarchivelib:v1.4.0",
        "com.github.MinecraftMediaLibrary:emc-installers:v1.0.1"
    ).forEach {
        "compileOnly"(it)
        "testImplementation"(it)
    }

    setOf(project(":api"), project(":v1_16_R3"), project(":v1_17_R1")).forEach {
        "api"(it)
    }
}