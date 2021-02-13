
plugins {
    id("com.github.pulsebeat02.java-conventions")
}

dependencies {
    implementation(project(":MinecraftMediaLibrary-Final"))
    implementation("org.bstats:bstats-bukkit:1.8")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:16.0.1")
}

description = "DeluxeMediaPlugin"
