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
import java.util.SortedSet
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class RequiredModuleClassesTask : DefaultTask() {

    companion object {

        const val RESOURCE: String = "META-INF/mcav/required-classes.txt"

        private const val LINE_SEPARATOR: String = "\n"
    }

    @get:Classpath
    abstract val pluginClasses: ConfigurableFileCollection

    @get:Classpath
    abstract val modules: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @TaskAction
    fun list(): Unit {
        val classes: SortedSet<String> = ModuleClasses.required(this.pluginClasses.files, this.modules.files)
        val file: File = this.output.get().file(RESOURCE).asFile
        file.parentFile.mkdirs()
        file.writeText(classes.joinToString(LINE_SEPARATOR, postfix = LINE_SEPARATOR))
    }
}
