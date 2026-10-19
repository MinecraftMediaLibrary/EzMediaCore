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

import me.brandonli.mcav.gradle.JavaToolchains

plugins {
    java
}

val benchmarkSourceSetName: String = "benchmark"
val benchmarkMainClass: String = "me.brandonli.mcav.plugin.benchmark.BrowserBenchmark"
val backendProperty: String = "benchmark.backend"
val outputProperty: String = "benchmark.output"
val defaultOutputPath: String = "browser-benchmark.md"
val benchmarkHeapSize: String = "4g"

val benchmark: SourceSet = sourceSets.create(benchmarkSourceSetName)

configurations.named(benchmark.implementationConfigurationName) {
    extendsFrom(configurations.testImplementation.get())
}

configurations.named(benchmark.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    benchmark.implementationConfigurationName(sourceSets.main.get().output)
}

tasks.register<JavaExec>("browserBenchmark") {
    this.group = "verification"
    this.description = "Measures the frame rate and latency of a browser backend: -Pbenchmark.backend=<backend> -Pbenchmark.output=<file>"
    this.classpath = benchmark.runtimeClasspath
    this.mainClass = benchmarkMainClass

    val backend: Provider<String> = providers.gradleProperty(backendProperty).orElse("")
    val output: Provider<String> = providers.gradleProperty(outputProperty)
        .orElse(layout.buildDirectory.file(defaultOutputPath).map { it.asFile.absolutePath })
    this.argumentProviders.add(CommandLineArgumentProvider { listOf(backend.get(), output.get()) })
    this.javaLauncher = JavaToolchains.launcher(project)
    this.maxHeapSize = benchmarkHeapSize
    this.outputs.upToDateWhen { false }
}
