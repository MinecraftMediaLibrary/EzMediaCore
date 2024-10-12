plugins {
    id("io.papermc.paperweight.userdev") version "1.7.3"
}

dependencies {
    compileOnly(project(":ezmediacore:nms-api"))
    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")
}