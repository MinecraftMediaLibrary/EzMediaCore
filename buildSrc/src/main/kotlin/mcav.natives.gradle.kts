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

val host = HostPlatform.current()
val system = host.zigSystem
val architecture = host.architecture
val pinnedZigVersion = libs.versionOf("zig")
val zigFolder = "zig-$architecture-$system-$pinnedZigVersion"
val zigArchive = zigFolder + if (system == "windows") ".zip" else ".tar.xz"
val zigPath = "$zigFolder/zig" + if (system == "windows") ".exe" else ""
val zigChecksum = pinnedSha256("zig", pinnedZigVersion, zigArchive)
val downloadZig = tasks.register<DownloadToolArchive>("downloadZig") {
    group = "build setup"
    description = "Downloads the pinned Zig and verifies its SHA-256"
    archiveUrl = "https://ziglang.org/download/$pinnedZigVersion/$zigArchive"
    sha256 = zigChecksum
    executablePath = zigPath
    installationDirectory = layout.buildDirectory.dir("tools/zig/$pinnedZigVersion")
}
val nativeSources = layout.projectDirectory.dir("src/main/native/mcv2")
val buildMcv2Natives = tasks.register<BuildMcv2Natives>("buildMcv2Natives") {
    group = "build"
    description = "Compiles MCV2's six native libraries from source"
    sourcesDirectory = nativeSources
    zigExecutable = selectTool("zig", downloadZig.flatMap { it.installationDirectory.file(zigPath) })
    zigVersion = pinnedZigVersion
    toolchainChecksum = toolOverride("zig").map { "ZIG override" }.orElse(zigChecksum)
    outputDirectory = layout.buildDirectory.dir("generated/natives")
    cacheDirectory = layout.buildDirectory.dir("tools/zig/cache")
    processCount = gradle.startParameter.maxWorkerCount
}
sourceSets.main {
    resources.srcDir(buildMcv2Natives)
}

val nativeResources = configurations.register("mcv2NativeResources") {
    isCanBeConsumed = true
    isCanBeResolved = false
}
artifacts.add(nativeResources.name, buildMcv2Natives.flatMap { it.outputDirectory }) {
    builtBy(buildMcv2Natives)
    type = "directory"
}

val pinnedClangFormatVersion = libs.versionOf("python-clang-format")
val pythonEnvironment = rootProject.tasks.named<PythonEnvironment>("installPythonEnvironment")
val selectedClangFormat = selectTool("clang-format", pythonEnvironment.flatMap { it.executable("clang-format") })
val formattedSources = fileTree(nativeSources) { include("*.cpp") }

tasks.register<ClangFormat>("formatMcv2Natives") {
    group = "formatting"
    description = "Formats the MCV2 native sources with clang-format $pinnedClangFormatVersion"
    sources.from(formattedSources)
    clangFormat = selectedClangFormat
    clangFormatVersion = pinnedClangFormatVersion
    rewrite = true
}
val checkNativesFormat = tasks.register<ClangFormat>("checkMcv2NativesFormat") {
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
