description = "v1_16_R2"

plugins {
    id("com.github.pulsebeat02.java-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.16.3-R0.1-SNAPSHOT")
    compileOnly(project(":minecraftmedialibrary-api"))
}