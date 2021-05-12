description = "v1_10_R1"

plugins {
    id("com.github.pulsebeat02.java-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.10.2-R0.1-SNAPSHOT")
    compileOnly(project(":minecraftmedialibrary-api"))
}