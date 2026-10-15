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

import java.io.IOException
import java.net.URI
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.tukaani.xz.XZInputStream

@DisableCachingByDefault(because = "Downloaded tools are cached in their installation directory")
abstract class DownloadToolArchive : DefaultTask() {

    private companion object {
        const val DOWNLOADING = "Downloading tool archive {}."
        const val VERIFIED = "Verified SHA-256 {} of {}."

        const val CONNECT_TIMEOUT_MILLISECONDS: Int = 30_000
        const val READ_TIMEOUT_MILLISECONDS: Int = 120_000

        const val PATH_SEPARATOR: Char = '/'

        const val ZIP_EXTENSION: String = ".zip"
        const val GZIP_TAR_EXTENSION: String = ".tar.gz"
        const val XZ_TAR_EXTENSION: String = ".tar.xz"

        const val UNPACKED_TAR: String = "archive.tar"

        const val EXECUTABLE_PERMISSIONS: String = "rwxr-xr-x"

        val SHA256_DIGEST: Regex = Regex("[0-9a-f]{64}")
    }

    @get:Input
    abstract val archiveUrl: Property<String>

    @get:Input
    abstract val sha256: Property<String>

    @get:Input
    abstract val executablePath: Property<String>

    @get:OutputDirectory
    abstract val installationDirectory: DirectoryProperty

    @get:Inject
    protected abstract val archives: ArchiveOperations

    @get:Inject
    protected abstract val files: FileSystemOperations

    @TaskAction
    fun install() {
        val address = archiveUrl.get()
        val expected = sha256.get()
        if (!expected.matches(SHA256_DIGEST)) {
            val message = "Pinned SHA-256 of $address must be 64 lowercase hexadecimal digits!"
            throw GradleException(message)
        }
        val archive = temporaryDir.resolve(URI(address).path.substringAfterLast(PATH_SEPARATOR))
        logger.lifecycle(DOWNLOADING, address)
        try {
            val connection = URI(address).toURL().openConnection()
            connection.connectTimeout = CONNECT_TIMEOUT_MILLISECONDS
            connection.readTimeout = READ_TIMEOUT_MILLISECONDS
            connection.getInputStream().use { input -> archive.outputStream().use { output -> input.copyTo(output) } }
        } catch (exception: IOException) {
            val message = "Tool archive $address could not be downloaded!"
            throw GradleException(message, exception)
        }
        val actual = ToolDigests.sha256(archive)
        if (actual != expected) {
            archive.delete()
            val message = "Tool archive $address has SHA-256 $actual instead of the pinned $expected, so it was not installed!"
            throw GradleException(message)
        }
        logger.lifecycle(VERIFIED, expected, address)
        val formatMessage = "Tool archive $address must be a .zip, .tar.gz or .tar.xz file!"
        val contents = when {
            archive.name.endsWith(ZIP_EXTENSION) -> archives.zipTree(archive)
            archive.name.endsWith(GZIP_TAR_EXTENSION) -> archives.tarTree(archive)
            archive.name.endsWith(XZ_TAR_EXTENSION) -> {
                val unpacked = temporaryDir.resolve(UNPACKED_TAR)
                XZInputStream(archive.inputStream()).use { input -> unpacked.outputStream().use { output -> input.copyTo(output) } }
                archives.tarTree(unpacked)
            }
            else -> throw GradleException(formatMessage)
        }
        files.sync {
            from(contents)
            into(installationDirectory)
            filesMatching(executablePath.get()) {
                permissions { unix(EXECUTABLE_PERMISSIONS) }
            }
        }
        if (!installationDirectory.file(executablePath).get().asFile.isFile) {
            val message = "Tool archive $address must contain ${executablePath.get()} to be installed!"
            throw GradleException(message)
        }
    }
}
