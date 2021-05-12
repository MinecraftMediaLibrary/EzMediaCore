description = "v1_12_R1"

plugins {
    id("com.github.pulsebeat02.java-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.12.2-R0.1-SNAPSHOT")
    compileOnly(project(":minecraftmedialibrary-api"))
}

