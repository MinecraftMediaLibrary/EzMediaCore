description = "v1_16_R1"

repositories {
    mavenLocal()
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.16.1-R0.1-SNAPSHOT")
    compileOnly(project(":minecraftmedialibrary-api"))
}
