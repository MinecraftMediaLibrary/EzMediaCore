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

    @TempDir
    lateinit var directory: Path

    @Test
    fun shouldInstallTheVerifiedZipAndMakeItsToolExecutable() {
        val task = zippedTool()
        task.install()
        val installed = directory.resolve("installed/tool/bin/tool").toFile()
        val content: String = installed.readText()
        assertEquals("verified executable\n", content)

        val executable: Boolean = installed.canExecute()
        assertTrue(executable)
    }

    @Test
    fun shouldNeitherExtractUntrustedBytesNorReplaceAnExistingToolOnAChecksumMismatch() {
        val task = zippedTool()
        val installed = directory.resolve("installed/tool/bin/tool").toFile()
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

        val archiveKept: Boolean = task.temporaryDir.resolve("tool.zip").exists()
        assertFalse(archiveKept)
    }

    @Test
    fun shouldFailOnAnInvalidChecksumBeforeDownloading() {
        val task = zippedTool()
        task.sha256.set("missing")
        val failure = assertThrows(GradleException::class.java) { task.install() }
        val message: String? = failure.message
        assertEquals("Pinned SHA-256 of ${task.archiveUrl.get()} must be 64 lowercase hexadecimal digits!", message)

        val installed: Boolean = directory.resolve("installed").toFile().exists()
        assertFalse(installed)
    }

    @Test
    fun shouldNameTheUnavailableArchiveWhenTheDownloadFails() {
        val task = zippedTool()
        task.archiveUrl.set(directory.resolve("missing.zip").toUri().toString())
        val failure = assertThrows(GradleException::class.java) { task.install() }
        val message: String? = failure.message
        assertEquals("Tool archive ${task.archiveUrl.get()} could not be downloaded!", message)

        val installed: Boolean = directory.resolve("installed").toFile().exists()
        assertFalse(installed)
    }

    @Test
    fun shouldRequireTheRequestedExecutableInAVerifiedArchive() {
        val task = zippedTool()
        task.executablePath.set("missing")
        val failure = assertThrows(GradleException::class.java) { task.install() }
        val message: String? = failure.message
        assertEquals("Tool archive ${task.archiveUrl.get()} must contain missing to be installed!", message)
    }

    @Test
    fun shouldNotUnpackAVerifiedArchiveOfAnotherFormat() {
        val task = zippedTool()
        val archive = directory.resolve("tool.7z").toFile()
        directory.resolve("tool.zip").toFile().copyTo(archive)
        task.archiveUrl.set(archive.toURI().toString())
        val failure = assertThrows(GradleException::class.java) { task.install() }
        val message: String? = failure.message
        assertEquals("Tool archive ${task.archiveUrl.get()} must be a .zip, .tar.gz or .tar.xz file!", message)

        val installed: Boolean = directory.resolve("installed").toFile().exists()
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
            val task = project.tasks.register("downloadTool", DownloadToolArchive::class.java).get()
            task.archiveUrl.set(archive.toURI().toString())
            task.sha256.set(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(archive.readBytes())))
            task.executablePath.set("tool/bin/tool")
            task.installationDirectory.set(directory.resolve("installed-$extension").toFile())
            task.install()
            val tool = directory.resolve("installed-$extension/tool/bin/tool").toFile()
            tool.readText() to tool.canExecute()
        }
        val expected: Map<String, Pair<String, Boolean>> = archives.mapValues { "verified executable\n" to true }
        assertEquals(expected, installed)
    }

    private fun zippedTool(): DownloadToolArchive {
        val archive = directory.resolve("tool.zip").toFile()
        ZipOutputStream(archive.outputStream()).use { output ->
            output.putNextEntry(ZipEntry("tool/bin/tool"))
            output.write("verified executable\n".toByteArray())
            output.closeEntry()
        }
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        return project.tasks.register("downloadTool", DownloadToolArchive::class.java).get().apply {
            archiveUrl.set(archive.toURI().toString())
            sha256.set(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(archive.readBytes())))
            executablePath.set("tool/bin/tool")
            installationDirectory.set(directory.resolve("installed").toFile())
        }
    }
}
