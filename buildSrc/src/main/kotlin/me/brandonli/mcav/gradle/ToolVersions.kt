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

import java.io.ByteArrayOutputStream
import org.gradle.api.GradleException
import org.gradle.process.ExecOperations

object ToolVersions {

    private const val VERSION_ARGUMENT: String = "--version"

    private const val VERSION_GROUP: Int = 1

    fun verify(
        processes: ExecOperations,
        executable: Any,
        tool: String,
        expected: String,
        prefix: String = "$tool ",
        argument: String = VERSION_ARGUMENT
    ): Unit {
        val output: ByteArrayOutputStream = ByteArrayOutputStream()
        processes.exec {
            commandLine(executable, argument)
            standardOutput = output
        }
        val reported: String = output.toString(Charsets.UTF_8).trim()
        val actual: String? = Regex("(?:^|\\s)${Regex.escape(prefix)}(\\S+)").find(reported)?.groupValues?.get(VERSION_GROUP)
        if (actual != expected) {
            val message: String = "$executable reports $reported, which is not the $tool $expected this build pins!"
            throw GradleException(message)
        }
    }
}
