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
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

private const val CATALOG_NAME: String = "libs"

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named(CATALOG_NAME)

object CatalogUtils {

    fun versionOf(catalog: VersionCatalog, alias: String): String {
        val version: Optional<VersionConstraint> = catalog.findVersion(alias)
        val constraint: VersionConstraint? = version.orElse(null)
        if (constraint == null) {
            val message: String = "Version $alias is missing from the catalog!"
            throw IllegalArgumentException(message)
        }

        return constraint.requiredVersion
    }

    fun libraryOf(catalog: VersionCatalog, alias: String): Provider<MinimalExternalModuleDependency> {
        val library: Optional<Provider<MinimalExternalModuleDependency>> = catalog.findLibrary(alias)
        val dependency: Provider<MinimalExternalModuleDependency>? = library.orElse(null)
        if (dependency == null) {
            val message: String = "Library $alias is missing from the catalog!"
            throw IllegalArgumentException(message)
        }

        return dependency
    }

    fun bundleOf(catalog: VersionCatalog, alias: String): Provider<ExternalModuleDependencyBundle> {
        val bundle: Optional<Provider<ExternalModuleDependencyBundle>> = catalog.findBundle(alias)
        val dependencies: Provider<ExternalModuleDependencyBundle>? = bundle.orElse(null)
        if (dependencies == null) {
            val message: String = "Bundle $alias is missing from the catalog!"
            throw IllegalArgumentException(message)
        }

        return dependencies
    }
}
