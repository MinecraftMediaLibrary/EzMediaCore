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
import java.io.IOException
import java.net.URI
import java.net.URLConnection
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.tukaani.xz.XZInputStream

@DisableCachingByDefault(because = "Downloaded tools are cached in their installation directory")
abstract class DownloadToolArchive : DefaultTask() {

    private companion object {

        const val DOWNLOADING: String = "Downloading tool archive {}."
        const val VERIFIED: String = "Verified SHA-256 {} of {}."

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
    fun install(): Unit {
        val address: String = this.archiveUrl.get()
        val expected: String = this.sha256.get()
        if (!expected.matches(SHA256_DIGEST)) {
            val message: String = "Pinned SHA-256 of $address must be 64 lowercase hexadecimal digits!"
            throw GradleException(message)
        }
        val archive: File = this.temporaryDir.resolve(URI(address).path.substringAfterLast(PATH_SEPARATOR))
        this.logger.lifecycle(DOWNLOADING, address)
        try {
            val connection: URLConnection = URI(address).toURL().openConnection()
            connection.connectTimeout = CONNECT_TIMEOUT_MILLISECONDS
            connection.readTimeout = READ_TIMEOUT_MILLISECONDS
            connection.getInputStream().use { input -> archive.outputStream().use { output -> input.copyTo(output) } }
        } catch (exception: IOException) {
            val message: String = "Tool archive $address could not be downloaded!"
            throw GradleException(message, exception)
        }
        val actual: String = ToolDigests.sha256(archive)
        if (actual != expected) {
            archive.delete()
            val message: String = "Tool archive $address has SHA-256 $actual instead of the pinned $expected, so it was not installed!"
            throw GradleException(message)
        }
        this.logger.lifecycle(VERIFIED, expected, address)
        val formatMessage: String = "Tool archive $address must be a .zip, .tar.gz or .tar.xz file!"
        val contents: FileTree = when {
            archive.name.endsWith(ZIP_EXTENSION) -> this.archives.zipTree(archive)
            archive.name.endsWith(GZIP_TAR_EXTENSION) -> this.archives.tarTree(archive)
            archive.name.endsWith(XZ_TAR_EXTENSION) -> {
                val unpacked: File = this.temporaryDir.resolve(UNPACKED_TAR)
                XZInputStream(archive.inputStream()).use { input -> unpacked.outputStream().use { output -> input.copyTo(output) } }
                this.archives.tarTree(unpacked)
            }
            else -> throw GradleException(formatMessage)
        }
        val installation: DirectoryProperty = this.installationDirectory
        val executable: String = this.executablePath.get()
        this.files.sync {
            this.from(contents)
            this.into(installation)
            this.filesMatching(executable) {
                this.permissions { this.unix(EXECUTABLE_PERMISSIONS) }
            }
        }
        if (!this.installationDirectory.file(this.executablePath).get().asFile.isFile) {
            val message: String = "Tool archive $address must contain ${this.executablePath.get()} to be installed!"
            throw GradleException(message)
        }
    }
}
