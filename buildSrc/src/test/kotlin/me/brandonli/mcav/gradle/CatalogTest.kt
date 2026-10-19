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
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.internal.artifacts.dependencies.DefaultMutableVersionConstraint
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CatalogTest {

    private companion object {

        const val VERSION_ALIAS: String = "java"
        const val LIBRARY_ALIAS: String = "guava"
        const val BUNDLE_ALIAS: String = "testing"

        const val JAVA_VERSION: String = "25"

        const val NEVER_RESOLVED_MESSAGE: String = "Never resolved!"
    }

    private lateinit var project: Project
    private lateinit var library: Provider<MinimalExternalModuleDependency>
    private lateinit var bundle: Provider<ExternalModuleDependencyBundle>
    private lateinit var catalog: FakeVersionCatalog

    @BeforeEach
    fun setUp(): Unit {
        this.project = ProjectBuilder.builder().build()
        this.library = this.project.provider<MinimalExternalModuleDependency> { throw IllegalStateException(NEVER_RESOLVED_MESSAGE) }
        this.bundle = this.project.provider<ExternalModuleDependencyBundle> { throw IllegalStateException(NEVER_RESOLVED_MESSAGE) }
        val versions: Map<String, DefaultMutableVersionConstraint> = mapOf(VERSION_ALIAS to DefaultMutableVersionConstraint(JAVA_VERSION))
        this.catalog = FakeVersionCatalog(versions, mapOf(LIBRARY_ALIAS to this.library), mapOf(BUNDLE_ALIAS to this.bundle))
    }

    @Test
    fun shouldReadTheCatalogNamedLibs(): Unit {
        val extension: VersionCatalogsExtension = this.catalog.asExtension()
        this.project.extensions.add(VersionCatalogsExtension::class.java, "versionCatalogs", extension)

        val found: VersionCatalog = this.project.libs
        assertSame(this.catalog, found)
    }

    @Test
    fun shouldReadTheRequiredVersionOfAnAlias(): Unit {
        val version: String = CatalogUtils.versionOf(this.catalog, VERSION_ALIAS)
        assertEquals(JAVA_VERSION, version)
    }

    @Test
    fun shouldReadALibraryByItsAlias(): Unit {
        val found: Provider<MinimalExternalModuleDependency> = CatalogUtils.libraryOf(this.catalog, LIBRARY_ALIAS)
        assertSame(this.library, found)
    }

    @Test
    fun shouldReadABundleByItsAlias(): Unit {
        val found: Provider<ExternalModuleDependencyBundle> = CatalogUtils.bundleOf(this.catalog, BUNDLE_ALIAS)
        assertSame(this.bundle, found)
    }
}
