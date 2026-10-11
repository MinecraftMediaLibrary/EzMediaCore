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

import me.brandonli.mcav.gradle.CheckerStubs
import me.brandonli.mcav.gradle.javaLanguageVersion
import me.brandonli.mcav.gradle.libraryOf
import me.brandonli.mcav.gradle.libs
import me.brandonli.mcav.gradle.versionOf
import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    id("org.checkerframework")
    id("net.ltgt.errorprone")
}

val javaRelease = libs.versionOf("java").toInt()

val enforcedChecks = listOf(
    "FieldCanBeFinal",
    "FieldCanBeStatic",
    "FieldCanBeLocal",
    "MethodCanBeStatic",
    "ClassCanBeStatic",
    "PrivateConstructorForUtilityClass",
    "ConstantField",
    "ConstantPatternCompile",
    "UnusedVariable",
    "UnusedMethod",
    "UnusedNestedClass",
    "UnusedLabel",
    "RedundantOverride",
    "MultiVariableDeclaration",
    "UngroupedOverloads",
    "WildcardImport",
    "RemoveUnusedImports",
    "MissingOverride",
    "UnnecessaryAnonymousClass",
    "UnnecessaryBoxedVariable",
    "UnnecessaryBoxedAssignment",
    "StaticQualifiedUsingExpression",
    "LongLiteralLowerCaseSuffix",
    "MultipleTopLevelClasses",
    "PackageLocation",
    "DefaultLocale"
)

repositories {
    mavenCentral()
    google()
    maven("https://repo.brandonli.me/snapshots")
    maven("https://maven.maxhenkel.de/repository/public")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://api.modrinth.com/maven") {
        content {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    errorprone(libs.libraryOf("errorprone-core"))
}

java {
    toolchain.languageVersion = javaLanguageVersion()
}

tasks.withType<JavaCompile>().configureEach {
    options.release = javaRelease
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all", "-Xlint:-processing", "-Werror"))
    options.isFork = true
    options.forkOptions.memoryMaximumSize = "4g"
    options.forkOptions.jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf("--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED")
    })
    options.errorprone {
        disableWarningsInGeneratedCode = true
        error(*enforcedChecks.toTypedArray())
    }
}

checkerFramework {
    version = libs.versionOf("checker-framework")
    checkers = listOf("org.checkerframework.checker.nullness.NullnessChecker")
    excludeTests = true
    extraJavacArgs = stubsArgument() + "-AslowTypecheckingSeconds=600"
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    filteringCharset = "UTF-8"
}

fun stubsArgument(): List<String> {
    val stubs = rootProject.file("gradle/checker-framework")
    val folders = CheckerStubs.forProject(stubs, project.name)
    return if (folders.isEmpty()) emptyList() else listOf("-Astubs=" + folders.joinToString(File.pathSeparator))
}
