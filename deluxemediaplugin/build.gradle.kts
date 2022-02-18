plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("xyz.jpenilla.run-paper") version "1.0.6"
}

dependencies {

    compileOnly("org.spigotmc:spigot-api:1.18.1-R0.1-SNAPSHOT")

    // PROJECT DEPENDENCIES
    setOf(
        project(":lib"),
        project(":api"),
        project(":main"),
    ).forEach {
        implementation(it)
    }

    // PROVIDED DEPENDENCIES / DOWNLOADED AT RUNTIME
    setOf(
        "com.mojang:authlib:1.5.26",
        "net.dv8tion:JDA:5.0.0-alpha.5",
    ).forEach {
        compileOnly(it)
    }

    // MAIN SHADED DEPENDENCIES
    setOf(
        "org.bstats:bstats-bukkit:2.2.1",
        "net.kyori:adventure-api:4.9.3",
        "net.kyori:adventure-platform-bukkit:4.0.1",
        "com.sedmelluq:lavaplayer:1.3.78",
        "com.github.stefvanschie.inventoryframework:IF:0.10.4"
    ).forEach {
        implementation(it)
    }

    // BRIGADIER USE ONLY
    compileOnly("com.mojang:brigadier:1.0.18")
    implementation("me.lucko:commodore:1.12") {
        exclude("com.mojang", "brigadier")
    }
}

tasks {

    build {
        dependsOn(shadowJar)
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        rename("plugin.json", "plugin.yml")
    }

    shadowJar {
        archiveBaseName.set("DeluxeMediaPlugin")
        val base = "io.github.pulsebeat02.deluxemediaplugin.lib"
        relocate("org.bstats", "$base.bstats")
        relocate("me.lucko", "$base.lucko")
        relocate("net.kyori", "$base.kyori")
        relocate("com.sedmelluq", "$base.sedmelluq")
        relocate("org.jsoup", "$base.jsoup")
        relocate("org.slf4j", "$base.slf4j")
        relocate("net.iharder", "$base.iharder")
        relocate("com.github.stefvanschie", "$base.stefvanschie")

        val libraryBase = "io.github.pulsebeat02.ezmediacore.lib"
        relocate("uk.co.caprica", "$libraryBase.caprica")
        relocate("com.github.kiulian", "$libraryBase.kiulian")
        relocate("se.michaelthelin", "$libraryBase.michaelthelin")
        relocate("com.github.kokorin", "$libraryBase.kokorin")
        relocate("org.jcodec", "$libraryBase.jcodec")
        relocate("com.github.benmanes", "$libraryBase.benmanes")
        relocate("it.unimi.dsi", "$libraryBase.dsi")
        relocate("com.alibaba", "$libraryBase.alibaba")
        relocate("net.sourceforge.jaad.aac", "$libraryBase.sourceforge")
        relocate("com.fasterxml", "$libraryBase.fasterxml")
        relocate("org.apache.httpcomponents", "$libraryBase.apache.httpcomponents")
        relocate("com.neovisionaries", "$libraryBase.neovisionaries")
    }
    runServer {
       minecraftVersion.set("1.18.1")
       debug = true
    }
}