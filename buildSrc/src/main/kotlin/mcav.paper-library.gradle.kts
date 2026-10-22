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

import me.brandonli.mcav.gradle.CatalogUtils
import me.brandonli.mcav.gradle.MCAVPublishingExtension
import me.brandonli.mcav.gradle.libs

plugins {
    id("mcav.module")
    id("mcav.mcv2-tools")
    id("mcav.natives")
    id("mcav.publishing")
    id("io.papermc.paperweight.userdev")
}

val paperAlias: String = "paper"

dependencies {
    paperweight.paperDevBundle(CatalogUtils.versionOf(libs, paperAlias))
}

extensions.configure<MCAVPublishingExtension> {
    gradleModuleMetadata = false
}
