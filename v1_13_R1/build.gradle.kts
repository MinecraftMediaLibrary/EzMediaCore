description = "v1_13_R1"

plugins {
    id("com.github.pulsebeat02.java-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.13-R0.1-SNAPSHOT")
    compileOnly(project(":minecraftmedialibrary-api"))
}