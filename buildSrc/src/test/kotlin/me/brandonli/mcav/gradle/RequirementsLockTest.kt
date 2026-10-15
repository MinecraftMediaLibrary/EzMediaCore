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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RequirementsLockTest {

    private companion object {

        const val JUPYTER_BOOK: String = "jupyter-book"
        const val JUPYTER_BOOK_VERSION: String = "1.0.4.post1"
        const val NUMPY: String = "numpy"
        const val NUMPY_VERSION: String = "2.5.3"
        const val SPHINX_DESIGN: String = "sphinx-design"
    }

    private val lock = """
        # This file is part of mcav, a media playback library for Java

        jupyter-book==1.0.4.post1 \
            --hash=sha256:2c9fb7cd8a5ab9e4e2e1bc4a0b9fe2f6e56e7b0ff8d6f3e1e2d6a3a7c3d1e0b1
        numpy==2.5.3 ; python_full_version >= '3.13' \
            --hash=sha256:9f1c3f0a6f0e2a5a8b0a3e7c1d2b4f6a8c0e2d4f6a8b0c2e4f6a8b0c2d4e6f8a
        sphinx-design==0.7.0 \
            --hash=sha256:0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c
    """.trimIndent()

    @Test
    fun shouldMissNoPinOfALockWithEveryPin() {
        val versions = mapOf(JUPYTER_BOOK to JUPYTER_BOOK_VERSION, NUMPY to NUMPY_VERSION, SPHINX_DESIGN to "0.7.0")
        val missing: List<String> = RequirementsLock.missingPins(this.lock, versions)
        assertEquals(emptyList<String>(), missing)
    }

    @Test
    fun shouldNameTheChangedAndTheAbsentPins() {
        val versions = mapOf(JUPYTER_BOOK to JUPYTER_BOOK_VERSION, NUMPY to "2.6.0", "matplotlib" to "3.11.2")
        val missing: List<String> = RequirementsLock.missingPins(this.lock, versions)
        assertEquals(listOf("numpy==2.6.0", "matplotlib==3.11.2"), missing)
    }

    @Test
    fun shouldNotTreatCommentsAndHashesAsPins() {
        val lock = "# numpy==2.5.3\n    --hash=sha256:9f1c3f0a6f0e2a5a8b0a3e7c1d2b4f6a8c0e2d4f6a8b0c2e4f6a8b0c2d4e6f8a\n"
        val missing: List<String> = RequirementsLock.missingPins(lock, mapOf(NUMPY to NUMPY_VERSION))
        assertEquals(listOf("numpy==2.5.3"), missing)
    }

    @Test
    fun shouldMatchAPinAgainstTheWholeVersion() {
        val versions = mapOf(JUPYTER_BOOK to "1.0.4", SPHINX_DESIGN to "0.7")
        val missing: List<String> = RequirementsLock.missingPins(this.lock, versions)
        assertEquals(listOf("jupyter-book==1.0.4", "sphinx-design==0.7"), missing)
    }
}
