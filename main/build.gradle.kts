dependencies {

    compileOnlyApi("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    implementation("io.github.slimjar:slimjar:1.2.4")
    compileOnly("uk.co.caprica:vlcj:4.7.1")
    compileOnly("uk.co.caprica:vlcj-natives:4.5.0")
    compileOnly("uk.co.caprica:native-streams:2.0.0")
    compileOnly("com.github.sealedtx:java-youtube-downloader:3.0.1")
    compileOnly("ws.schild:jave-core:3.1.1")
    compileOnly("io.netty:netty-all:5.0.0.Alpha2")
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.jetbrains:annotations:22.0.0")
    compileOnly("com.github.pulsebeat02:jarchivelib:master-SNAPSHOT")
    compileOnly("org.tukaani:xz:1.9")
    compileOnly("com.alibaba:fastjson:1.2.78")
    compileOnly("net.java.dev.jna:jna:5.9.0")
    compileOnly("net.java.dev.jna:jna-platform:5.9.0")
    compileOnly("se.michaelthelin.spotify:spotify-web-api-java:6.5.4")
    compileOnly("com.github.kokorin.jaffree:jaffree:2021.08.31")
    compileOnly("com.google.guava:guava:30.1.1-jre")
    compileOnly("org.apache.commons:commons-lang3:3.12.0")
    compileOnly("org.jcodec:jcodec:0.2.5")
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.0.3")
    compileOnly("net.dv8tion:JDA:4.3.0_277")
    compileOnly("net.sf.trove4j:trove4j:3.0.3")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.13.0-rc2")
    compileOnly("com.sedmelluq:lavaplayer:1.3.78")
    compileOnly("org.jsoup:jsoup:1.14.2")
    compileOnly("net.iharder:base64:2.3.9")

    testImplementation("uk.co.caprica:vlcj:4.7.1")
    testImplementation("uk.co.caprica:vlcj-natives:4.5.0")
    testImplementation("uk.co.caprica:native-streams:2.0.0")
    testImplementation("com.github.sealedtx:java-youtube-downloader:3.0.1")
    testImplementation("ws.schild:jave-core:3.1.1")
    testImplementation("io.netty:netty-all:5.0.0.Alpha2")
    testImplementation("com.mojang:authlib:1.5.25")
    testImplementation("org.jetbrains:annotations:22.0.0")
    testImplementation("com.github.pulsebeat02:jarchivelib:master-SNAPSHOT")
    testImplementation("org.tukaani:xz:1.9")
    testImplementation("com.alibaba:fastjson:1.2.78")
    testImplementation("net.java.dev.jna:jna:5.9.0")
    testImplementation("net.java.dev.jna:jna-platform:5.9.0")
    testImplementation("se.michaelthelin.spotify:spotify-web-api-java:6.5.4")
    testImplementation("com.github.kokorin.jaffree:jaffree:2021.08.31")
    testImplementation("com.google.guava:guava:30.1.1-jre")
    testImplementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("org.jcodec:jcodec:0.2.5")
    testImplementation("com.github.ben-manes.caffeine:caffeine:3.0.3")
    testImplementation("net.dv8tion:JDA:4.3.0_277")
    testImplementation("net.sf.trove4j:trove4j:3.0.3")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.13.0-rc2")
    testImplementation("com.sedmelluq:lavaplayer:1.3.78")
    testImplementation("org.jsoup:jsoup:1.14.2")
    testImplementation("net.iharder:base64:2.3.9")

    testImplementation("com.mpatric:mp3agic:0.9.1")
    testImplementation("com.github.kevinsawicki:http-request:6.0")

    api(project(":api"))
    api(project(":v1_16_R3"))
    api(project(":v1_17_R1"))

}