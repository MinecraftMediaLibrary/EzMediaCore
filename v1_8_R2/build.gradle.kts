description = "v1_8_R2"

plugins {
    id("com.github.pulsebeat02.java-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.8.3-R0.1-SNAPSHOT")
    compileOnly(project(":minecraftmedialibrary-api"))
}