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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import me.brandonli.mcav.gradle.BuildMcv2Natives.Work.Parameters
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.process.ExecOperations
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class BuildMcv2NativesTest {

    @TempDir
    lateinit var directory: Path

    abstract class RecordingBuild : BuildMcv2Natives() {
        lateinit var recordedProcesses: ExecOperations
        lateinit var recordedWorkers: WorkerExecutor
        override val processes: ExecOperations get() = recordedProcesses
        override val workers: WorkerExecutor get() = recordedWorkers
    }

    class RecordingWork(private val recordedParameters: Parameters, override val processes: ExecOperations) : BuildMcv2Natives.Work() {
        override fun getParameters() = recordedParameters
    }

    private lateinit var processes: RecordedProcesses

    private val commands get() = processes.commands.map { it.drop(1) }

    private val submitted = mutableListOf<RecordingWork>()

    @Test
    fun handsTheCompilesAndLinksToAGradleWorker() {
        val task = compiler("test-version")
        task.build()
        assertEquals(listOf(listOf("version")), commands)
        assertTrue(task.outputDirectory.get().file("stale.dll").asFile.exists())
        submitted.single().execute()
        assertEquals(22, commands.count { it.first() == "c++" })
        assertEquals(6, commands.count { it.first() == "cc" })
        assertFalse(task.outputDirectory.get().file("stale.dll").asFile.exists())
    }

    @Test
    fun buildsAllTargetsWithTheirBaselineAndOnlyTheirOwnExtensions() {
        val task = compiler("test-version")
        buildWithWorkers(task)
        val links = commands.filter { it.first() == "cc" }
        assertEquals(setOf("x86_64-linux-gnu.2.28", "aarch64-linux-gnu.2.28", "x86_64-windows-gnu",
            "aarch64-windows-gnu", "x86_64-macos.11.0", "aarch64-macos.11.0"), links.map { it[2] }.toSet())
        assertEquals(6, links.size)
        assertTrue(links.all { "-shared" in it && "-s" in it })
        assertTrue(links.filter { "linux" in it[2] }.all { "-Wl,-z,noexecstack" in it })
        assertTrue(links.filter { "macos" in it[2] }.all { "-Wl,-install_name,@rpath/libmcv2kernels.dylib" in it })
        val compiles = commands.filter { it.first() == "c++" }
        assertEquals(22, compiles.size)
        for (command in compiles) {
            assertTrue("-mcpu=baseline" in command)
            assertTrue("-ffp-contract=off" in command && "-fwrapv" in command && "-fno-strict-aliasing" in command)
            assertFalse(command.any { "native" == it.substringAfter('=') || it == "-ffast-math" })
            val source = command[command.indexOf("-c") + 1]
            assertEquals(source == "level_avx2.cpp", "-mavx2" in command)
            assertEquals(source == "level_avx512.cpp", "-mavx512f" in command)
            assertEquals(source.startsWith("level_sve"), "-mcpu=baseline+sve" in command)
            assertEquals(command[2].startsWith("x86_64") && "macos" !in command[2], "-mbranches-within-32B-boundaries" in command)
        }
        assertTrue(processes.environments.drop(1).all { it["SOURCE_DATE_EPOCH"] == "0" })
        val natives = task.outputDirectory.get().asFile.resolve("mcav/mcv2/natives")
        val expectedPaths = setOf("linux-x86_64/libmcv2kernels.so", "linux-aarch64/libmcv2kernels.so",
            "macos-x86_64/libmcv2kernels.dylib", "macos-aarch64/libmcv2kernels.dylib",
            "windows-x86_64/mcv2kernels.dll", "windows-aarch64/mcv2kernels.dll")
        val sums = natives.resolve("SHA256SUMS").readLines()
        assertEquals(expectedPaths, sums.map { it.substringAfter("  ") }.toSet())
        assertTrue(sums.all { it.startsWith("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad  ") })
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad  mcv2.cpp\n",
            natives.resolve("SOURCES").readText())
        assertFalse(task.outputDirectory.get().file("stale.dll").asFile.exists())
        assertFalse(natives.walkTopDown().any { it.extension == "lib" })
    }

    @Test
    fun compilesAsManyObjectsAtOnceAsItHasProcesses() {
        val bothStarted = CountDownLatch(2)
        val running = AtomicInteger()
        val mostAtOnce = AtomicInteger()
        val task = compiler("test-version", 2) {
            mostAtOnce.accumulateAndGet(running.incrementAndGet(), ::maxOf)
            bothStarted.countDown()
            bothStarted.await(30, TimeUnit.SECONDS)
            running.decrementAndGet()
        }
        buildWithWorkers(task)
        assertEquals(2, mostAtOnce.get())
        assertEquals(22, commands.count { it.first() == "c++" })
    }

    @Test
    fun aFailingCompileFailsTheBuildBeforeAnyLink() {
        val failure = IllegalStateException("zig: error: unable to compile")
        val task = compiler("test-version") { arguments ->
            if ("level_avx2.cpp" in arguments) {
                throw failure
            }
        }
        val thrown = assertThrows(GradleException::class.java) { buildWithWorkers(task) }
        assertEquals("Zig could not build MCV2's natives", thrown.message)
        assertSame(failure, thrown.cause)
        assertTrue(commands.none { it.first() == "cc" })
    }

    @Test
    fun wrongCompilerVersionFailsBeforeReplacingAnyOutput() {
        val task = compiler("wrong-version")
        val failure = assertThrows(GradleException::class.java) { task.build() }
        val zig = directory.resolve("zig").toString()
        assertEquals("$zig must be zig test-version, but it is wrong-version", failure.message)
        assertTrue(task.outputDirectory.get().file("stale.dll").asFile.exists())
        assertEquals(listOf(listOf(zig, "version")), processes.commands)
        assertTrue(submitted.isEmpty())
    }

    private fun buildWithWorkers(task: RecordingBuild) {
        task.build()
        submitted.forEach { it.execute() }
    }

    private fun compiler(version: String, processCount: Int = 4, onCompile: (List<String>) -> Unit = {}): RecordingBuild {
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        val task = project.tasks.register("natives", RecordingBuild::class.java).get()
        task.zigVersion.set("test-version")
        task.zigExecutable.set(directory.resolve("zig").toFile())
        task.sourcesDirectory.set(directory.resolve("sources").toFile().apply { mkdirs() })
        task.sourcesDirectory.file("mcv2.cpp").get().asFile.writeText("abc")
        task.sourcesDirectory.file(".clang-format").get().asFile.writeText("formatter")
        task.outputDirectory.set(directory.resolve("output").toFile().apply { mkdirs() })
        task.outputDirectory.file("stale.dll").get().asFile.writeText("stale")
        task.cacheDirectory.set(directory.resolve("cache").toFile())
        task.processCount.set(processCount)
        processes = RecordedProcesses("$version\n") { command ->
            val arguments = command.drop(1)
            if (arguments != listOf("version")) {
                if (arguments.first() == "c++") {
                    onCompile(arguments)
                }
                val output = File(arguments[arguments.indexOf("-o") + 1])
                output.writeText("abc")
                if (output.extension == "dll") output.resolveSibling("mcv2kernels.lib").writeText("unused")
            }
        }
        task.recordedProcesses = processes.operations
        val queue = mock(WorkQueue::class.java)
        doAnswer { invocation ->
            assertEquals(BuildMcv2Natives.Work::class.java, invocation.getArgument<Class<*>>(0))
            val parameters = project.objects.newInstance(Parameters::class.java)
            invocation.getArgument<Action<Parameters>>(1).execute(parameters)
            submitted.add(RecordingWork(parameters, processes.operations))
        }.`when`(queue).submit(any<Class<BuildMcv2Natives.Work>>(), any<Action<Parameters>>())
        val workers = mock(WorkerExecutor::class.java)
        `when`(workers.noIsolation()).thenReturn(queue)
        task.recordedWorkers = workers
        return task
    }
}
