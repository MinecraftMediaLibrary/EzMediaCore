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

import java.io.OutputStream
import org.gradle.api.Action
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.RETURNS_SELF
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.mockito.invocation.Invocation

class RecordedProcesses(private val versionOutput: String, private val onCommand: (List<String>) -> Unit = {}) {

    val commands: MutableList<List<String>> = mutableListOf<List<String>>()

    val environments: MutableList<Map<String, Any?>> = mutableListOf<Map<String, Any?>>()

    val workingDirectories: MutableList<Any?> = mutableListOf<Any?>()

    val operations: ExecOperations = mock(ExecOperations::class.java)

    init {
        doAnswer { invocation ->
            val specification: ExecSpec = mock(ExecSpec::class.java, RETURNS_SELF)
            invocation.getArgument<Action<ExecSpec>>(0).execute(specification)
            val calls: Collection<Invocation> = mockingDetails(specification).invocations
            val command: List<String> = when (val arguments: Any = calls.single { it.method.name == "commandLine" }.rawArguments[0]) {
                is Array<*> -> arguments.toList()
                else -> (arguments as Iterable<*>).toList()
            }.map { it.toString() }
            val environment: MutableMap<String, Any?> = mutableMapOf<String, Any?>()
            calls.filter { it.method.name == "environment" }.forEach { call ->
                when (call.rawArguments.size) {
                    1 -> (call.rawArguments[0] as Map<*, *>).forEach { (name, value) -> environment[name as String] = value }
                    else -> environment[call.rawArguments[0] as String] = call.rawArguments[1]
                }
            }
            synchronized(this) {
                commands += command
                environments += environment
                workingDirectories += calls.singleOrNull { it.method.name == "workingDir" }?.rawArguments?.get(0)
            }
            if (command.last() == "--version" || command.last() == "version") {
                (calls.single { it.method.name == "setStandardOutput" }.rawArguments[0] as OutputStream).write(versionOutput.toByteArray())
            }
            onCommand(command)
            mock(ExecResult::class.java)
        }.`when`(operations).exec(any())
    }
}
