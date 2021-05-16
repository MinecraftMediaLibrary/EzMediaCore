description = "v1_11_R1"

repositories {
    mavenLocal()
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.11.2-R0.1-SNAPSHOT")
    compileOnly(project(":minecraftmedialibrary-api"))
}
