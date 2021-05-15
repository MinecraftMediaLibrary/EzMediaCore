import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

description = "minecraftmedialibrary"

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencies {
    api(project(":minecraftmedialibrary-api"))
    implementation(project(":minecraftmedialibrary-nms"))
}

tasks.withType<ShadowJar> {
    relocate("uk.co.caprica.vlcj", "com.github.pulsebeat02.vlcj")
    relocate("uk.co.caprica.vlcj.binding", "com.github.pulsebeat02.vlcj.binding")
    relocate("uk.co.caprica.nativestreams", "com.github.pulsebeat02.vlcj.nativestreams")
    relocate("com.github.kiulian.downloader", "com.github.pulsebeat02.youtube")
    relocate("ws.schild.jave", "com.github.pulsebeat02.jave")
    relocate("org.apache.commons.compress", "com.github.pulsebeat02.compress")
    relocate("org.rauschig.jarchivelib", "com.github.pulsebeat02.jarchivelib")
    relocate("org.tukaani.xz", "com.github.pulsebeat02.xz")
    relocate("org.ow2.asm", "com.github.pulsebeat02.asm")
    relocate("org.ow2.asm.commons", "com.github.pulsebeat02.asm.commons")
    relocate("org.apache.commons.io", "org.bukkit.craftbukkit.libs.org.apache.commons.io")
}
