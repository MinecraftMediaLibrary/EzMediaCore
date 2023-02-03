plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "io.github.pulsebeat02"
version = "v1.0.0"

repositories {
    mavenCentral()
}

dependencies {

    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("me.friwi:jcefmaven:109.1.11")

    // Projects
    setOf(
        project(":ezmediacore:lib"),
        project(":ezmediacore:api"),
        project(":ezmediacore:main"),
    ).forEach {
        implementation(it)
    }


    setOf(
        "org.bstats:bstats-bukkit:3.0.0",
        "net.kyori:adventure-api:4.12.0",
        "net.kyori:adventure-platform-bukkit:4.2.0",
    ).forEach {
        implementation(it)
    }

    compileOnly("com.mojang:brigadier:1.0.18")
    implementation("me.lucko:commodore:2.2") {
        exclude("com.mojang", "brigadier")
    }
}

tasks {

    build {
        dependsOn(shadowJar)
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        rename("plugin.json", "plugin.yml")
    }

    shadowJar {
        archiveBaseName.set("DeluxeMediaPlugin")
        val base = "io.github.pulsebeat02.deluxemediaplugin.lib"
        relocate("org.bstats", "$base.bstats")
        relocate("me.lucko", "$base.lucko")
        relocate("net.kyori", "$base.kyori")
    }
}