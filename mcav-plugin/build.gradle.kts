import me.brandonli.mcav.gradle.moduleCoordinate

plugins {
    id("mcav.module")
    id("mcav.paper-plugin")
    id("mcav.browser-benchmark")
}

dependencies {
    implementation(libs.gremlin.runtime)
    implementation(moduleCoordinate("mcav-voicechat"))
    compileOnly(libs.paper.api)
    runtimeDownload(moduleCoordinate("mcav-bukkit"))
    runtimeDownload(moduleCoordinate("mcav-discord"))
    runtimeDownload(moduleCoordinate("mcav-http"))
    runtimeDownload(moduleCoordinate("mcav-common"))
    runtimeDownload(moduleCoordinate("mcav-vm"))
    runtimeDownload(moduleCoordinate("mcav-vnc"))
    runtimeDownload(moduleCoordinate("mcav-browser"))
    runtimeDownload(libs.bundles.cloud)
    runtimeDownload(libs.commodore)
    runtimeDownload(libs.bstats.bukkit)
    runtimeDownload(libs.jda)
    testImplementation(libs.voicechat.api)
    testImplementation(libs.slf4j.simple)
}
