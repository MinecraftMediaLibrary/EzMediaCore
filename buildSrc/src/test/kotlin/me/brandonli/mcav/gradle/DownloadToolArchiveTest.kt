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

import java.nio.file.Path
import java.security.MessageDigest
import java.util.Base64
import java.util.HexFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class DownloadToolArchiveTest {

    private companion object {

        const val TASK_NAME: String = "downloadTool"

        const val ARCHIVE_FILE: String = "tool.zip"
        const val MISSING_ARCHIVE: String = "missing.zip"
        const val EXECUTABLE_PATH: String = "tool/bin/tool"
        const val INSTALLATION_DIRECTORY: String = "installed"
        const val INSTALLED_EXECUTABLE: String = "installed/tool/bin/tool"
        const val MISSING_EXECUTABLE_PATH: String = "missing"
        const val SEVEN_ZIP_ARCHIVE: String = "tool.7z"

        const val EXECUTABLE_CONTENT: String = "verified executable\n"

        const val DIGEST_ALGORITHM: String = "SHA-256"
    }

    @TempDir
    lateinit var directory: Path

    @Test
    fun shouldInstallTheVerifiedZipAndMakeItsToolExecutable() {
        val task = zippedTool()
        task.install()
        val installed = directory.resolve(INSTALLED_EXECUTABLE).toFile()
        val content: String = installed.readText()
        assertEquals(EXECUTABLE_CONTENT, content)

        val executable: Boolean = installed.canExecute()
        assertTrue(executable)
    }

    @Test
    fun shouldNeitherExtractUntrustedBytesNorReplaceAnExistingToolOnAChecksumMismatch() {
        val task = zippedTool()
        val installed = directory.resolve(INSTALLED_EXECUTABLE).toFile()
        installed.parentFile.mkdirs()
        installed.writeText("previous verified tool\n")
        val expected = "0".repeat(64)
        val actual = task.sha256.get()
        task.sha256.set(expected)
        val failure = assertThrows(GradleException::class.java) { task.install() }
        val message: String? = failure.message
        assertEquals(
            "Tool archive ${task.archiveUrl.get()} has SHA-256 $actual instead of the pinned $expected, so it was not installed!",
            message
        )

        val content: String = installed.readText()
        assertEquals("previous verified tool\n", content)

        val archiveKept: Boolean = task.temporaryDir.resolve(ARCHIVE_FILE).exists()
        assertFalse(archiveKept)
    }

    @Test
    fun shouldFailOnAnInvalidChecksumBeforeDownloading() {
        val task = zippedTool()
        task.sha256.set("missing")
        val failure = assertThrows(GradleException::class.java) { task.install() }
        val message: String? = failure.message
        assertEquals("Pinned SHA-256 of ${task.archiveUrl.get()} must be 64 lowercase hexadecimal digits!", message)

        val installed: Boolean = directory.resolve(INSTALLATION_DIRECTORY).toFile().exists()
        assertFalse(installed)
    }

    @Test
    fun shouldNameTheUnavailableArchiveWhenTheDownloadFails() {
        val task = zippedTool()
        task.archiveUrl.set(directory.resolve(MISSING_ARCHIVE).toUri().toString())
        val failure = assertThrows(GradleException::class.java) { task.install() }
        val message: String? = failure.message
        assertEquals("Tool archive ${task.archiveUrl.get()} could not be downloaded!", message)

        val installed: Boolean = directory.resolve(INSTALLATION_DIRECTORY).toFile().exists()
        assertFalse(installed)
    }

    @Test
    fun shouldRequireTheRequestedExecutableInAVerifiedArchive() {
        val task = zippedTool()
        task.executablePath.set(MISSING_EXECUTABLE_PATH)
        val failure = assertThrows(GradleException::class.java) { task.install() }
        val message: String? = failure.message
        assertEquals("Tool archive ${task.archiveUrl.get()} must contain missing to be installed!", message)
    }

    @Test
    fun shouldNotUnpackAVerifiedArchiveOfAnotherFormat() {
        val task = zippedTool()
        val archive = directory.resolve(SEVEN_ZIP_ARCHIVE).toFile()
        directory.resolve(ARCHIVE_FILE).toFile().copyTo(archive)
        task.archiveUrl.set(archive.toURI().toString())
        val failure = assertThrows(GradleException::class.java) { task.install() }
        val message: String? = failure.message
        assertEquals("Tool archive ${task.archiveUrl.get()} must be a .zip, .tar.gz or .tar.xz file!", message)

        val installed: Boolean = directory.resolve(INSTALLATION_DIRECTORY).toFile().exists()
        assertFalse(installed)
    }

    @Test
    fun shouldExtractGzipAndXzArchivesWithoutAnExternalUnpacker() {
        val archives = mapOf(
            "tar.gz" to "H4sIAAAAAAACA+3NQQqCQAAF0Fl3Cm/gWInn0ZpAkAQbo+M3tQnaF0Hvbf7nb36e56kexnOdSwmfEYuubZ9ZvGeM2/2rP/Ym7romVDF8wXrJ/VLu" +
                "w3+6pmU8jelYpVs6rLkfprQJAAAAAAAAAAAA/Lw7jjgM0wAoAAA=",
            "tar.xz" to "/Td6WFoAAATm1rRGAgAhARYAAAB0L+Wj4Cf/AHNdADob7NgvJU6bwGN6v5a3JejC7neVC0X31phps0q2DkY/2CgXahtiZSmSEfj8sTJH859ZU5Ir" +
                "KQ3BNzSNn9Lgm5Pjp01y3THUEfiMj5qL+5NnvL7IiCthCBuqOOi+oVXsi4XON87LGOIoT5AAvVZcrNZukgAAAEjvLyQMjRxeAAGPAYBQAADrwzaQscRn+wIA" +
                "AAAABFla"
        )
        val installed: Map<String, Pair<String, Boolean>> = archives.mapValues { (extension, encoded) ->
            val archive = directory.resolve("tool.$extension").toFile()
            archive.writeBytes(Base64.getDecoder().decode(encoded))
            val project = ProjectBuilder.builder().withProjectDir(directory.resolve(extension).toFile()).build()
            val task = project.tasks.register(TASK_NAME, DownloadToolArchive::class.java).get()
            task.archiveUrl.set(archive.toURI().toString())
            task.sha256.set(HexFormat.of().formatHex(MessageDigest.getInstance(DIGEST_ALGORITHM).digest(archive.readBytes())))
            task.executablePath.set(EXECUTABLE_PATH)
            task.installationDirectory.set(directory.resolve("installed-$extension").toFile())
            task.install()
            val tool = directory.resolve("installed-$extension/tool/bin/tool").toFile()
            tool.readText() to tool.canExecute()
        }
        val expected: Map<String, Pair<String, Boolean>> = archives.mapValues { EXECUTABLE_CONTENT to true }
        assertEquals(expected, installed)
    }

    private fun zippedTool(): DownloadToolArchive {
        val archive = directory.resolve(ARCHIVE_FILE).toFile()
        ZipOutputStream(archive.outputStream()).use { output ->
            output.putNextEntry(ZipEntry(EXECUTABLE_PATH))
            output.write(EXECUTABLE_CONTENT.toByteArray())
            output.closeEntry()
        }
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        return project.tasks.register(TASK_NAME, DownloadToolArchive::class.java).get().apply {
            archiveUrl.set(archive.toURI().toString())
            sha256.set(HexFormat.of().formatHex(MessageDigest.getInstance(DIGEST_ALGORITHM).digest(archive.readBytes())))
            executablePath.set(EXECUTABLE_PATH)
            installationDirectory.set(directory.resolve(INSTALLATION_DIRECTORY).toFile())
        }
    }
}
