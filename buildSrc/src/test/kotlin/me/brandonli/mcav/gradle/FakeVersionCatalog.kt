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

import java.util.Optional
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency

class FakeVersionCatalog(
    private val versions: Map<String, VersionConstraint>,
    private val libraries: Map<String, Provider<MinimalExternalModuleDependency>>,
    private val bundles: Map<String, Provider<ExternalModuleDependencyBundle>>,
) : VersionCatalog {

    override fun getName(): String {
        return "libs"
    }

    override fun findLibrary(alias: String): Optional<Provider<MinimalExternalModuleDependency>> {
        return Optional.ofNullable(this.libraries[alias])
    }

    override fun findBundle(alias: String): Optional<Provider<ExternalModuleDependencyBundle>> {
        return Optional.ofNullable(this.bundles[alias])
    }

    override fun findVersion(alias: String): Optional<VersionConstraint> {
        return Optional.ofNullable(this.versions[alias])
    }

    override fun findPlugin(alias: String): Optional<Provider<PluginDependency>> {
        return Optional.empty()
    }

    override fun getLibraryAliases(): List<String> {
        return this.libraries.keys.toList()
    }

    override fun getBundleAliases(): List<String> {
        return this.bundles.keys.toList()
    }

    override fun getVersionAliases(): List<String> {
        return this.versions.keys.toList()
    }

    override fun getPluginAliases(): List<String> {
        return emptyList()
    }

    fun asExtension(): VersionCatalogsExtension {
        return Catalogs(this)
    }

    private class Catalogs(private val catalog: VersionCatalog) : VersionCatalogsExtension {

        override fun find(name: String): Optional<VersionCatalog> {
            val found: Optional<VersionCatalog> = Optional.of(this.catalog)
            return found.filter { it.name == name }
        }

        override fun getCatalogNames(): Set<String> {
            return setOf(this.catalog.name)
        }

        override fun iterator(): MutableIterator<VersionCatalog> {
            val catalogs: MutableList<VersionCatalog> = mutableListOf(this.catalog)
            return catalogs.iterator()
        }
    }
}
