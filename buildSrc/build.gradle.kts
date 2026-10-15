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

val catalogVersions = layout.buildDirectory.dir("generated/sources/catalog/kotlin")
val jacocoVersion = libs.versions.jacoco
val repositoryHeader = layout.projectDirectory.file("../HEADER")
val generateCatalogVersions = tasks.register("generateCatalogVersions") {
    group = "build"
    description = "Writes the catalog versions the plugins of this build need outside the main build"
    inputs.property("jacoco", jacocoVersion)
    inputs.file(repositoryHeader)
    outputs.dir(catalogVersions)
    doLast {
        val file = catalogVersions.get().file("me/brandonli/mcav/gradle/CatalogVersions.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            repositoryHeader.asFile.readText() + "\n" + """
            |package me.brandonli.mcav.gradle
            |
            |object CatalogVersions {
            |    const val JACOCO = "${jacocoVersion.get()}"
            |}
            |""".trimMargin()
        )
    }
}

kotlin.sourceSets.main {
    kotlin.srcDir(generateCatalogVersions)
}

val testSourceSet = sourceSets.test.get()
val fuzzSeconds = providers.gradleProperty("fuzz.seconds").map { it.toInt() }.getOrElse(0)
val fuzzInputs = layout.projectDirectory.dir("src/test/resources")
val buildClasses = sourceSets.main.get().output.classesDirs.asFileTree.matching {
    include("me/brandonli/mcav/gradle/**")
    exclude("me/brandonli/mcav/gradle/CatalogVersions*")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        excludeTags("fuzz")
    }
}

// one fuzz test method per class: forkEvery gives each class, and so each method, the JVM of its own that Jazzer needs
val fuzzTest = tasks.register<Test>("fuzzTest") {
    group = "verification"
    description = "Replays the committed fuzz inputs of buildSrc, and fuzzes every fuzz test class with -Pfuzz.seconds=<n>"
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    useJUnitPlatform {
        includeTags("fuzz")
    }
    forkEvery = 1
    failOnNoDiscoveredTests = false
    inputs.files(fuzzInputs)
        .withPropertyName("fuzzInputs")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    systemProperty("jazzer.internal.basedir", "build/jazzer")
    if (fuzzSeconds > 0) {
        environment("JAZZER_FUZZ", "1")
        systemProperty("jazzer.max_duration", "${fuzzSeconds}s")
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
                minimum = "1.0".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                minimum = "1.0".toBigDecimal()
            }
        }
    }
}

pitest {
    pitestVersion = libs.versions.pitest.asProvider()
    junit5PluginVersion = libs.versions.pitest.junit5.plugin
    targetClasses = setOf("me.brandonli.mcav.gradle.*")
    excludedClasses = setOf("me.brandonli.mcav.gradle.CatalogVersions")
    excludedGroups = setOf("fuzz")
    threads = 4
    outputFormats = setOf("HTML", "XML")
    timestampedReports = false
}

tasks.check {
    dependsOn(fuzzTest)
}

fun plugin(plugin: Provider<PluginDependency>): Provider<String> =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
