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

import me.brandonli.mcav.gradle.LogMessagesTask
import me.brandonli.mcav.gradle.QualifiedNamesTask
import me.brandonli.mcav.gradle.VariableNamesTask

plugins {
    base
}

val javaSources = layout.projectDirectory.dir("src").asFileTree.matching { include("*/java/**/*.java") }

val qualifiedNames = tasks.register<QualifiedNamesTask>("qualifiedNames") {
    group = "verification"
    description = "Fails on fully qualified type names in the Java code of every source set of this module"
    sources.from(javaSources)
    report = layout.buildDirectory.file("reports/qualified-names.txt")
}

val logMessages = tasks.register<LogMessagesTask>("logMessages") {
    group = "verification"
    description = "Fails on log calls that join strings with + in the Java code of every source set of this module"
    sources.from(javaSources)
    report = layout.buildDirectory.file("reports/log-messages.txt")
}

val variableNames = tasks.register<VariableNamesTask>("variableNames") {
    group = "verification"
    description = "Fails on one-letter and abbreviated variable names in the Java code of every source set of this module"
    sources.from(javaSources)
    report = layout.buildDirectory.file("reports/variable-names.txt")
}

tasks.check {
    dependsOn(qualifiedNames, logMessages, variableNames)
}
