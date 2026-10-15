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

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.internal.artifacts.dependencies.DefaultMutableVersionConstraint
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JavaToolchainsTest {

    private companion object {

        const val VERSION_ALIAS: String = "java"

        const val VENDOR_PROPERTY: String = "java.vendor"
    }

    private lateinit var project: Project
    private lateinit var runningVersion: JavaLanguageVersion

    @BeforeEach
    fun setUp(): Unit {
        val feature: Int = Runtime.version().feature()
        val version: DefaultMutableVersionConstraint = DefaultMutableVersionConstraint(feature.toString())
        val catalog: FakeVersionCatalog = FakeVersionCatalog(mapOf(VERSION_ALIAS to version), emptyMap(), emptyMap())
        val extension: VersionCatalogsExtension = catalog.asExtension()
        this.project = ProjectBuilder.builder().build()
        this.project.pluginManager.apply("java-base")
        this.project.extensions.add(VersionCatalogsExtension::class.java, "versionCatalogs", extension)
        this.runningVersion = JavaLanguageVersion.of(feature)
    }

    @Test
    fun shouldReadTheJavaVersionFromTheCatalog(): Unit {
        val version: JavaLanguageVersion = JavaToolchains.languageVersion(this.project)
        assertEquals(this.runningVersion, version)
    }

    @Test
    fun shouldFindALauncherOfTheCatalogVersion(): Unit {
        val launcher: JavaLauncher = JavaToolchains.launcher(this.project).get()

        val version: JavaLanguageVersion = launcher.metadata.languageVersion
        assertEquals(this.runningVersion, version)
    }

    @Test
    fun shouldFindALauncherOfTheRequestedVendor(): Unit {
        val runningVendor: String = System.getProperty(VENDOR_PROPERTY)
        val vendor: JvmVendorSpec = JvmVendorSpec.matching(runningVendor)
        val launcher: JavaLauncher = JavaToolchains.launcher(this.project, vendor).get()

        val version: JavaLanguageVersion = launcher.metadata.languageVersion
        assertEquals(this.runningVersion, version)
    }
}
