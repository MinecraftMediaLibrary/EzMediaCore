import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

description = "deluxemediaplugin"
version = "1.4.0"

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    maven("https://repo.mattstudios.me/artifactory/public/")
    maven("https://libraries.minecraft.net/")
}

dependencies {
    implementation(project(":main"))
    implementation(project(":lib"))
    implementation("org.bstats:bstats-bukkit:1.8")
    implementation("com.mojang:brigadier:1.0.18")
    implementation("net.kyori:adventure-platform-bukkit:4.0.0-SNAPSHOT")
    implementation("dev.triumphteam:triumph-gui:3.0.0-SNAPSHOT")
    implementation("me.lucko:commodore:1.9")
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:20.1.0")
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("DeluxeMediaPlugin")
    relocate("net.kyori", "io.github.pulsebeat02.deluxemediaplugin.lib.kyori")
    relocate("org.bstats", "io.github.pulsebeat02.deluxemediaplugin.lib.bstats")
    relocate("com.mojang.brigadier", "io.github.pulsebeat02.deluxemediaplugin.lib.brigadier")
    relocate(
        "io.github.pulsebeat02.minecraftmedialibrary",
        "io.github.pulsebeat02.deluxemediaplugin.lib.minecraftmedialibrary"
    )
    relocate("dev.triumphteam.gui", "io.github.pulsebeat02.deluxemediaplugin.lib.gui")
    relocate("me.mattstudios.util", "io.github.pulsebeat02.deluxemediaplugin.lib.util")
    relocate("me.lucko.commodore", "io.github.pulsebeat02.deluxemediaplugin.lib.commodore")

    relocate("uk.co.caprica.vlcj", "io.github.pulsebeat02.vlcj")
    relocate("uk.co.caprica.vlcj.binding", "io.github.pulsebeat02.vlcj.binding")
    relocate("uk.co.caprica.nativestreams", "io.github.pulsebeat02.vlcj.nativestreams")
    relocate("com.github.kiulian.downloader", "io.github.pulsebeat02.youtube")
    relocate("ws.schild.jave", "io.github.pulsebeat02.jave")
    relocate("org.apache.commons.compress", "io.github.pulsebeat02.compress")
    relocate("org.rauschig.jarchivelib", "io.github.pulsebeat02.jarchivelib")
    relocate("org.tukaani.xz", "io.github.pulsebeat02.xz")
    relocate("org.apache.commons.io", "org.bukkit.craftbukkit.libs.org.apache.commons.io")
    relocate("com.wrapper.spotify", "io.github.pulsebeat02.spotify")
    relocate("com.github.kokorin", "io.github.pulsebeat02.kokorin")
    relocate("io.github.slimjar", "io.github.pulsebeat02.slimjar")
}

