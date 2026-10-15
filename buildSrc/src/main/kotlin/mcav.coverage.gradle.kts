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

import me.brandonli.mcav.gradle.CatalogVersions
import me.brandonli.mcav.gradle.CoverageLintTask
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter

plugins {
    java
    jacoco
}

val coverageReportPath: String = "reports/jacoco/test/jacocoTestReport.xml"
val mainSourcePath: String = "src/main/java"
val coverageExceptionsPath: String = "coverage-exceptions.txt"

val coverageProperty: String = "mcav.coverage"
val disabledValue: String = "false"

val propertyTestName: String = "propertyTest"
val fuzzTaskPrefix: String = "fuzz"

jacoco {
    toolVersion = CatalogVersions.JACOCO
}

val jacocoReport = tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required = true
        html.required = true
    }
}

val instrumentedClasses = listOf("me.brandonli.*", "org.cef.browser.McavOffscreenBrowser*")

tasks.withType<Test>().configureEach {
    extensions.configure<JacocoTaskExtension> {
        includes = instrumentedClasses
    }
}

val testTask = tasks.named<Test>("test") {
    finalizedBy(jacocoReport)
}

val coverageLint = tasks.register<CoverageLintTask>("coverageLint") {
    group = "verification"
    description = "Runs the tests and reports every line and branch of production code that no test covers"
    dependsOn(testTask, jacocoReport)
    report.from(layout.buildDirectory.file(coverageReportPath))
    sourceDirectory = layout.projectDirectory.dir(mainSourcePath)
    exceptionsFile.from(layout.projectDirectory.file(coverageExceptionsPath))
    testsFiltered = testTask.map {
        val filter = it.filter as DefaultTestFilter
        filter.commandLineIncludePatterns.isNotEmpty() || filter.includePatterns.isNotEmpty() ||
            filter.excludePatterns.isNotEmpty() || it.includes.isNotEmpty() || it.excludes.isNotEmpty()
    }
    projectPath = project.path
}

tasks.withType<Test>().named { it == propertyTestName || it.startsWith(fuzzTaskPrefix) }.configureEach {
    extensions.configure<JacocoTaskExtension> {
        isEnabled = false
    }
}

val coverageGate = providers.gradleProperty(coverageProperty).map { !it.equals(disabledValue, ignoreCase = true) }.getOrElse(true)
if (coverageGate) {
    tasks.check {
        dependsOn(coverageLint)
    }
}
