pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "mcav"

include(
    "mcav-browser",
    "mcav-bukkit",
    "mcav-common",
    "mcav-discord",
    "mcav-docs",
    "mcav-http",
    "mcav-installer",
    "mcav-jcstress",
    "mcav-lwjgl",
    "mcav-mod",
    "mcav-plugin",
    "mcav-vm",
    "mcav-vnc",
    "mcav-voicechat",
)
