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

import me.brandonli.mcav.gradle.BuildMcv2Natives
import me.brandonli.mcav.gradle.ClangFormat
import me.brandonli.mcav.gradle.DownloadToolArchive
import me.brandonli.mcav.gradle.HostPlatform
import me.brandonli.mcav.gradle.PythonEnvironment
import me.brandonli.mcav.gradle.libs
import me.brandonli.mcav.gradle.pinnedSha256
import me.brandonli.mcav.gradle.selectTool
import me.brandonli.mcav.gradle.toolOverride
import me.brandonli.mcav.gradle.versionOf

plugins {
    java
}

val zigTool: String = "zig"
val clangFormatTool: String = "clang-format"
val clangFormatAlias: String = "python-clang-format"

val windowsSystem: String = "windows"
val zipExtension: String = ".zip"
val xzTarExtension: String = ".tar.xz"
val executableExtension: String = ".exe"
val zigDownloadUrl: String = "https://ziglang.org/download"
val zigOverrideChecksum: String = "ZIG override"

val nativeSourcePath: String = "src/main/native/mcv2"
val nativeSourcePattern: String = "*.cpp"
val zigToolsPath: String = "tools/zig"
val zigCachePath: String = "tools/zig/cache"
val generatedNativesPath: String = "generated/natives"

val nativeResourcesConfiguration: String = "mcv2NativeResources"
val directoryArtifactType: String = "directory"

val host: HostPlatform = HostPlatform.current()
val system: String = host.zigSystem
val architecture: String = host.architecture
val pinnedZigVersion: String = libs.versionOf(zigTool)
val zigFolder: String = "$zigTool-$architecture-$system-$pinnedZigVersion"
val zigArchive: String = zigFolder + if (system == windowsSystem) zipExtension else xzTarExtension
val zigPath: String = "$zigFolder/$zigTool" + if (system == windowsSystem) executableExtension else ""
val zigChecksum: Provider<String> = pinnedSha256(zigTool, pinnedZigVersion, zigArchive)
val downloadZig: TaskProvider<DownloadToolArchive> = tasks.register<DownloadToolArchive>("downloadZig") {
    group = "build setup"
    description = "Downloads the pinned Zig and verifies its SHA-256"
    archiveUrl = "$zigDownloadUrl/$pinnedZigVersion/$zigArchive"
    sha256 = zigChecksum
    executablePath = zigPath
    installationDirectory = layout.buildDirectory.dir("$zigToolsPath/$pinnedZigVersion")
}
val nativeSources: Directory = layout.projectDirectory.dir(nativeSourcePath)
val buildMcv2Natives: TaskProvider<BuildMcv2Natives> = tasks.register<BuildMcv2Natives>("buildMcv2Natives") {
    group = "build"
    description = "Compiles MCV2's six native libraries from source"
    sourcesDirectory = nativeSources
    zigExecutable = selectTool(zigTool, downloadZig.flatMap { it.installationDirectory.file(zigPath) })
    zigVersion = pinnedZigVersion
    toolchainChecksum = toolOverride(zigTool).map { zigOverrideChecksum }.orElse(zigChecksum)
    outputDirectory = layout.buildDirectory.dir(generatedNativesPath)
    cacheDirectory = layout.buildDirectory.dir(zigCachePath)
    processCount = gradle.startParameter.maxWorkerCount
}
sourceSets.main {
    resources.srcDir(buildMcv2Natives)
}

val nativeResources: NamedDomainObjectProvider<Configuration> = configurations.register(nativeResourcesConfiguration) {
    isCanBeConsumed = true
    isCanBeResolved = false
}
artifacts.add(nativeResources.name, buildMcv2Natives.flatMap { it.outputDirectory }) {
    builtBy(buildMcv2Natives)
    type = directoryArtifactType
}

val pinnedClangFormatVersion: String = libs.versionOf(clangFormatAlias)
val pythonEnvironment: TaskProvider<PythonEnvironment> = rootProject.tasks.named<PythonEnvironment>("installPythonEnvironment")
val selectedClangFormat: Provider<RegularFile> = selectTool(clangFormatTool, pythonEnvironment.flatMap { it.executable(clangFormatTool) })
val formattedSources: ConfigurableFileTree = fileTree(nativeSources) { include(nativeSourcePattern) }

tasks.register<ClangFormat>("formatMcv2Natives") {
    group = "formatting"
    description = "Formats the MCV2 native sources with clang-format $pinnedClangFormatVersion"
    sources.from(formattedSources)
    clangFormat = selectedClangFormat
    clangFormatVersion = pinnedClangFormatVersion
    rewrite = true
}
val checkNativesFormat: TaskProvider<ClangFormat> = tasks.register<ClangFormat>("checkMcv2NativesFormat") {
    group = "verification"
    description = "Checks that the MCV2 native sources are formatted with clang-format $pinnedClangFormatVersion"
    sources.from(formattedSources)
    clangFormat = selectedClangFormat
    clangFormatVersion = pinnedClangFormatVersion
    rewrite = false
}
tasks.named("check") {
    dependsOn(checkNativesFormat)
}
