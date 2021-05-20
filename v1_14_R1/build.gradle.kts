description = "v1_14_R1"

repositories {
    mavenLocal()
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.14.4-R0.1-SNAPSHOT")
    compileOnly(project(":api"))
}
