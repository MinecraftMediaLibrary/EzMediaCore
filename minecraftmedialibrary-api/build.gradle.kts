description = "minecraftmedialibrary-api"

plugins {
    id("com.github.pulsebeat02.java-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("uk.co.caprica:vlcj:4.7.1")
    compileOnly("uk.co.caprica:vlcj-natives:4.1.0")
    compileOnly("uk.co.caprica:native-streams:1.0.0")
    compileOnly("com.github.sealedtx:java-youtube-downloader:2.5.2")
    compileOnly("ws.schild:jave-core:3.0.1")
    compileOnly("io.netty:netty-all:5.0.0.Alpha2")
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.jetbrains:annotations:20.1.0")
    compileOnly("org.ow2.asm:asm:9.1")
    compileOnly("org.ow2.asm:asm-commons:9.1")
    compileOnly("com.github.pulsebeat02:jarchivelib:master-SNAPSHOT")
    compileOnly("org.tukaani:xz:1.0")
    compileOnly("com.alibaba:fastjson:1.2.73")
    compileOnly("net.java.dev.jna:jna:5.7.0")
    compileOnly("org.bytedeco:javacv-platform:1.5.5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("io.github.glytching:junit-extensions:2.4.0")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.31")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.4.31")
}