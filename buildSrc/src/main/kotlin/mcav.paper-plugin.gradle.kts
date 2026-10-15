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

val minecraftAlias: String = "minecraft"
val paperAlias: String = "paper"
val junitBomAlias: String = "junit-bom"
val junitJupiterAlias: String = "junit-jupiter"
val gsonAlias: String = "gson"
val junitLauncherAlias: String = "junit-platform-launcher"
val voiceChatAlias: String = "voicechat-plugin"
val sparkAlias: String = "spark"
val sparkBuildAlias: String = "spark-build"

val snapshotSuffix: String = "-SNAPSHOT"
val minecraftVersionPrefix: String = "-v"
val noChangingModuleCache: Int = 0
val changingModuleCacheUnit: String = "seconds"
val classpathSuffix: String = "Classpath"
val projectPathPrefix: String = ":"

val nativeResourcesConfiguration: String = "mcv2NativeResources"
val bukkitProjectPath: String = ":mcav-bukkit"
val requiredClassesPath: String = "generated/resources/required-classes"
val dependencyManifestSource: String = "dependencies.txt"
val dependencyManifestTarget: String = "mcav/plugin/dependencies.txt"

val pluginName: String = "MCAV"
val pluginDescription: String = "MCAV Sandbox Plugin"
val pluginAuthor: String = "PulseBeat_02"
val pluginLogPrefix: String = "MCAV Sandbox"
val pluginLoaderClass: String = "me.brandonli.mcav.plugin.MCAVLoader"
val pluginMainClass: String = "me.brandonli.mcav.plugin.MCAVSandbox"
val voiceChatPlugin: String = "voicechat"

val serverHeapSize: String = "8192m"

val endToEndProperty: String = "mcav.e2e"
val endToEndPortProperty: String = "mcav.e2e.repositoryPort"
val acceptEulaProperty: String = "mcav.acceptMinecraftEula"
val disabledValue: String = "false"
val anyFreePort: Int = 0
val endToEndRepositoryPath: String = "e2e-repository"
val endToEndRepositoryName: String = "endToEnd"
val endToEndPublishTaskSuffix: String = "publishMavenPublicationToEndToEndRepository"
val publicRepositorySchemes: Set<String> = setOf("http", "https")
val localRepositoryHost: String = "http://127.0.0.1"

val endToEndTestName: String = "e2eTest"
val endToEndImplementation: String = "e2eTestImplementation"
val endToEndRuntimeOnly: String = "e2eTestRuntimeOnly"
val endToEndCachePath: String = "e2e-cache"

val paperFileNameProperty: String = "mcav.e2e.paperFileName"
val voiceChatFileNameProperty: String = "mcav.e2e.voiceChatFileName"
val cacheDirectoryProperty: String = "mcav.e2e.cacheDirectory"
val acceptEulaTestProperty: String = "mcav.e2e.acceptEula"
val repositoryDirectoryProperty: String = "mcav.e2e.repositoryDirectory"
val pluginJarProperty: String = "mcav.e2e.pluginJar"
val javaProperty: String = "mcav.e2e.java"

val legacyFormattingWarningProperty: String = "net.kyori.adventure.text.warnWhenLegacyFormattingDetected"
val voiceChatModrinthProject: String = "simple-voice-chat"
val sparkJobUrl: String = "https://ci.lucko.me/job/spark"

val minecraftVersion: String = libs.versionOf(minecraftAlias)
private val paperVersion: PaperVersion = PaperVersion.parse(libs.versionOf(paperAlias))
version = "${rootProject.version.toString().removeSuffix(snapshotSuffix)}$minecraftVersionPrefix$minecraftVersion"

configurations.compileOnly {
    extendsFrom(configurations.runtimeDownload.get())
}

configurations.testImplementation {
    extendsFrom(configurations.compileOnly.get())
}

configurations.runtimeDownload {
    resolutionStrategy.cacheChangingModulesFor(noChangingModuleCache, changingModuleCacheUnit)
}

configurations.matching { it.name.endsWith(classpathSuffix) }.configureEach {
    resolutionStrategy.dependencySubstitution.all {
        val module: ModuleComponentSelector = requested as? ModuleComponentSelector ?: return@all
        val project: Project? = rootProject.findProject("$projectPathPrefix${module.module}")
        if (module.group == rootProject.group.toString() && project != null) {
            useTarget(project)
        }
    }
}

val nativeResources: NamedDomainObjectProvider<Configuration> = configurations.register(nativeResourcesConfiguration) {
    isCanBeConsumed = false
    isCanBeResolved = true
}
dependencies {
    add(nativeResources.name, project(path = bukkitProjectPath, configuration = nativeResourcesConfiguration))
}
tasks.shadowJar {
    from(nativeResources)
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

val downloadedModules: Provider<List<String>> = configurations.runtimeDownload.map { configuration ->
    configuration.dependencies
        .filter { it.group == rootProject.group.toString() && rootProject.findProject("$projectPathPrefix${it.name}") != null }
        .map { "$projectPathPrefix${it.name}" }
}
val requiredModuleClasses: TaskProvider<RequiredModuleClassesTask> = tasks.register<RequiredModuleClassesTask>("requiredModuleClasses") {
    group = "build"
    description = "Lists the classes of the downloaded modules that the plugin uses, for its loader to check them"
    pluginClasses.from(sourceSets.main.map { it.output.classesDirs })
    modules.from(
        configurations.compileClasspath.map { classpath ->
            val downloaded: List<String> = downloadedModules.get()
            classpath.incoming.artifactView {
                componentFilter { it is ProjectComponentIdentifier && it.projectPath in downloaded }
            }.files
        }
    )
    output = layout.buildDirectory.dir(requiredClassesPath)
}

sourceSets.main {
    resources.srcDir(requiredModuleClasses)
}

tasks.processResources {
    val dependencyManifestPath: String = dependencyManifestTarget
    inputs.property("dependencyManifestPath", dependencyManifestPath)
    filesMatching(dependencyManifestSource) {
        path = dependencyManifestPath
    }
}

val pluginPermissions: Map<String, String> = mapOf(
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
    name = pluginName
    version = "${project.version}"
    description = pluginDescription
    authors = listOf(pluginAuthor)
    prefix = pluginLogPrefix
    loader = pluginLoaderClass
    main = pluginMainClass
    dependencies.server(voiceChatPlugin, PaperPluginYaml.Load.BEFORE, false)
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
    jvmArgs("-Xms$serverHeapSize", "-Xmx$serverHeapSize", "-XX:+AllowEnhancedClassRedefinition", "-XX:+AllowRedefinitionToAddDeleteMethods")
}

val endToEnd: Boolean = providers.gradleProperty(endToEndProperty).map { it.toBoolean() }.getOrElse(false)
val endToEndRepository: File = rootProject.layout.buildDirectory.dir(endToEndRepositoryPath).get().asFile
val endToEndPort: Int = if (endToEnd) {
    providers.gradleProperty(endToEndPortProperty).orNull?.toInt() ?: ServerSocket(anyFreePort).use { it.localPort }
} else {
    anyFreePort
}

if (endToEnd) {
    val repository: MavenArtifactRepository = repositories.maven {
        name = endToEndRepositoryName
        url = endToEndRepository.toURI()
    }
    repositories.remove(repository)
    repositories.addFirst(repository)
    tasks.named<WriteDependencySet>("writeDependencies") {
        dependsOn(downloadedModules.map { modules -> modules.map { "$it:$endToEndPublishTaskSuffix" } })
        val publicRepositories: List<String> = repositories.withType<MavenArtifactRepository>()
            .filter { it.url.scheme in publicRepositorySchemes }
            .map { it.url.toString() }
        repos = listOf("$localRepositoryHost:$endToEndPort/") + publicRepositories
    }
}

val endToEndTestSourceSet: SourceSet = sourceSets.create(endToEndTestName)

dependencies {
    add(endToEndImplementation, platform(libs.libraryOf(junitBomAlias)))
    add(endToEndImplementation, libs.libraryOf(junitJupiterAlias))
    add(endToEndImplementation, libs.libraryOf(gsonAlias))
    add(endToEndRuntimeOnly, libs.libraryOf(junitLauncherAlias))
}

tasks.register<Test>(endToEndTestName) {
    group = "verification"
    description = "Runs the plugin on a real headless Paper server; needs the internet, -Pmcav.e2e=true and -Pmcav.acceptMinecraftEula=true"
    testClassesDirs = endToEndTestSourceSet.output.classesDirs
    classpath = endToEndTestSourceSet.runtimeClasspath
    val pluginJar: Provider<RegularFile> = tasks.shadowJar.flatMap { it.archiveFile }
    inputs.file(pluginJar)
    outputs.upToDateWhen { false }
    systemProperty(paperFileNameProperty, paperVersion.fileName)
    systemProperty(voiceChatFileNameProperty, "$voiceChatPlugin-${libs.versionOf(voiceChatAlias)}.jar")
    systemProperty(cacheDirectoryProperty, layout.buildDirectory.dir(endToEndCachePath).get().asFile.absolutePath)
    systemProperty(acceptEulaTestProperty, providers.gradleProperty(acceptEulaProperty).getOrElse(disabledValue))
    systemProperty(repositoryDirectoryProperty, endToEndRepository.absolutePath)
    systemProperty(endToEndPortProperty, endToEndPort)
    val launcher: Provider<JavaLauncher> = javaLauncher()
    val usesThisBuild: Boolean = endToEnd
    doFirst {
        if (!usesThisBuild) {
            val message: String = "Run the end-to-end test with -Pmcav.e2e=true, so the server uses the modules of this build!"
            throw GradleException(message)
        }
        systemProperty(pluginJarProperty, pluginJar.get().asFile.absolutePath)
        systemProperty(javaProperty, launcher.get().executablePath.asFile.absolutePath)
    }
}

tasks.runServer {
    systemProperty(legacyFormattingWarningProperty, false)
    downloadPlugins {
        modrinth(voiceChatModrinthProject, libs.versionOf(voiceChatAlias))
        val spark: String = libs.versionOf(sparkAlias)
        url("$sparkJobUrl/${libs.versionOf(sparkBuildAlias)}/artifact/spark-bukkit/build/libs/spark-$spark-bukkit.jar")
    }
}
