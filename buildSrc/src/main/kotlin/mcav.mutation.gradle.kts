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

import me.brandonli.mcav.gradle.TestRuntimeProfile
import me.brandonli.mcav.gradle.javaExecutable
import me.brandonli.mcav.gradle.libs
import me.brandonli.mcav.gradle.versionOf

plugins {
    java
    id("info.solidsoft.pitest")
}

pitest {
    pitestVersion = libs.versionOf("pitest")
    junit5PluginVersion = libs.versionOf("pitest-junit5-plugin")
    targetClasses = setOf("me.brandonli.mcav.*")
    threads = 4
    timeoutConstInMillis = 4000
    outputFormats = setOf("HTML", "XML")
    timestampedReports = false
    jvmArgs = provider { TestRuntimeProfile.forMutation(tasks.test.get()) }
    val testJavaHome = providers.gradleProperty("mcav.testJavaHome")
    if (testJavaHome.isPresent) {
        jvmPath = file(javaExecutable(testJavaHome.get()))
    }
}
