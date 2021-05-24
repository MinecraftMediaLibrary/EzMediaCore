import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

description = "deluxemediaplugin"
version = "1.4.0"

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencies {
    implementation(project(":main"))
    implementation("org.bstats:bstats-bukkit:1.8")
    implementation("com.mojang:brigadier:1.0.18")
    implementation("net.kyori:adventure-platform-bukkit:4.0.0-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:20.1.0")
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("DeluxeMediaPlugin")
    relocate("net.kyori", "com.github.pulsebeat02.deluxemediaplugin.lib.kyori")
    relocate("org.bstats", "com.github.pulsebeat02.deluxemediaplugin.lib.bstats")
    relocate("com.mojang.brigadier", "com.github.pulsebeat02.deluxemediaplugin.lib.brigadier")
    relocate("com.github.pulsebeat02.minecraftmedialibrary", "com.github.pulsebeat02.deluxemediaplugin.lib.minecraftmedialibrary")

//    relocate("uk.co.caprica.vlcj", "com.github.pulsebeat02.vlcj")
//    relocate("uk.co.caprica.vlcj.binding", "com.github.pulsebeat02.vlcj.binding")
//    relocate("uk.co.caprica.nativestreams", "com.github.pulsebeat02.vlcj.nativestreams")
//    relocate("com.github.kiulian.downloader", "com.github.pulsebeat02.youtube")
//    relocate("ws.schild.jave", "com.github.pulsebeat02.jave")
//    relocate("org.apache.commons.compress", "com.github.pulsebeat02.compress")
//    relocate("org.rauschig.jarchivelib", "com.github.pulsebeat02.jarchivelib")
//    relocate("org.tukaani.xz", "com.github.pulsebeat02.xz")
//    relocate("org.ow2.asm", "com.github.pulsebeat02.asm")
//    relocate("org.ow2.asm.commons", "com.github.pulsebeat02.asm.commons")
//    relocate("org.apache.commons.io", "org.bukkit.craftbukkit.libs.org.apache.commons.io")
//    relocate("com.wrapper.spotify", "com.github.pulsebeat02.spotify")
}

