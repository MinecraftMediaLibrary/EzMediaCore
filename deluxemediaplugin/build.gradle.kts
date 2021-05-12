description = "deluxemediaplugin"
version = "RELEASE-1.4.0"

plugins {
    id("com.github.pulsebeat02.java-conventions")
}

dependencies {
    implementation(project(":minecraftmedialibrary"))
    implementation("org.bstats:bstats-bukkit:1.8")
    implementation("com.mojang:brigadier:1.0.17")
    implementation("net.kyori:adventure-platform-bukkit:4.0.0-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:16.0.1")
}


