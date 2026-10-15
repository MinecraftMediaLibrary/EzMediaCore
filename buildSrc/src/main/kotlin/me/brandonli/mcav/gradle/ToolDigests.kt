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
import java.security.MessageDigest
import java.util.HexFormat

internal object ToolDigests {

    private const val ALGORITHM: String = "SHA-256"

    private const val BUFFER_SIZE: Int = 65_536

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance(ALGORITHM)
        file.inputStream().use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            var count = input.read(buffer)
            while (count >= 0) {
                digest.update(buffer, 0, count)
                count = input.read(buffer)
            }
        }
        return HexFormat.of().formatHex(digest.digest())
    }
}
