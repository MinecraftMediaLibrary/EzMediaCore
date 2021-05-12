description = "v1_11_R1"

plugins {
    id("com.github.pulsebeat02.java-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.11.2-R0.1-SNAPSHOT")
    compileOnly(project(":minecraftmedialibrary-api"))
}