plugins {
    `kotlin-dsl`
    jacoco
    alias(libs.plugins.pitest)
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.xz)
    implementation(plugin(libs.plugins.spotless))
    implementation(plugin(libs.plugins.paperweight.userdev))
    implementation(plugin(libs.plugins.checker.framework))
    implementation(plugin(libs.plugins.node))
    implementation(plugin(libs.plugins.errorprone))
    implementation(plugin(libs.plugins.pitest))
    implementation(plugin(libs.plugins.shadow))
    implementation(plugin(libs.plugins.run.paper))
    implementation(plugin(libs.plugins.resource.factory.paper))
    implementation(plugin(libs.plugins.gremlin))
    implementation(plugin(libs.plugins.javacpp.platform))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.jazzer.junit)
    testImplementation(gradleTestKit())
    testRuntimeOnly(libs.junit.platform.launcher)
}

val catalogVersionsPath: String = "generated/sources/catalog/kotlin"
val catalogVersionsFile: String = "me/brandonli/mcav/gradle/CatalogVersions.kt"
val repositoryHeaderPath: String = "../HEADER"
val lineSeparator: String = "\n"

val buildPackageClasses: String = "me/brandonli/mcav/gradle/**"
val generatedClasses: String = "me/brandonli/mcav/gradle/CatalogVersions*"
val mutatedClasses: String = "me.brandonli.mcav.gradle.*"
val generatedClass: String = "me.brandonli.mcav.gradle.CatalogVersions"
val mutationThreads: Int = 4
val fullCoverage: String = "1.0"

val fuzzTag: String = "fuzz"
val fuzzSecondsProperty: String = "fuzz.seconds"
val fuzzInputsPath: String = "src/test/resources"
val jazzerBaseDirectoryProperty: String = "jazzer.internal.basedir"
val jazzerBaseDirectoryPath: String = "build/jazzer"
val jazzerFuzzVariable: String = "JAZZER_FUZZ"
val enabledValue: String = "1"
val jazzerDurationProperty: String = "jazzer.max_duration"

val catalogVersions: Provider<Directory> = layout.buildDirectory.dir(catalogVersionsPath)
val jacocoVersion: Provider<String> = libs.versions.jacoco
val repositoryHeader: RegularFile = layout.projectDirectory.file(repositoryHeaderPath)
val generateCatalogVersions: TaskProvider<Task> = tasks.register("generateCatalogVersions") {
    group = "build"
    description = "Writes the catalog versions the plugins of this build need outside the main build"
    inputs.property("jacoco", jacocoVersion)
    inputs.file(repositoryHeader)
    outputs.dir(catalogVersions)
    doLast {
        val file: File = catalogVersions.get().file(catalogVersionsFile).asFile
        file.parentFile.mkdirs()
        file.writeText(
            repositoryHeader.asFile.readText() + lineSeparator + """
            |package me.brandonli.mcav.gradle
            |
            |object CatalogVersions {
            |    const val JACOCO: String = "${jacocoVersion.get()}"
            |}
            |""".trimMargin()
        )
    }
}

kotlin.sourceSets.main {
    kotlin.srcDir(generateCatalogVersions)
}

val testSourceSet: SourceSet = sourceSets.test.get()
val fuzzSeconds: Int = providers.gradleProperty(fuzzSecondsProperty).map { it.toInt() }.getOrElse(0)
val fuzzInputs: Directory = layout.projectDirectory.dir(fuzzInputsPath)
val buildClasses: FileTree = sourceSets.main.get().output.classesDirs.asFileTree.matching {
    include(buildPackageClasses)
    exclude(generatedClasses)
}

tasks.test {
    useJUnitPlatform {
        excludeTags(fuzzTag)
    }
}

// one fuzz test method per class: forkEvery gives each class, and so each method, the JVM of its own that Jazzer needs
val fuzzTest: TaskProvider<Test> = tasks.register<Test>("fuzzTest") {
    group = "verification"
    description = "Replays the committed fuzz inputs of buildSrc, and fuzzes every fuzz test class with -Pfuzz.seconds=<n>"
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    useJUnitPlatform {
        includeTags(fuzzTag)
    }
    forkEvery = 1
    failOnNoDiscoveredTests = false
    inputs.files(fuzzInputs)
        .withPropertyName("fuzzInputs")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    systemProperty(jazzerBaseDirectoryProperty, jazzerBaseDirectoryPath)
    if (fuzzSeconds > 0) {
        environment(jazzerFuzzVariable, enabledValue)
        systemProperty(jazzerDurationProperty, "${fuzzSeconds}s")
        outputs.upToDateWhen { false }
    }
}

tasks.jacocoTestReport {
    classDirectories.setFrom(buildClasses)
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    classDirectories.setFrom(buildClasses)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = fullCoverage.toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                minimum = fullCoverage.toBigDecimal()
            }
        }
    }
}

pitest {
    pitestVersion = libs.versions.pitest.asProvider()
    junit5PluginVersion = libs.versions.pitest.junit5.plugin
    targetClasses = setOf(mutatedClasses)
    excludedClasses = setOf(generatedClass)
    excludedGroups = setOf(fuzzTag)
    threads = mutationThreads
    outputFormats = setOf("HTML", "XML")
    timestampedReports = false
    jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED")
}

tasks.check {
    dependsOn(fuzzTest)
    dependsOn(tasks.jacocoTestCoverageVerification)
}

fun plugin(plugin: Provider<PluginDependency>): Provider<String> =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
