description = "v1_14_R1"

plugins {
    id("com.github.pulsebeat02.java-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.14.4-R0.1-SNAPSHOT")
    compileOnly(project(":minecraftmedialibrary-api"))
}