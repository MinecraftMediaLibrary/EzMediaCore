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
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ModuleClassesTest {

    @TempDir
    lateinit var folder: Path

    private fun classBytes(vararg names: String): ByteArray =
        names.joinToString("\u0001\u0000\u0010", prefix = "Êþº¾", postfix = "\u0007") { it }
            .toByteArray(Charsets.ISO_8859_1)

    private fun jar(name: String, vararg classes: String): File {
        val file = folder.resolve(name).toFile()
        ZipOutputStream(file.outputStream()).use { zip ->
            classes.forEach {
                zip.putNextEntry(ZipEntry("$it.class"))
                zip.closeEntry()
            }
            zip.putNextEntry(ZipEntry("META-INF/MANIFEST.MF"))
            zip.closeEntry()
        }
        return file
    }

    @Test
    fun readsTheModuleClassesAClassFileNamesInItsDescriptorsAndSignatures() {
        val bytes = classBytes(
            "me/brandonli/mcav/bukkit/media/mcv2/Mcv2Result",
            "(Lme/brandonli/mcav/MCAV;)V",
            "Ljava/util/List<Lme/brandonli/mcav/bukkit/media/mcv2/Mcv2Result\$Statistics;>;",
            "java/lang/String"
        )
        assertEquals(
            setOf(
                "me/brandonli/mcav/bukkit/media/mcv2/Mcv2Result",
                "me/brandonli/mcav/MCAV",
                "me/brandonli/mcav/bukkit/media/mcv2/Mcv2Result\$Statistics"
            ),
            ModuleClasses.referenced(bytes)
        )
    }

    @Test
    fun findsTheModuleClassesOfJarsAndFoldersButNoOthers() {
        val classes = folder.resolve("classes").toFile()
        File(classes, "me/brandonli/mcav/vm").mkdirs()
        File(classes, "me/brandonli/mcav/vm/VMPlayer.class").writeBytes(ByteArray(0))
        File(classes, "me/brandonli/mcav/vm/notes.txt").writeText("not a class")
        val jar = jar("mcav-common.jar", "me/brandonli/mcav/MCAV", "com/google/common/base/Preconditions")
        assertEquals(
            setOf("me/brandonli/mcav/vm/VMPlayer", "me/brandonli/mcav/MCAV"),
            ModuleClasses.available(listOf(classes, jar, folder.resolve("missing.jar").toFile()))
        )
    }

    @Test
    fun requiresTheModuleClassesThePluginUsesThatTheDownloadedModulesHold() {
        val plugin = folder.resolve("plugin").toFile()
        File(plugin, "me/brandonli/mcav/plugin").mkdirs()
        File(plugin, "me/brandonli/mcav/plugin/README.txt").writeText("not a class")
        File(plugin, "me/brandonli/mcav/plugin/MCAVSandbox.class").writeBytes(
            classBytes(
                "me/brandonli/mcav/plugin/MCAVSandbox",
                "me/brandonli/mcav/MCAV",
                "me/brandonli/mcav/voicechat/SVCFilter",
                "me/brandonli/mcav/bukkit/media/mcv2/Mcv2PackServer"
            )
        )
        val modules = listOf(
            jar("mcav-common.jar", "me/brandonli/mcav/MCAV"),
            jar("mcav-bukkit.jar", "me/brandonli/mcav/bukkit/media/mcv2/Mcv2PackServer")
        )
        assertEquals(
            listOf("me/brandonli/mcav/MCAV", "me/brandonli/mcav/bukkit/media/mcv2/Mcv2PackServer"),
            ModuleClasses.required(listOf(plugin, folder.resolve("absent").toFile()), modules).toList()
        )
    }

    @Test
    fun theTaskWritesTheRequiredClassesOneALineIntoItsResource() {
        val plugin = folder.resolve("plugin").toFile()
        File(plugin, "me/brandonli/mcav/plugin").mkdirs()
        File(plugin, "me/brandonli/mcav/plugin/MCAVSandbox.class").writeBytes(
            classBytes(
                "me/brandonli/mcav/plugin/MCAVSandbox",
                "me/brandonli/mcav/MCAV",
                "me/brandonli/mcav/bukkit/media/mcv2/Mcv2PackServer"
            )
        )
        val project = ProjectBuilder.builder().withProjectDir(folder.resolve("project").toFile()).build()
        val task = project.tasks.register("requiredModuleClasses", RequiredModuleClassesTask::class.java).get()
        task.pluginClasses.from(plugin)
        task.modules.from(
            jar("mcav-common.jar", "me/brandonli/mcav/MCAV"),
            jar("mcav-bukkit.jar", "me/brandonli/mcav/bukkit/media/mcv2/Mcv2PackServer")
        )
        task.output.set(folder.resolve("output").toFile())
        task.list()
        assertEquals(
            "me/brandonli/mcav/MCAV\nme/brandonli/mcav/bukkit/media/mcv2/Mcv2PackServer\n",
            folder.resolve("output/${RequiredModuleClassesTask.RESOURCE}").toFile().readText()
        )
    }
}
