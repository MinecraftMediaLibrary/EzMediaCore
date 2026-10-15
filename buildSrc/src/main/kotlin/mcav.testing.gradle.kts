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

import me.brandonli.mcav.gradle.ExclusiveResource
import me.brandonli.mcav.gradle.FuzzTargets
import me.brandonli.mcav.gradle.bundleOf
import me.brandonli.mcav.gradle.javaExecutable
import me.brandonli.mcav.gradle.libraryOf
import me.brandonli.mcav.gradle.libs

plugins {
    java
}

dependencies {
    testImplementation(platform(libs.libraryOf("junit-bom")))
    testImplementation(libs.bundleOf("testing"))
    testRuntimeOnly(libs.libraryOf("junit-platform-launcher"))
}

val networkTests = providers.gradleProperty("mcav.networkTests").getOrElse("false")
val syncMeasurement = providers.gradleProperty("mcav.syncMeasurement").getOrElse("false")
val measuresSync = syncMeasurement.toBoolean()
val testJavaHome = providers.gradleProperty("mcav.testJavaHome")
val propertyTries = providers.gradleProperty("property.tries").getOrElse("200")
val propertySmokeTries = providers.gradleProperty("property.smokeTries").getOrElse("10")
val fuzzSeconds = providers.gradleProperty("fuzz.seconds").map { it.toInt() }.getOrElse(0)
val testSourceSet = sourceSets.test.get()
val quietMachine = gradle.sharedServices.registerIfAbsent("quietMachine", ExclusiveResource::class) {
    maxParallelUsages = 1
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    failOnNoDiscoveredTests = false
    maxHeapSize = "2g"
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "-XX:+EnableDynamicAgentLoading",
        "-Xshare:off",
        "--sun-misc-unsafe-memory-access=allow"
    )
    systemProperty("mcav.networkTests", networkTests)
    systemProperty("mcav.syncMeasurement", syncMeasurement)
    if (testJavaHome.isPresent) {
        executable = javaExecutable(testJavaHome.get())
    }
    systemProperty("jqwik.database", "build/jqwik-database-$name")
    systemProperty("jqwik.failures.after.default", "PREVIOUS_SEED")
    if (measuresSync) {
        usesService(quietMachine)
    }
}

tasks.test {
    useJUnitPlatform {
        excludeTags("fuzz")
    }
    systemProperty("jqwik.tries.default", propertySmokeTries)
    maxParallelForks = if (measuresSync) 1 else (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    systemProperty("jqwik.database", "")
}

val propertyTest = tasks.register<Test>("propertyTest") {
    group = "verification"
    description = "Runs the jqwik property tests of this module"
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    useJUnitPlatform {
        includeEngines("jqwik")
    }
    systemProperty("jqwik.tries.default", propertyTries)
}

val fuzzInputs = layout.projectDirectory.dir("src/test/resources")
val fuzzing = fuzzSeconds > 0
val fuzzTargets = if (fuzzing) FuzzTargets.find(testSourceSet.java.srcDirs) else emptyList()
val fuzzTaskNames = fuzzTargets.map { it.taskName() }.toSet() + "fuzzTest"

tasks.withType<Test>().matching { it.name in fuzzTaskNames }.configureEach {
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    useJUnitPlatform {
        includeTags("fuzz")
    }
    include("**/*FuzzTest.class")
    inputs.files(fuzzInputs)
        .withPropertyName("fuzzInputs")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(layout.buildDirectory.dir("jazzer/$name")).withPropertyName("jazzerDirectory")
    systemProperty("jazzer.internal.basedir", "build/jazzer/$name")
    systemProperty("junit.jupiter.execution.timeout.testtemplate.method.default", "30s")
    maxHeapSize = "1g"
}

val fuzzTasks = fuzzTargets.map { target ->
    tasks.register<Test>(target.taskName()) {
        group = "verification"
        description = "Fuzzes ${target.filterPattern()} for $fuzzSeconds seconds in a JVM of its own"
        filter.includeTestsMatching(target.filterPattern())
        environment("JAZZER_FUZZ", "1")
        systemProperty("jazzer.max_duration", "${fuzzSeconds}s")
        systemProperty("junit.jupiter.execution.timeout.mode", "disabled")
        outputs.upToDateWhen { false }
        testLogging {
            events("started", "failed", "skipped")
        }
    }
}

val fuzzTest = tasks.register<Test>("fuzzTest") {
    group = "verification"
    description = "Replays the committed fuzz inputs of this module, and fuzzes every fuzz test method with -Pfuzz.seconds=<n>"
    dependsOn(fuzzTasks)
}

tasks.check {
    dependsOn(propertyTest, fuzzTest)
}
