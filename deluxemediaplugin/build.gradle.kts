import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    maven("https://repo.mattstudios.me/artifactory/public/")
    maven("https://libraries.minecraft.net/")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":main"))
    implementation(project(":lib"))
    implementation("org.bstats:bstats-bukkit:2.2.1")
    implementation("com.mojang:brigadier:1.0.18")
    implementation("net.kyori:adventure-platform-bukkit:4.0.0-SNAPSHOT")
    implementation("dev.triumphteam:triumph-gui:3.0.0-SNAPSHOT")
    implementation("me.lucko:commodore:1.10")
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.spigotmc:spigot-api:1.17-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:21.0.1")
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("DeluxeMediaPlugin")
    relocate("net.kyori", "io.github.pulsebeat02.deluxemediaplugin.lib.kyori")
    relocate("org.bstats", "io.github.pulsebeat02.deluxemediaplugin.lib.bstats")
    relocate("com.mojang.brigadier", "io.github.pulsebeat02.deluxemediaplugin.lib.brigadier")
    relocate("dev.triumphteam.gui", "io.github.pulsebeat02.deluxemediaplugin.lib.gui")
    relocate("me.mattstudios.util", "io.github.pulsebeat02.deluxemediaplugin.lib.util")
    relocate("me.lucko.commodore", "io.github.pulsebeat02.deluxemediaplugin.lib.commodore")

    relocate("uk.co.caprica.vlcj", "io.github.pulsebeat02.ezmediacore.lib.vlcj")
    relocate("uk.co.caprica.vlcj.binding", "io.github.pulsebeat02.ezmediacore.lib.vlcj.binding")
    relocate("uk.co.caprica.nativestreams", "io.github.pulsebeat02.ezmediacore.lib.vlcj.nativestreams")
    relocate("com.github.kiulian.downloader", "io.github.pulsebeat02.ezmediacore.lib.youtube")
    relocate("ws.schild.jave", "io.github.pulsebeat02.ezmediacore.lib.jave")
    relocate("org.apache.commons.compress", "io.github.pulsebeat02.ezmediacore.lib.compress")
    relocate("org.rauschig.jarchivelib", "io.github.pulsebeat02.ezmediacore.lib.jarchivelib")
    relocate("org.tukaani.xz", "io.github.pulsebeat02.ezmediacore.lib.xz")
    relocate("org.apache.commons.io", "org.bukkit.craftbukkit.libs.org.apache.commons.io")
    relocate("com.wrapper.spotify", "io.github.pulsebeat02.ezmediacore.lib.spotify")
    relocate("com.github.kokorin", "io.github.pulsebeat02.ezmediacore.lib.kokorin")
    relocate("io.github.slimjar", "io.github.pulsebeat02.ezmediacore.lib.slimjar")
    relocate("org.jcodec", "io.github.pulsebeat02.ezmediacore.lib.jcodec")
}

