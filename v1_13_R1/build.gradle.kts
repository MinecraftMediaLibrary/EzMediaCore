description = "v1_13_R1"

repositories {
    mavenLocal()
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.13-R0.1-SNAPSHOT")
    compileOnly(project(":minecraftmedialibrary-api"))
}
