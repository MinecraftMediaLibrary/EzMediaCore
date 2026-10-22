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

/**
 * Lists the classes of the MCAV modules that a plugin's own classes use, as a resource of the plugin.
 *
 * The resource holds one class name in internal form on each line, sorted. The modules are downloaded apart from the
 * plugin, so its loader reads the list and refuses to start when a module lacks one of the classes.
 */
@CacheableTask
abstract class RequiredModuleClassesTask : DefaultTask() {

    /**
     * Holds the path of the list.
     */
    companion object {

        /**
         * The path of the list inside the plugin.
         */
        const val RESOURCE: String = "META-INF/mcav/required-classes.txt"

        private const val LINE_SEPARATOR: String = "\n"
    }

    /**
     * The class directories of the plugin.
     */
    @get:Classpath
    abstract val pluginClasses: ConfigurableFileCollection

    /**
     * The class directories and jars of the modules that the plugin downloads.
     */
    @get:Classpath
    abstract val modules: ConfigurableFileCollection

    /**
     * The resource directory that receives the list at [RESOURCE].
     */
    @get:OutputDirectory
    abstract val output: DirectoryProperty

    /**
     * Writes the list of the module classes that the plugin uses.
     */
    @TaskAction
    fun list(): Unit {
        val classes: SortedSet<String> = ModuleClasses.required(this.pluginClasses.files, this.modules.files)
        val text: String = classes.joinToString(LINE_SEPARATOR, postfix = LINE_SEPARATOR)
        val file: File = this.output.get().file(RESOURCE).asFile
        file.parentFile.mkdirs()
        file.writeText(text)
    }
}
