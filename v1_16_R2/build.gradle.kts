description = "v1_16_R2"

repositories {
    mavenLocal()
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.16.3-R0.1-SNAPSHOT")
    compileOnly(project(":api"))
}
