description = "v1_9_R1"

plugins {
    id("com.github.pulsebeat02.java-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.9.2-R0.1-SNAPSHOT")
    compileOnly(project(":minecraftmedialibrary-api"))
}


