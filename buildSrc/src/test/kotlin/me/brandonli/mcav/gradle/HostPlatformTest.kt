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

import java.util.Locale
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.dependencies.DefaultMinimalDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultMutableVersionConstraint
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HostPlatformTest {

    private companion object {

        const val JAVA_HOME: String = "/opt/jdk"
        const val PYTHON: String = "python"

        const val LWJGL_GROUP: String = "org.lwjgl"
        const val LWJGL_NAME: String = "lwjgl"
        const val LWJGL_VERSION: String = "3.3.6"
    }

    @Test
    fun shouldKeepTheArchiveAndClassifierMappingsOfTheSixDownloadHosts() {
        val hosts = listOf(
            listOf("Linux", "amd64", "x86_64", "linux", "x86_64-unknown-linux-musl", "natives-linux"),
            listOf("Linux", "arm64", "aarch64", "linux", "aarch64-unknown-linux-musl", "natives-linux-arm64"),
            listOf("Windows 11", "x86_64", "x86_64", "windows", "x86_64-pc-windows-msvc", "natives-windows"),
            listOf("Windows 11", "aarch64", "aarch64", "windows", "aarch64-pc-windows-msvc", "natives-windows-arm64"),
            listOf("Mac OS X", "amd64", "x86_64", "macos", "x86_64-apple-darwin", "natives-macos"),
            listOf("Mac OS X", "arm64", "aarch64", "macos", "aarch64-apple-darwin", "natives-macos-arm64")
        )
        val mappings: List<List<String?>> = hosts.map { expected ->
            val host = HostPlatform.normalize(expected[0], expected[1])
            listOf(expected[0], expected[1], host.architecture, host.zigSystem, host.uvTarget, host.lwjglNatives)
        }
        assertEquals(hosts, mappings)
    }

    @Test
    fun shouldKeepTheAdditionalArchitecturesOfLwjglAndRejectUnknownHosts() {
        val windowsX86: List<String?> = listOf("x86", "i386", "i686").map { HostPlatform.normalize("Windows", it).lwjglNatives }
        assertEquals(List(3) { "natives-windows-x86" }, windowsX86)

        val linuxArm32: List<String?> = listOf("arm", "arm32", "armv7l").map { HostPlatform.normalize("Linux", it).lwjglNatives }
        assertEquals(List(3) { "natives-linux-arm32" }, linuxArm32)

        val powerPc: String? = HostPlatform.normalize("Linux", "ppc64le").lwjglNatives
        assertEquals("natives-linux-ppc64le", powerPc)

        val riscV: String? = HostPlatform.normalize("Linux", "riscv64").lwjglNatives
        assertEquals("natives-linux-riscv64", riscV)

        val freeBsd: String? = HostPlatform.normalize("FreeBSD", "amd64").lwjglNatives
        assertEquals("natives-freebsd", freeBsd)

        val darwin: HostPlatform = HostPlatform.normalize("Darwin", "arm64")
        val darwinNatives: String? = darwin.lwjglNatives
        assertEquals("natives-macos-arm64", darwinNatives)

        val darwinZigSystem: String = darwin.zigSystem
        assertEquals("darwin", darwinZigSystem)

        val darwinUvTarget: String = darwin.uvTarget
        assertEquals("aarch64-unknown-linux-musl", darwinUvTarget)

        val unknownArchitectures: List<String?> = listOf("Linux", "Windows", "Mac OS X", "FreeBSD", "Plan 9").map {
            HostPlatform.normalize(it, "unknown").lwjglNatives
        }
        assertEquals(List(5) { null }, unknownArchitectures)

        val freeBsdArm: String? = HostPlatform.normalize("FreeBSD", "arm64").lwjglNatives
        assertNull(freeBsdArm)
    }

    @Test
    fun shouldNormalizeTheHostIndependentlyOfTheDefaultLocale() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            val host = HostPlatform.normalize("WINDOWS", "AMD64")
            val windows: Boolean = host.isWindows
            assertTrue(windows)

            val uvTarget: String = host.uvTarget
            assertEquals("x86_64-pc-windows-msvc", uvTarget)

            val windowsNatives: String? = host.lwjglNatives
            assertEquals("natives-windows", windowsNatives)

            val linuxNatives: String? = HostPlatform.normalize("LINUX", "X86_64").lwjglNatives
            assertEquals("natives-linux", linuxNatives)
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun shouldPlaceTheJavaAndVirtualEnvironmentExecutablesOfEachSystem(): Unit {
        val linux: HostPlatform = HostPlatform.normalize("Linux", "amd64")
        val windows: HostPlatform = HostPlatform.normalize("Windows 11", "amd64")

        val linuxJava: String = linux.javaExecutable(JAVA_HOME)
        assertEquals("/opt/jdk/bin/java", linuxJava)

        val windowsJava: String = windows.javaExecutable(JAVA_HOME)
        assertEquals("/opt/jdk/bin/java.exe", windowsJava)

        val linuxPython: String = linux.virtualEnvironmentExecutable(PYTHON)
        assertEquals("bin/python", linuxPython)

        val windowsPython: String = windows.virtualEnvironmentExecutable(PYTHON)
        assertEquals("Scripts/python.exe", windowsPython)
    }

    @Test
    fun shouldPlaceTheExecutablesOfThisHost(): Unit {
        val host: HostPlatform = HostPlatform.current()

        val java: String = javaExecutable(JAVA_HOME)
        val expectedJava: String = host.javaExecutable(JAVA_HOME)
        assertEquals(expectedJava, java)

        val python: String = virtualEnvironmentExecutable(PYTHON)
        val expectedPython: String = host.virtualEnvironmentExecutable(PYTHON)
        assertEquals(expectedPython, python)
    }

    @Test
    fun shouldAddTheLwjglNativesOfAHostThatHasThem(): Unit {
        val project: Project = this.javaProject()
        val linux: HostPlatform = HostPlatform.normalize("Linux", "amd64")
        project.dependencies.addLwjglTestNatives(linux, listOf(this.lwjgl(project)))

        val classifiers: List<String?> = this.testRuntimeClassifiers(project)
        assertEquals(listOf("natives-linux"), classifiers)
    }

    @Test
    fun shouldAddNoLwjglNativesForAHostWithoutThem(): Unit {
        val project: Project = this.javaProject()
        val unknown: HostPlatform = HostPlatform.normalize("Plan 9", "amd64")
        project.dependencies.addLwjglTestNatives(unknown, listOf(this.lwjgl(project)))

        val classifiers: List<String?> = this.testRuntimeClassifiers(project)
        assertEquals(emptyList<String?>(), classifiers)
    }

    @Test
    fun shouldAddTheLwjglNativesOfThisHost(): Unit {
        val project: Project = this.javaProject()
        project.dependencies.addLwjglTestNatives(listOf(this.lwjgl(project)))

        val classifiers: List<String?> = this.testRuntimeClassifiers(project)
        val expected: List<String?> = listOfNotNull(HostPlatform.current().lwjglNatives)
        assertEquals(expected, classifiers)
    }

    private fun javaProject(): Project {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply("java")
        return project
    }

    private fun lwjgl(project: Project): Provider<MinimalExternalModuleDependency> {
        return project.provider<MinimalExternalModuleDependency> {
            DefaultMinimalDependency(DefaultModuleIdentifier.newId(LWJGL_GROUP, LWJGL_NAME), DefaultMutableVersionConstraint(LWJGL_VERSION))
        }
    }

    private fun testRuntimeClassifiers(project: Project): List<String?> {
        val dependencies: Iterable<ExternalModuleDependency> =
            project.configurations.getByName("testRuntimeOnly").dependencies.filterIsInstance<ExternalModuleDependency>()
        return dependencies.map { it.artifacts.single().classifier }
    }
}
