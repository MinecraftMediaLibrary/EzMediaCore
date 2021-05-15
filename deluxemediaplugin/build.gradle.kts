import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

description = "deluxemediaplugin"
version = "RELEASE-1.4.0"

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencies {
    implementation(project(":minecraftmedialibrary"))
    implementation("org.bstats:bstats-bukkit:1.8")
    implementation("com.mojang:brigadier:1.0.18")
    implementation("net.kyori:adventure-platform-bukkit:4.0.0-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:20.1.0")
}

tasks.withType<ShadowJar> {
    relocate("net.kyori", "com.github.pulsebeat02.deluxemediaplugin.lib.kyori")
    relocate("org.bstats", "com.github.pulsebeat02.deluxemediaplugin.lib.bstats")
    relocate("com.mojang.brigadier", "com.github.pulsebeat02.deluxemediaplugin.lib.brigadier")
}
