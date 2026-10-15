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

import javax.inject.Inject
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

@CacheableTask
abstract class JupyterBook : PythonTask() {

    private companion object {

        const val BOOK_DIRECTORY: String = "book"
        const val SITE_DIRECTORY: String = "site"
        const val HTML_DIRECTORY: String = "site/_build/html"

        const val ENTRY_POINT: String = "from jupyter_book.cli.main import main\nmain()"

        const val JUPYTER_CONFIG_VARIABLE: String = "JUPYTER_CONFIG_DIR"
        const val JUPYTER_CONFIG_DIRECTORY: String = "jupyter"
    }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    protected abstract val files: FileSystemOperations

    @get:Inject
    protected abstract val processes: ExecOperations

    @TaskAction
    fun build() {
        val staging = temporaryDir
        files.delete { delete(staging) }
        val book = staging.resolve(BOOK_DIRECTORY)
        files.sync {
            from(sources)
            into(book)
        }
        val commands = listOf(
            listOf("config", "sphinx", BOOK_DIRECTORY),
            listOf("build", BOOK_DIRECTORY, "--path-output", SITE_DIRECTORY, "--warningiserror", "--keep-going", "--all")
        )
        commands.forEach { arguments ->
            processes.exec {
                workingDir(staging)
                commandLine(listOf(pythonExecutable.get().asFile, "-c", ENTRY_POINT) + arguments)
                environment(NO_USER_SITE_VARIABLE, ENABLED)
                environment(NO_BYTECODE_VARIABLE, ENABLED)
                environment(HASH_SEED_VARIABLE, FIXED_HASH_SEED)
                environment(MATPLOTLIB_CONFIG_VARIABLE, staging.resolve(MATPLOTLIB_CONFIG_DIRECTORY).absolutePath)
                environment(JUPYTER_CONFIG_VARIABLE, staging.resolve(JUPYTER_CONFIG_DIRECTORY).absolutePath)
            }
        }
        files.sync {
            from(staging.resolve(HTML_DIRECTORY))
            into(outputDirectory)
        }
    }
}
