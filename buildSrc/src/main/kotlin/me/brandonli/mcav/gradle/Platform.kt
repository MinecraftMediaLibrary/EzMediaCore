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
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider

class HostPlatform private constructor(val operatingSystem: String, val architecture: String) {

    val isWindows: Boolean get() = operatingSystem.contains(WINDOWS)

    val zigSystem: String get() = when {
        isWindows -> WINDOWS
        operatingSystem.contains(MAC) -> ZIG_MACOS
        operatingSystem.contains(LINUX) -> LINUX
        else -> operatingSystem
    }

    val uvTarget: String get() = architecture + when {
        isWindows -> WINDOWS_UV_TARGET
        operatingSystem.contains(MAC) -> MACOS_UV_TARGET
        else -> LINUX_UV_TARGET
    }

    val lwjglNatives: String? get() = when {
        operatingSystem.contains(MAC) || operatingSystem.contains(DARWIN) -> when (architecture) {
            AARCH64 -> MACOS_ARM64_NATIVES
            X86_64 -> MACOS_NATIVES
            else -> null
        }
        operatingSystem.contains(WIN) -> when (architecture) {
            AARCH64 -> WINDOWS_ARM64_NATIVES
            X86_64 -> WINDOWS_NATIVES
            X86, I386, I686 -> WINDOWS_X86_NATIVES
            else -> null
        }
        operatingSystem.contains(FREEBSD) -> if (architecture == X86_64) FREEBSD_NATIVES else null
        operatingSystem.contains(LINUX) -> when {
            architecture == X86_64 -> LINUX_NATIVES
            architecture == AARCH64 -> LINUX_ARM64_NATIVES
            architecture == ARM || architecture == ARM32 || architecture.startsWith(ARMV7_PREFIX) -> LINUX_ARM32_NATIVES
            architecture == PPC64LE -> LINUX_PPC64LE_NATIVES
            architecture == RISCV64 -> LINUX_RISCV64_NATIVES
            else -> null
        }
        else -> null
    }

    fun javaExecutable(javaHome: String): String = javaHome + JAVA_BINARY + if (isWindows) EXECUTABLE_EXTENSION else ""

    fun virtualEnvironmentExecutable(name: String): String =
        if (isWindows) "$WINDOWS_SCRIPTS_DIRECTORY$name$EXECUTABLE_EXTENSION" else "$UNIX_BINARIES_DIRECTORY$name"

    companion object {

        private const val WINDOWS: String = "windows"
        private const val WIN: String = "win"
        private const val MAC: String = "mac"
        private const val DARWIN: String = "darwin"
        private const val LINUX: String = "linux"
        private const val FREEBSD: String = "freebsd"

        private const val ZIG_MACOS: String = "macos"

        private const val X86_64: String = "x86_64"
        private const val AARCH64: String = "aarch64"
        private const val X86: String = "x86"
        private const val I386: String = "i386"
        private const val I686: String = "i686"
        private const val ARM: String = "arm"
        private const val ARM32: String = "arm32"
        private const val ARMV7_PREFIX: String = "armv7"
        private const val PPC64LE: String = "ppc64le"
        private const val RISCV64: String = "riscv64"
        private const val AMD64: String = "amd64"
        private const val ARM64: String = "arm64"

        private const val WINDOWS_UV_TARGET: String = "-pc-windows-msvc"
        private const val MACOS_UV_TARGET: String = "-apple-darwin"
        private const val LINUX_UV_TARGET: String = "-unknown-linux-musl"

        private const val MACOS_ARM64_NATIVES: String = "natives-macos-arm64"
        private const val MACOS_NATIVES: String = "natives-macos"
        private const val WINDOWS_ARM64_NATIVES: String = "natives-windows-arm64"
        private const val WINDOWS_NATIVES: String = "natives-windows"
        private const val WINDOWS_X86_NATIVES: String = "natives-windows-x86"
        private const val FREEBSD_NATIVES: String = "natives-freebsd"
        private const val LINUX_NATIVES: String = "natives-linux"
        private const val LINUX_ARM64_NATIVES: String = "natives-linux-arm64"
        private const val LINUX_ARM32_NATIVES: String = "natives-linux-arm32"
        private const val LINUX_PPC64LE_NATIVES: String = "natives-linux-ppc64le"
        private const val LINUX_RISCV64_NATIVES: String = "natives-linux-riscv64"

        private const val JAVA_BINARY: String = "/bin/java"
        private const val EXECUTABLE_EXTENSION: String = ".exe"
        private const val WINDOWS_SCRIPTS_DIRECTORY: String = "Scripts/"
        private const val UNIX_BINARIES_DIRECTORY: String = "bin/"

        private const val OPERATING_SYSTEM_PROPERTY: String = "os.name"
        private const val ARCHITECTURE_PROPERTY: String = "os.arch"

        private val ARCHITECTURE_ALIASES: Map<String, String> = mapOf(AMD64 to X86_64, ARM64 to AARCH64)

        fun current(): HostPlatform = normalize(System.getProperty(OPERATING_SYSTEM_PROPERTY), System.getProperty(ARCHITECTURE_PROPERTY))

        fun normalize(operatingSystem: String, architecture: String): HostPlatform {
            val name: String = architecture.lowercase(Locale.ROOT)
            val normalizedArchitecture: String = ARCHITECTURE_ALIASES.getOrDefault(name, name)
            return HostPlatform(operatingSystem.lowercase(Locale.ROOT), normalizedArchitecture)
        }
    }
}

private const val TEST_RUNTIME_CONFIGURATION: String = "testRuntimeOnly"

val isWindows: Boolean = HostPlatform.current().isWindows

fun javaExecutable(javaHome: String): String = HostPlatform.current().javaExecutable(javaHome)

fun virtualEnvironmentExecutable(name: String): String = HostPlatform.current().virtualEnvironmentExecutable(name)

fun DependencyHandler.addLwjglTestNatives(libraries: Iterable<Provider<MinimalExternalModuleDependency>>): Unit {
    addLwjglTestNatives(HostPlatform.current(), libraries)
}

fun DependencyHandler.addLwjglTestNatives(platform: HostPlatform, libraries: Iterable<Provider<MinimalExternalModuleDependency>>): Unit {
    platform.lwjglNatives?.let { natives ->
        libraries.forEach { library ->
            add(TEST_RUNTIME_CONFIGURATION, variantOf(library) { classifier(natives) })
        }
    }
}
