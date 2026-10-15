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

val javaAlias: String = "java"
val errorProneAlias: String = "errorprone-core"
val checkerFrameworkAlias: String = "checker-framework"

val sourceEncoding: String = "UTF-8"
val compilerHeapSize: String = "4g"
val nullnessChecker: String = "org.checkerframework.checker.nullness.NullnessChecker"
val slowTypecheckingSeconds: Int = 600
val checkerStubsPath: String = "gradle/checker-framework"

val repositoryUrls: List<String> = listOf(
    "https://repo.brandonli.me/snapshots",
    "https://maven.maxhenkel.de/repository/public",
    "https://repo.papermc.io/repository/maven-public/",
    "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
    "https://repo.codemc.io/repository/maven-releases/"
)
val modrinthRepositoryUrl: String = "https://api.modrinth.com/maven"
val modrinthGroup: String = "maven.modrinth"

val javaRelease: Int = libs.versionOf(javaAlias).toInt()

val enforcedChecks: List<String> = listOf(
    "FieldCanBeFinal",
    "FieldCanBeStatic",
    "FieldCanBeLocal",
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
    repositoryUrls.forEach { maven(it) }
    maven(modrinthRepositoryUrl) {
        content {
            includeGroup(modrinthGroup)
        }
    }
}

dependencies {
    errorprone(libs.libraryOf(errorProneAlias))
}

java {
    toolchain.languageVersion = javaLanguageVersion()
}

tasks.withType<JavaCompile>().configureEach {
    options.release = javaRelease
    options.encoding = sourceEncoding
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all", "-Xlint:-processing", "-Werror"))
    options.isFork = true
    options.forkOptions.memoryMaximumSize = compilerHeapSize
    options.forkOptions.jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf("--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED")
    })
    options.errorprone {
        disableWarningsInGeneratedCode = true
        error(*enforcedChecks.toTypedArray())
        // the style guide keeps stateless private helpers as instance methods called with this.
        disable("MethodCanBeStatic")
    }
}

checkerFramework {
    version = libs.versionOf(checkerFrameworkAlias)
    checkers = listOf(nullnessChecker)
    excludeTests = true
    extraJavacArgs = stubsArgument() + "-AslowTypecheckingSeconds=$slowTypecheckingSeconds"
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    filteringCharset = sourceEncoding
}

fun stubsArgument(): List<String> {
    val stubs: File = rootProject.file(checkerStubsPath)
    val folders: List<File> = CheckerStubs.forProject(stubs, project.name)
    return if (folders.isEmpty()) emptyList() else listOf("-Astubs=" + folders.joinToString(File.pathSeparator))
}
