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

package me.brandonli.mcav.gradle

import java.util.Locale
import java.util.Properties
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

private val pinnedChecksums = Properties().apply {
    DownloadToolArchive::class.java.getResourceAsStream("/tool-checksums.properties")!!.use { load(it) }
}

private fun overrideVariable(tool: String): String = tool.uppercase(Locale.ROOT).replace('-', '_')

fun Project.pinnedSha256(tool: String, version: String, archive: String): Provider<String> = provider {
    val key = "$tool/$version/$archive"
    val message =
        "tool-checksums.properties pins no SHA-256 for $key, so set ${overrideVariable(tool)} to the path of $tool $version!"
    pinnedChecksums.getProperty(key) ?: throw GradleException(message)
}

fun Project.toolOverride(tool: String): Provider<RegularFile> =
    layout.file(providers.environmentVariable(overrideVariable(tool)).map { file(it) })

fun Project.selectTool(tool: String, installed: Provider<RegularFile>): Provider<RegularFile> {
    val override = toolOverride(tool)
    return if (override.isPresent) override else installed
}
