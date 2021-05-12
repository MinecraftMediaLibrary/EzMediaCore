description = "v1_13_R2"

plugins {
    id("com.github.pulsebeat02.java-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.13.2-R0.1-SNAPSHOT")
    compileOnly(project(":minecraftmedialibrary-api"))
}