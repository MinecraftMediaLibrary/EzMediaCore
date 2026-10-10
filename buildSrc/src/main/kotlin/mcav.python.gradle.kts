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

import java.util.Properties
import me.brandonli.mcav.gradle.DownloadToolArchive
import me.brandonli.mcav.gradle.HostPlatform
import me.brandonli.mcav.gradle.LockPythonRequirements
import me.brandonli.mcav.gradle.McavPythonExtension
import me.brandonli.mcav.gradle.PythonEnvironment
import me.brandonli.mcav.gradle.isWindows
import me.brandonli.mcav.gradle.libs
import me.brandonli.mcav.gradle.venvExecutable
import me.brandonli.mcav.gradle.versionOf

val settings = extensions.create<McavPythonExtension>("mcavPython")
val uvTarget = HostPlatform.current().uvTarget
val pinnedUvVersion = libs.versionOf("uv")
val pinnedPythonVersion = libs.versionOf("python")
val uvArchive = "uv-$uvTarget" + if (isWindows) ".zip" else ".tar.gz"
val uvPath = if (isWindows) "uv.exe" else "uv-$uvTarget/uv"
val uvChecksums = Properties().apply {
    DownloadToolArchive::class.java.getResourceAsStream("/uv-checksums.properties")!!.use { load(it) }
}
val downloadUv = tasks.register<DownloadToolArchive>("downloadUv") {
    group = "build setup"
    description = "Downloads the pinned uv and verifies its SHA-256"
    archiveUrl = "https://github.com/astral-sh/uv/releases/download/$pinnedUvVersion/$uvArchive"
    sha256 = provider {
        uvChecksums.getProperty(uvArchive)
            ?: throw GradleException("uv-checksums.properties pins no SHA-256 for $uvArchive; set UV=/path/to/uv $pinnedUvVersion")
    }
    executablePath = uvPath
    installationDirectory = layout.buildDirectory.dir("tools/uv/$pinnedUvVersion")
}
val uvOverride = providers.environmentVariable("UV")
val selectedUv = if (uvOverride.isPresent) {
    layout.file(uvOverride.map { file(it) })
} else {
    downloadUv.flatMap { it.installationDirectory.file(uvPath) }
}
val requirementsLock = layout.projectDirectory.file("requirements.lock")
val pythonInstallation = layout.buildDirectory.dir("python")
val uvCache = layout.buildDirectory.dir("uv-cache")
val lockTaskPath = "$path:lockPythonRequirements"
val lockHeader = providers.fileContents(layout.settingsDirectory.file("HEADER")).asText.map { header ->
    header.trimEnd().lines().drop(1).dropLast(1).joinToString("\n", postfix = "\n\n") { "#" + it.removePrefix(" *").trimEnd() }
}

val lockRequirements = tasks.register<LockPythonRequirements>("lockPythonRequirements") {
    group = "build setup"
    description = "Regenerates requirements.lock from the Python versions in the catalog"
    uvExecutable = selectedUv
    uvVersion = pinnedUvVersion
    pythonVersion = pinnedPythonVersion
    versions = settings.versions
    markers = settings.markers
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
    versions = settings.versions
    lockTask = lockTaskPath
    pythonPath = venvExecutable("python")
    installationDirectory = pythonInstallation
    cacheDirectory = uvCache
}
