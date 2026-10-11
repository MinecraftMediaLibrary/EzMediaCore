/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import java.net.ServerSocket
import me.brandonli.mcav.gradle.PaperVersion
import me.brandonli.mcav.gradle.RequiredModuleClassesTask
import me.brandonli.mcav.gradle.javaLauncher
import me.brandonli.mcav.gradle.libraryOf
import me.brandonli.mcav.gradle.libs
import me.brandonli.mcav.gradle.versionOf
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import xyz.jpenilla.gremlin.gradle.WriteDependencySet
import xyz.jpenilla.resourcefactory.bukkit.Permission
import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml
import xyz.jpenilla.runtask.task.AbstractRun

plugins {
    java
    id("mcav.licensing")
    id("com.gradleup.shadow")
    id("xyz.jpenilla.run-paper")
    id("xyz.jpenilla.resource-factory-paper-convention")
    id("xyz.jpenilla.gremlin-gradle")
    id("org.bytedeco.gradle-javacpp-platform")
}

val minecraftVersion = libs.versionOf("minecraft")
private val paperVersion = PaperVersion.parse(libs.versionOf("paper"))
version = "${rootProject.version.toString().removeSuffix("-SNAPSHOT")}-v$minecraftVersion"

configurations.compileOnly {
    extendsFrom(configurations.runtimeDownload.get())
}

configurations.testImplementation {
    extendsFrom(configurations.compileOnly.get())
}

configurations.runtimeDownload {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

configurations.matching { it.name.endsWith("Classpath") }.configureEach {
    resolutionStrategy.dependencySubstitution.all {
        val module = requested as? ModuleComponentSelector ?: return@all
        val project = rootProject.findProject(":${module.module}")
        if (module.group == rootProject.group.toString() && project != null) {
            useTarget(project)
        }
    }
}

val nativeResources = configurations.register("mcv2NativeResources") {
    isCanBeConsumed = false
    isCanBeResolved = true
}
dependencies {
    add(nativeResources.name, project(path = ":mcav-bukkit", configuration = "mcv2NativeResources"))
}
tasks.shadowJar {
    from(nativeResources)
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

val downloadedModules = configurations.runtimeDownload.map { configuration ->
    configuration.dependencies
        .filter { it.group == rootProject.group.toString() && rootProject.findProject(":${it.name}") != null }
        .map { ":${it.name}" }
}
val requiredModuleClasses = tasks.register<RequiredModuleClassesTask>("requiredModuleClasses") {
    group = "build"
    description = "Lists the classes of the downloaded modules that the plugin uses, for its loader to check them"
    pluginClasses.from(sourceSets.main.map { it.output.classesDirs })
    modules.from(
        configurations.compileClasspath.map { classpath ->
            val downloaded = downloadedModules.get()
            classpath.incoming.artifactView {
                componentFilter { it is ProjectComponentIdentifier && it.projectPath in downloaded }
            }.files
        }
    )
    output = layout.buildDirectory.dir("generated/resources/required-classes")
}

sourceSets.main {
    resources.srcDir(requiredModuleClasses)
}

tasks.processResources {
    val dependencyManifestPath = "mcav/plugin/dependencies.txt"
    inputs.property("dependencyManifestPath", dependencyManifestPath)
    filesMatching("dependencies.txt") {
        path = dependencyManifestPath
    }
}

val pluginPermissions = mapOf(
    "mcav.command.help" to "Shows the commands of MCAV and what they do",
    "mcav.command.dump" to "Uploads a report of the server and its logs, with addresses and other commands masked, for a bug report",
    "mcav.command.screen" to "Builds a wall of item frames with maps that images, videos, browsers and virtual machines show on",
    "mcav.command.hologram.set" to "Shows a video or an image as a hologram",
    "mcav.command.hologram.disable" to "Removes the hologram",
    "mcav.command.image.chat" to "Shows an image in chat",
    "mcav.command.image.block" to "Shows an image with blocks",
    "mcav.command.image.entity" to "Shows an image with the names of entities",
    "mcav.command.image.scoreboard" to "Shows an image on the scoreboard",
    "mcav.command.image.map" to "Shows an image on a wall of maps",
    "mcav.command.image.release" to "Removes the image",
    "mcav.command.video.chat" to "Plays a video in chat",
    "mcav.command.video.block" to "Plays a video with blocks",
    "mcav.command.video.entity" to "Plays a video with the names of entities",
    "mcav.command.video.scoreboard" to "Plays a video on the scoreboard",
    "mcav.command.video.map" to "Plays a video on a wall of maps",
    "mcav.command.video.mcv2" to "Plays a video on a wall of maps through the MCV2 codec, dithered for players without its resource pack",
    "mcav.command.video.pause" to "Pauses the video",
    "mcav.command.video.resume" to "Resumes the video",
    "mcav.command.video.release" to "Stops the video",
    "mcav.command.video.seek" to "Jumps to a time of the video",
    "mcav.command.video.volume" to "Sets the volume of the videos",
    "mcav.command.video.speed" to "Plays the video file faster or slower",
    "mcav.command.video.loop" to "Makes the videos play again when they end",
    "mcav.command.video.device" to "Lists the cameras and capture cards of the server, and plays them with the DEVICE player",
    "mcav.command.mcv2.play" to "Plays, streams and stops pre-encoded MCV2 streams from the plugin's mcv2 folder",
    "mcav.command.mcv2.encode" to "Pre-encodes a video file into an MCV2 stream in the plugin's mcv2 folder, and cancels that encode",
    "mcav.command.browser.create" to "Opens a web page on a wall of maps, with its sound in an audio output",
    "mcav.browser.release" to "Closes the browser",
    "mcav.browser.interact" to "Clicks and types into the browser: clicks on its screen, and chat with /mcav browser interact",
    "mcav.command.vm.create" to "Boots a QEMU virtual machine on a wall of maps, with its sound in an audio output",
    "mcav.vm.release" to "Powers off the virtual machine",
    "mcav.vm.interact" to "Clicks and types into the virtual machine: clicks on its screen, and chat with /mcav vm interact",
    "mcav.command.vnc.create" to "Shows the desktop of a VNC server listed in vnc.allowed-hosts on a wall of maps",
    "mcav.vnc.release" to "Disconnects from the VNC desktop",
    "mcav.vnc.interact" to "Clicks and types on the VNC desktop: clicks on its screen, and chat with /mcav vnc interact",
)

paperPluginYaml {
    apiVersion = minecraftVersion
    name = "MCAV"
    version = "${project.version}"
    description = "MCAV Sandbox Plugin"
    authors = listOf("PulseBeat_02")
    prefix = "MCAV Sandbox"
    loader = "me.brandonli.mcav.plugin.MCAVLoader"
    main = "me.brandonli.mcav.plugin.MCAVSandbox"
    dependencies.server("voicechat", PaperPluginYaml.Load.BEFORE, false)
    defaultPermission = Permission.Default.OP
    permissions {
        pluginPermissions.forEach { (name, text) -> register(name) { description = text } }
    }
}

tasks.runServer {
    minecraftVersion(minecraftVersion)
    build(paperVersion.build)
}

tasks.withType<AbstractRun>().configureEach {
    javaLauncher = javaLauncher(JvmVendorSpec.JETBRAINS)
    jvmArgs("-Xms8192m", "-Xmx8192m", "-XX:+AllowEnhancedClassRedefinition", "-XX:+AllowRedefinitionToAddDeleteMethods")
}

val endToEnd = providers.gradleProperty("mcav.e2e").map { it.toBoolean() }.getOrElse(false)
val endToEndRepository = rootProject.layout.buildDirectory.dir("e2e-repository").get().asFile
val endToEndPort = if (endToEnd) {
    providers.gradleProperty("mcav.e2e.repositoryPort").orNull?.toInt() ?: ServerSocket(0).use { it.localPort }
} else {
    0
}

if (endToEnd) {
    val repository = repositories.maven {
        name = "endToEnd"
        url = endToEndRepository.toURI()
    }
    repositories.remove(repository)
    repositories.addFirst(repository)
    tasks.named<WriteDependencySet>("writeDependencies") {
        dependsOn(downloadedModules.map { modules -> modules.map { "$it:publishMavenPublicationToEndToEndRepository" } })
        val publicRepositories = repositories.withType<MavenArtifactRepository>()
            .filter { it.url.scheme == "http" || it.url.scheme == "https" }
            .map { it.url.toString() }
        repos = listOf("http://127.0.0.1:$endToEndPort/") + publicRepositories
    }
}

val e2eTestSourceSet = sourceSets.create("e2eTest")

dependencies {
    "e2eTestImplementation"(platform(libs.libraryOf("junit-bom")))
    "e2eTestImplementation"(libs.libraryOf("junit-jupiter"))
    "e2eTestImplementation"(libs.libraryOf("gson"))
    "e2eTestRuntimeOnly"(libs.libraryOf("junit-platform-launcher"))
}

tasks.register<Test>("e2eTest") {
    group = "verification"
    description = "Runs the plugin on a real headless Paper server; needs the internet, -Pmcav.e2e=true and -Pmcav.acceptMinecraftEula=true"
    testClassesDirs = e2eTestSourceSet.output.classesDirs
    classpath = e2eTestSourceSet.runtimeClasspath
    val pluginJar = tasks.shadowJar.flatMap { it.archiveFile }
    inputs.file(pluginJar)
    outputs.upToDateWhen { false }
    systemProperty("mcav.e2e.paperFileName", paperVersion.fileName)
    systemProperty("mcav.e2e.voiceChatFileName", "voicechat-${libs.versionOf("voicechat-plugin")}.jar")
    systemProperty("mcav.e2e.cacheDirectory", layout.buildDirectory.dir("e2e-cache").get().asFile.absolutePath)
    systemProperty("mcav.e2e.acceptEula", providers.gradleProperty("mcav.acceptMinecraftEula").getOrElse("false"))
    systemProperty("mcav.e2e.repositoryDirectory", endToEndRepository.absolutePath)
    systemProperty("mcav.e2e.repositoryPort", endToEndPort)
    val launcher = javaLauncher()
    val usesThisBuild = endToEnd
    doFirst {
        if (!usesThisBuild) {
            throw GradleException("Run the end-to-end test with -Pmcav.e2e=true, so the server uses the modules of this build")
        }
        systemProperty("mcav.e2e.pluginJar", pluginJar.get().asFile.absolutePath)
        systemProperty("mcav.e2e.java", launcher.get().executablePath.asFile.absolutePath)
    }
}

tasks.runServer {
    systemProperty("net.kyori.adventure.text.warnWhenLegacyFormattingDetected", false)
    downloadPlugins {
        modrinth("simple-voice-chat", libs.versionOf("voicechat-plugin"))
        val spark = libs.versionOf("spark")
        url("https://ci.lucko.me/job/spark/${libs.versionOf("spark-build")}/artifact/spark-bukkit/build/libs/spark-$spark-bukkit.jar")
    }
}
