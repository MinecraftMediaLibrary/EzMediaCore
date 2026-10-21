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
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class UvEnvironmentTest {

    private companion object {

        const val INSTALLATION_DIRECTORY: String = "python-installation"
        const val CACHE_DIRECTORY: String = "uv-cache"
        const val PYTHON_DIRECTORY: String = "python"
        const val BINARY_DIRECTORY: String = "bin"
    }

    @TempDir
    lateinit var directory: Path

    @Test
    fun shouldKeepUvConfigurationCachesAndPythonInsideTheBuild(): Unit {
        val installation: File = this.directory.resolve(INSTALLATION_DIRECTORY).toFile()
        val cache: File = this.directory.resolve(CACHE_DIRECTORY).toFile()
        val variables: Map<String, String> = UvEnvironment.variables(installation, cache)
        val expected: Map<String, String> = mapOf(
            "UV_NO_CONFIG" to "1",
            "UV_CACHE_DIR" to cache.absolutePath,
            "UV_PYTHON_CACHE_DIR" to cache.resolve(PYTHON_DIRECTORY).absolutePath,
            "UV_PYTHON_INSTALL_DIR" to installation.resolve(PYTHON_DIRECTORY).absolutePath,
            "UV_PYTHON_BIN_DIR" to installation.resolve(BINARY_DIRECTORY).absolutePath,
            "UV_PYTHON_PREFERENCE" to "only-managed",
            "UV_LINK_MODE" to "copy"
        )
        assertEquals(expected, variables)
    }
}
