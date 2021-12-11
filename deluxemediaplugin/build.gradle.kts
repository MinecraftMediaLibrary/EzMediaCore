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

    setOf("com.mojang:authlib:1.5.26").forEach {
        compileOnly(it)
    }

    setOf(
        "org.bstats:bstats-bukkit:2.2.1",
        "com.mojang:brigadier:1.0.18",
        "net.kyori:adventure-api:4.9.3",
        "net.kyori:adventure-platform-bukkit:4.0.0",
        "com.github.stefvanschie.inventoryframework:IF:0.10.3",
        "me.lucko:commodore:1.11",
        "net.dv8tion:JDA:4.3.0_310",
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

        relocate("net.kyori", "io.github.pulsebeat02.deluxemediaplugin.lib.kyori")
        relocate("org.bstats", "io.github.pulsebeat02.deluxemediaplugin.lib.bstats")
        relocate("com.mojang.brigadier", "io.github.pulsebeat02.deluxemediaplugin.lib.brigadier")
        relocate(
            "com.github.stefvanschie.inventoryframework",
            "io.github.pulsebeat02.deluxemediaplugin.lib.inventoryframework"
        )
        relocate("me.mattstudios.util", "io.github.pulsebeat02.deluxemediaplugin.lib.util")
        relocate("me.lucko.commodore", "io.github.pulsebeat02.deluxemediaplugin.lib.commodore")

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
