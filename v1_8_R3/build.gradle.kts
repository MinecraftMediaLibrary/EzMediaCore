description = "v1_8_R3"

repositories {
    mavenLocal()
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
    compileOnly(project(":api"))
}
