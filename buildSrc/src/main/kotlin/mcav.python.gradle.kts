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

val uvTarget = HostPlatform.current().uvTarget
val pinnedUvVersion = libs.versionOf("uv")
val pinnedPythonVersion = libs.versionOf("python")
val pinnedPackages = listOf(
    "clang-format", "jupyter-book", "matplotlib", "moderngl", "numpy", "pillow", "pytest", "pytest-xdist", "sphinx-design"
).associateWith { libs.versionOf("python-$it") }
val packageMarkers = mapOf(
    "clang-format" to "sys_platform != 'win32' or platform_machine != 'ARM64'",
    "moderngl" to "platform_machine != 'aarch64' and platform_machine != 'ARM64'"
)
val uvArchive = "uv-$uvTarget" + if (isWindows) ".zip" else ".tar.gz"
val uvPath = if (isWindows) "uv.exe" else "uv-$uvTarget/uv"
val downloadUv = tasks.register<DownloadToolArchive>("downloadUv") {
    group = "build setup"
    description = "Downloads the pinned uv and verifies its SHA-256"
    archiveUrl = "https://github.com/astral-sh/uv/releases/download/$pinnedUvVersion/$uvArchive"
    sha256 = pinnedSha256("uv", pinnedUvVersion, uvArchive)
    executablePath = uvPath
    installationDirectory = layout.buildDirectory.dir("tools/uv/$pinnedUvVersion")
}
val selectedUv = selectTool("uv", downloadUv.flatMap { it.installationDirectory.file(uvPath) })
val requirementsLock = layout.projectDirectory.file("requirements.lock")
val pythonInstallation = layout.buildDirectory.dir("tools/python/$pinnedPythonVersion")
val uvCache = layout.buildDirectory.dir("tools/uv/cache")
val lockHeader = providers.fileContents(layout.settingsDirectory.file("HEADER")).asText.map { header ->
    header.trimEnd().lines().drop(1).dropLast(1).joinToString("\n", postfix = "\n\n") { "#" + it.removePrefix(" *").trimEnd() }
}

val lockRequirements = tasks.register<LockPythonRequirements>("lockPythonRequirements") {
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
