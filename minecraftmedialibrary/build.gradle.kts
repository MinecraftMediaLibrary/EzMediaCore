description = "minecraftmedialibrary"

plugins {
    id("com.github.pulsebeat02.java-conventions")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencies {
    implementation(project(":minecraftmedialibrary-api"))
    implementation(project(":minecraftmedialibrary-nms"))
}

shadowJar {
    relocate 'com.myCompany.project.event', 'com.myCompany.relocated.project.event'
}