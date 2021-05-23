description = "v1_10_R1"

repositories {
    mavenLocal()
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.10.2-R0.1-SNAPSHOT")
    compileOnly(project(":api"))
}
