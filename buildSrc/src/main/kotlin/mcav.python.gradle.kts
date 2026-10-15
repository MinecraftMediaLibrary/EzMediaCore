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

import me.brandonli.mcav.gradle.DownloadToolArchive
import me.brandonli.mcav.gradle.HostPlatform
import me.brandonli.mcav.gradle.LockPythonRequirements
import me.brandonli.mcav.gradle.PythonEnvironment
import me.brandonli.mcav.gradle.isWindows
import me.brandonli.mcav.gradle.libs
import me.brandonli.mcav.gradle.pinnedSha256
import me.brandonli.mcav.gradle.selectTool
import me.brandonli.mcav.gradle.versionOf

val uvTool: String = "uv"
val pythonAlias: String = "python"
val pythonPackageAliasPrefix: String = "python-"
val pythonPackages: List<String> =
    listOf("clang-format", "jupyter-book", "matplotlib", "moderngl", "numpy", "pillow", "pytest", "pytest-xdist", "sphinx-design")

val uvReleasesUrl: String = "https://github.com/astral-sh/uv/releases/download"
val zipExtension: String = ".zip"
val gzipTarExtension: String = ".tar.gz"
val windowsUvExecutable: String = "uv.exe"

val requirementsLockPath: String = "requirements.lock"
val uvToolsPath: String = "tools/uv"
val pythonToolsPath: String = "tools/python"
val uvCachePath: String = "tools/uv/cache"

val licenseHeaderPath: String = "HEADER"
val javadocLinePrefix: String = " *"
val lockCommentPrefix: String = "#"
val lineSeparator: String = "\n"

val uvTarget: String = HostPlatform.current().uvTarget
val pinnedUvVersion: String = libs.versionOf(uvTool)
val pinnedPythonVersion: String = libs.versionOf(pythonAlias)
val pinnedPackages: Map<String, String> = pythonPackages.associateWith { libs.versionOf("$pythonPackageAliasPrefix$it") }
val packageMarkers: Map<String, String> = mapOf(
    "clang-format" to "sys_platform != 'win32' or platform_machine != 'ARM64'",
    "moderngl" to "platform_machine != 'aarch64' and platform_machine != 'ARM64'"
)
val uvArchive: String = "$uvTool-$uvTarget" + if (isWindows) zipExtension else gzipTarExtension
val uvPath: String = if (isWindows) windowsUvExecutable else "$uvTool-$uvTarget/$uvTool"
val downloadUv: TaskProvider<DownloadToolArchive> = tasks.register<DownloadToolArchive>("downloadUv") {
    group = "build setup"
    description = "Downloads the pinned uv and verifies its SHA-256"
    archiveUrl = "$uvReleasesUrl/$pinnedUvVersion/$uvArchive"
    sha256 = pinnedSha256(uvTool, pinnedUvVersion, uvArchive)
    executablePath = uvPath
    installationDirectory = layout.buildDirectory.dir("$uvToolsPath/$pinnedUvVersion")
}
val selectedUv: Provider<RegularFile> = selectTool(uvTool, downloadUv.flatMap { it.installationDirectory.file(uvPath) })
val requirementsLock: RegularFile = layout.projectDirectory.file(requirementsLockPath)
val pythonInstallation: Provider<Directory> = layout.buildDirectory.dir("$pythonToolsPath/$pinnedPythonVersion")
val uvCache: Provider<Directory> = layout.buildDirectory.dir(uvCachePath)
val lockHeader: Provider<String> = providers.fileContents(layout.settingsDirectory.file(licenseHeaderPath)).asText.map { header ->
    header.trimEnd().lines().drop(1).dropLast(1).joinToString(lineSeparator, postfix = lineSeparator + lineSeparator) {
        lockCommentPrefix + it.removePrefix(javadocLinePrefix).trimEnd()
    }
}

val lockRequirements: TaskProvider<LockPythonRequirements> = tasks.register<LockPythonRequirements>("lockPythonRequirements") {
    group = "build setup"
    description = "Regenerates requirements.lock from the Python versions in the catalog"
    uvExecutable = selectedUv
    uvVersion = pinnedUvVersion
    pythonVersion = pinnedPythonVersion
    versions = pinnedPackages
    markers = packageMarkers
    licenseHeader = lockHeader
    lockFile = requirementsLock
    installationDirectory = pythonInstallation
    cacheDirectory = uvCache
}
tasks.register<PythonEnvironment>("installPythonEnvironment") {
    group = "build setup"
    description = "Installs the pinned Python and the hash-locked packages of requirements.lock"
    mustRunAfter(lockRequirements)
    uvExecutable = selectedUv
    uvVersion = pinnedUvVersion
    pythonVersion = pinnedPythonVersion
    requirements = requirementsLock
    versions = pinnedPackages
    lockTask = lockRequirements.map { it.path }
    installationDirectory = pythonInstallation
    cacheDirectory = uvCache
}
