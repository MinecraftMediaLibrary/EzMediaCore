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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TaskDescriptionsTest {

    @Test
    fun everyRegisteredTaskStartsWithItsGroupAndAOneLineDescriptionInTheThirdPerson() {
        val conventions = File("src/main/kotlin").listFiles { file -> file.name.endsWith(".gradle.kts") }!!.toList()
        val scripts = conventions + File("build.gradle.kts")
        val registration = Regex("""tasks\.register(<\w+>)?\("\w+"\) \{\n( +)group = "[a-z ]+"\n\2description = "[A-Z][a-z]+s [^"\n]*[^."]"\n""")
        for (script in scripts.sorted()) {
            val text = script.readText()
            assertEquals(Regex("""tasks\.register\b""").findAll(text).count(), registration.findAll(text).count(), script.path)
        }
    }
}
