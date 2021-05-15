description = "v1_9_R2"

repositories {
    mavenLocal()
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.9.4-R0.1-SNAPSHOT")
    compileOnly(project(":minecraftmedialibrary-api"))
}
