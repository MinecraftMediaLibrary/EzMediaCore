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

val fuzzTest = tasks.register<Test>("fuzzTest") {
    group = "verification"
    description = "Replays the committed fuzz inputs of this module, and fuzzes every fuzz test with -Pfuzz.seconds=<n>"
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    useJUnitPlatform {
        includeTags("fuzz")
    }
    include("**/*FuzzTest.class")
    inputs.files(layout.projectDirectory.dir("src/test/resources"))
        .withPropertyName("fuzzInputs")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.property("fuzzSeconds", fuzzSeconds)
    outputs.dir(layout.buildDirectory.dir("jazzer")).withPropertyName("jazzerDirectory")
    val replaysOnly = fuzzSeconds == 0
    outputs.cacheIf("only the replay of the committed inputs is deterministic") { replaysOnly }
    systemProperty("jazzer.internal.basedir", "build/jazzer")
    systemProperty("junit.jupiter.execution.timeout.testtemplate.method.default", "30s")
    maxHeapSize = "1g"
    if (fuzzSeconds > 0) {
        environment("JAZZER_FUZZ", "1")
        systemProperty("jazzer.max_duration", "${fuzzSeconds}s")
        systemProperty("junit.jupiter.execution.timeout.mode", "disabled")
        forkEvery = 1
        outputs.upToDateWhen { false }
        testLogging {
            events("started", "failed", "skipped")
        }
    }
}

tasks.check {
    dependsOn(propertyTest, fuzzTest)
}
