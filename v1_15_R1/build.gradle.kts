description = "v1_15_R1"

repositories {
    mavenLocal()
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.15.2-R0.1-SNAPSHOT")
    compileOnly(project(":api"))
}
