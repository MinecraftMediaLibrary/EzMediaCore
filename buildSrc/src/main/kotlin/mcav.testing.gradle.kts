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
import me.brandonli.mcav.gradle.ExclusiveResource
import me.brandonli.mcav.gradle.FuzzTarget
import me.brandonli.mcav.gradle.FuzzTargets
import me.brandonli.mcav.gradle.HostPlatform
import me.brandonli.mcav.gradle.libs

plugins {
    java
}

val junitBomAlias: String = "junit-bom"
val testingBundleAlias: String = "testing"
val junitLauncherAlias: String = "junit-platform-launcher"

val networkTestsProperty: String = "mcav.networkTests"
val syncMeasurementProperty: String = "mcav.syncMeasurement"
val testJavaHomeProperty: String = "mcav.testJavaHome"
val propertyTriesProperty: String = "property.tries"
val propertySmokeTriesProperty: String = "property.smokeTries"
val fuzzSecondsProperty: String = "fuzz.seconds"
val disabledValue: String = "false"
val defaultPropertyTries: String = "200"
val defaultPropertySmokeTries: String = "10"

val quietMachineService: String = "quietMachine"
val testHeapSize: String = "2g"
val fuzzHeapSize: String = "1g"
val processorsPerFork: Int = 2

val jqwikEngine: String = "jqwik"
val jqwikDatabaseProperty: String = "jqwik.database"
val jqwikDatabasePath: String = "build/jqwik-database"
val jqwikFailuresProperty: String = "jqwik.failures.after.default"
val previousSeed: String = "PREVIOUS_SEED"
val jqwikTriesProperty: String = "jqwik.tries.default"
val noDatabase: String = ""

val fuzzTag: String = "fuzz"
val fuzzTestName: String = "fuzzTest"
val fuzzInputsPath: String = "src/test/resources"
val fuzzTestClasses: String = "**/*FuzzTest.class"
val jazzerPath: String = "jazzer"
val jazzerBaseDirectoryProperty: String = "jazzer.internal.basedir"
val jazzerBaseDirectoryPath: String = "build/jazzer"
val testTemplateTimeoutProperty: String = "junit.jupiter.execution.timeout.testtemplate.method.default"
val replayTimeout: String = "30s"
val jazzerFuzzVariable: String = "JAZZER_FUZZ"
val enabledValue: String = "1"
val jazzerDurationProperty: String = "jazzer.max_duration"
val timeoutModeProperty: String = "junit.jupiter.execution.timeout.mode"
val disabledTimeoutMode: String = "disabled"

dependencies {
    testImplementation(platform(CatalogUtils.libraryOf(libs, junitBomAlias)))
    testImplementation(CatalogUtils.bundleOf(libs, testingBundleAlias))
    testRuntimeOnly(CatalogUtils.libraryOf(libs, junitLauncherAlias))
}

val networkTests: String = providers.gradleProperty(networkTestsProperty).getOrElse(disabledValue)
val syncMeasurement: String = providers.gradleProperty(syncMeasurementProperty).getOrElse(disabledValue)
val measuresSync: Boolean = syncMeasurement.toBoolean()
val testJavaHome: Provider<String> = providers.gradleProperty(testJavaHomeProperty)
val propertyTries: String = providers.gradleProperty(propertyTriesProperty).getOrElse(defaultPropertyTries)
val propertySmokeTries: String = providers.gradleProperty(propertySmokeTriesProperty).getOrElse(defaultPropertySmokeTries)
val fuzzSeconds: Int = providers.gradleProperty(fuzzSecondsProperty).map { it.toInt() }.getOrElse(0)
val testSourceSet: SourceSet = sourceSets.test.get()
val quietMachine: Provider<ExclusiveResource> = gradle.sharedServices.registerIfAbsent(quietMachineService, ExclusiveResource::class) {
    maxParallelUsages = 1
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    failOnNoDiscoveredTests = false
    maxHeapSize = testHeapSize
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "-XX:+EnableDynamicAgentLoading",
        "-Xshare:off",
        "--sun-misc-unsafe-memory-access=allow"
    )
    systemProperty(networkTestsProperty, networkTests)
    systemProperty(syncMeasurementProperty, syncMeasurement)
    if (testJavaHome.isPresent) {
        executable = HostPlatform.current().javaExecutable(testJavaHome.get())
    }
    systemProperty(jqwikDatabaseProperty, "$jqwikDatabasePath-$name")
    systemProperty(jqwikFailuresProperty, previousSeed)
    if (measuresSync) {
        usesService(quietMachine)
    }
}

tasks.test {
    useJUnitPlatform {
        excludeTags(fuzzTag)
    }
    systemProperty(jqwikTriesProperty, propertySmokeTries)
    maxParallelForks = if (measuresSync) 1 else (Runtime.getRuntime().availableProcessors() / processorsPerFork).coerceAtLeast(1)
    systemProperty(jqwikDatabaseProperty, noDatabase)
}

val propertyTest: TaskProvider<Test> = tasks.register<Test>("propertyTest") {
    group = "verification"
    description = "Runs the jqwik property tests of this module"
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    useJUnitPlatform {
        includeEngines(jqwikEngine)
    }
    systemProperty(jqwikTriesProperty, propertyTries)
}

val fuzzInputs: Directory = layout.projectDirectory.dir(fuzzInputsPath)
val fuzzing: Boolean = fuzzSeconds > 0
val fuzzTargets: List<FuzzTarget> = if (fuzzing) FuzzTargets.find(testSourceSet.java.srcDirs) else emptyList()
val fuzzTaskNames: Set<String> = fuzzTargets.map { it.taskName() }.toSet() + fuzzTestName

tasks.withType<Test>().matching { it.name in fuzzTaskNames }.configureEach {
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    useJUnitPlatform {
        includeTags(fuzzTag)
    }
    include(fuzzTestClasses)
    inputs.files(fuzzInputs)
        .withPropertyName("fuzzInputs")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(layout.buildDirectory.dir("$jazzerPath/$name")).withPropertyName("jazzerDirectory")
    systemProperty(jazzerBaseDirectoryProperty, "$jazzerBaseDirectoryPath/$name")
    systemProperty(testTemplateTimeoutProperty, replayTimeout)
    maxHeapSize = fuzzHeapSize
}

val fuzzTasks: List<TaskProvider<Test>> = fuzzTargets.map { target ->
    tasks.register<Test>(target.taskName()) {
        group = "verification"
        description = "Fuzzes ${target.filterPattern()} for $fuzzSeconds seconds in a JVM of its own"
        filter.includeTestsMatching(target.filterPattern())
        environment(jazzerFuzzVariable, enabledValue)
        systemProperty(jazzerDurationProperty, "${fuzzSeconds}s")
        systemProperty(timeoutModeProperty, disabledTimeoutMode)
        outputs.upToDateWhen { false }
        testLogging {
            events("started", "failed", "skipped")
        }
    }
}

val fuzzTest: TaskProvider<Test> = tasks.register<Test>(fuzzTestName) {
    group = "verification"
    description = "Replays the committed fuzz inputs of this module, and fuzzes every fuzz test method with -Pfuzz.seconds=<n>"
    dependsOn(fuzzTasks)
}

tasks.check {
    dependsOn(propertyTest, fuzzTest)
}
