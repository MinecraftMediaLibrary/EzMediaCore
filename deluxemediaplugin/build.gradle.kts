plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencies {

    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")

    setOf(
        project(":api"),
        project(":main"),
        project(":lib")
    ).forEach {
        implementation(it)
    }

    setOf(
        "org.bstats:bstats-bukkit:2.2.1",
        "com.mojang:authlib:1.5.26",
        "net.dv8tion:JDA:5.0.0-alpha.2",
        "me.lucko:commodore:1.11",
        "com.github.stefvanschie.inventoryframework:IF:0.10.3"
    ).forEach {
        compileOnly(it)
    }

    setOf(
        "com.mojang:brigadier:1.0.18",
        "net.kyori:adventure-api:4.9.3",
        "net.kyori:adventure-platform-bukkit:4.0.0",
        "com.sedmelluq:lavaplayer:1.3.78"
    ).forEach {
        implementation(it)
    }
}

tasks {

    build {
        finalizedBy(shadowJar)
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        rename("plugin.json", "plugin.yml")
    }

    shadowJar {
        archiveBaseName.set("DeluxeMediaPlugin")
        relocate("org", "io.github.pulsebeat02.deluxemediaplugin.lib.org")
        relocate("me", "io.github.pulsebeat02.deluxemediaplugin.lib.me")
        relocate("com", "io.github.pulsebeat02.deluxemediaplugin.lib.com")
        relocate("net", "io.github.pulsebeat02.deluxemediaplugin.lib.net")

        relocate("uk.co.caprica.vlcj", "io.github.pulsebeat02.ezmediacore.lib.vlcj")
        relocate("com.github.kiulian.downloader", "io.github.pulsebeat02.ezmediacore.lib.youtube")
        relocate("com.wrapper.spotify", "io.github.pulsebeat02.ezmediacore.lib.spotify")
        relocate("com.github.kokorin", "io.github.pulsebeat02.ezmediacore.lib.kokorin")
        relocate("org.jcodec", "io.github.pulsebeat02.ezmediacore.lib.jcodec")
        relocate("com.github.benmanes.caffeine", "io.github.pulsebeat02.ezmediacore.lib.caffeine")
        relocate("it.unimi.dsi.fastutil", "io.github.pulsebeat02.ezmediacore.lib.fastutil")
        relocate("com.alilbaba.fastjson", "io.github.pulsebeat02.ezmediacore.lib.fastjson")
        relocate("net.sourceforge.jaad.aac", "io.github.pulsebeat02.ezmediacore.lib.aac")
    }

}
