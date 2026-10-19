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

import java.io.File
import java.io.InputStream
import java.util.Locale
import java.util.Objects
import java.util.Properties
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

object Tools {

    private const val CHECKSUMS_RESOURCE: String = "/tool-checksums.properties"

    private const val NAME_SEPARATOR: Char = '-'
    private const val VARIABLE_SEPARATOR: Char = '_'

    private val PINNED_CHECKSUMS: Properties = this.loadPinnedChecksums()

    fun pinnedSha256(project: Project, tool: String, version: String, archive: String): Provider<String> {
        return project.provider { this.pinnedSha256Of(tool, version, archive) }
    }

    fun toolOverride(project: Project, tool: String): Provider<RegularFile> {
        val variable: String = this.overrideVariable(tool)
        val path: Provider<String> = project.providers.environmentVariable(variable)
        val file: Provider<File> = path.map { project.file(it) }
        return project.layout.file(file)
    }

    fun selectTool(project: Project, tool: String, installed: Provider<RegularFile>): Provider<RegularFile> {
        val override: Provider<RegularFile> = this.toolOverride(project, tool)
        return if (override.isPresent) override else installed
    }

    private fun loadPinnedChecksums(): Properties {
        val message: String = "Resource $CHECKSUMS_RESOURCE is missing from buildSrc!"
        val resource: InputStream? = DownloadToolArchive::class.java.getResourceAsStream(CHECKSUMS_RESOURCE)
        val checksums: InputStream = Objects.requireNonNull<InputStream>(resource, message)
        val properties: Properties = Properties()
        checksums.use(properties::load)

        return properties
    }

    private fun pinnedSha256Of(tool: String, version: String, archive: String): String {
        val key: String = "$tool/$version/$archive"
        val sha256: String? = PINNED_CHECKSUMS.getProperty(key)
        if (sha256 == null) {
            val variable: String = this.overrideVariable(tool)
            val message: String = "tool-checksums.properties pins no SHA-256 for $key, so set $variable to a $tool $version binary!"
            throw GradleException(message)
        }

        return sha256
    }

    private fun overrideVariable(tool: String): String {
        return tool.uppercase(Locale.ROOT).replace(NAME_SEPARATOR, VARIABLE_SEPARATOR)
    }
}
