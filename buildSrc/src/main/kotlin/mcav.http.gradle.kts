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

import com.github.gradle.node.NodeExtension
import me.brandonli.mcav.gradle.HostPlatform
import me.brandonli.mcav.gradle.NodeLayout

plugins {
    id("mcav.module")
    id("mcav.publishing")
}

val pathVariable: String = "PATH"
val corepackPromptVariable: String = "COREPACK_ENABLE_DOWNLOAD_PROMPT"
val disabledPrompt: String = "0"
val nodeOptionsVariable: String = "NODE_OPTIONS"
val nodeHeapMegabytes: Int = 4096
val pnpmTool: String = "pnpm"

val websitePath: String = "mcav-website"
val packageJsonPath: String = "mcav-website/package.json"
val pnpmLockPath: String = "mcav-website/pnpm-lock.yaml"
val installedModulesPath: String = "mcav-website/node_modules/.modules.yaml"
val websiteSourcePath: String = "mcav-website/src"
val websitePublicPath: String = "mcav-website/public"
val websiteTestPath: String = "mcav-website/test"
val websiteOutputPath: String = "mcav-website/out"
val websiteConfigurationFiles: List<String> = listOf(
    "mcav-website/next.config.ts",
    "mcav-website/third-party-notices.mjs",
    "mcav-website/tsconfig.json",
    "mcav-website/postcss.config.mjs"
)
val websiteTestsPassedPath: String = "website-tests/passed"
val passedMarker: String = "passed\n"

val bundledWebsitePath: String = "mcav/http/website"
val websiteTestResources: String = "website/**"
val httpResourcePath: String = "mcav/http"
val builtJarProperty: String = "mcav.http.jar"
val builtJarPath: String = "libs/mcav-http.jar"

val node = rootProject.extensions.getByType<NodeExtension>()
val nodeSetup = rootProject.tasks.named("nodeSetup")
val host = HostPlatform.current()
val nodeInstallation = node.resolvedNodeDir.get()
val nodeExecutable = NodeLayout.executable(nodeInstallation, host).asFile
val corepackScript = NodeLayout.corepackScript(nodeInstallation, host).asFile
val nodePath = nodeExecutable.parentFile.absolutePath + File.pathSeparator + System.getenv(pathVariable)

val pnpmProjectInstall = tasks.register<Exec>("pnpmProjectInstall") {
    group = "build"
    description = "Installs the pnpm dependencies of the website"
    dependsOn(nodeSetup)
    workingDir = file(websitePath)
    executable = nodeExecutable.absolutePath
    environment(pathVariable, nodePath)
    environment(corepackPromptVariable, disabledPrompt)
    args(corepackScript.absolutePath, pnpmTool, "install", "--frozen-lockfile")
    inputs.file(packageJsonPath)
    inputs.file(pnpmLockPath)
    outputs.file(installedModulesPath)
}

val buildWebsite = tasks.register<Exec>("buildWebsite") {
    group = "build"
    description = "Builds the Next.js website"
    dependsOn(pnpmProjectInstall)
    workingDir = file(websitePath)
    executable = nodeExecutable.absolutePath
    environment(pathVariable, nodePath)
    environment(corepackPromptVariable, disabledPrompt)
    environment(nodeOptionsVariable, "--max-old-space-size=$nodeHeapMegabytes")
    args(corepackScript.absolutePath, pnpmTool, "run", "build")
    inputs.dir(websiteSourcePath)
    inputs.dir(websitePublicPath)
    inputs.file(packageJsonPath)
    inputs.file(pnpmLockPath)
    inputs.files(websiteConfigurationFiles)
    outputs.dir(websiteOutputPath)
    outputs.cacheIf { false }
}

val testWebsite = tasks.register<Exec>("testWebsite") {
    group = "verification"
    description = "Runs the tests of the website"
    dependsOn(pnpmProjectInstall)
    workingDir = file(websitePath)
    executable = nodeExecutable.absolutePath
    environment(pathVariable, nodePath)
    environment(corepackPromptVariable, disabledPrompt)
    args(corepackScript.absolutePath, pnpmTool, "test")
    inputs.dir(websiteSourcePath)
    inputs.dir(websiteTestPath)
    inputs.file(packageJsonPath)
    val passed = layout.buildDirectory.file(websiteTestsPassedPath)
    outputs.file(passed)
    doLast {
        passed.get().asFile.writeText(passedMarker)
    }
}

tasks.check {
    dependsOn(testWebsite)
}

tasks.jar {
    from(buildWebsite) {
        into(bundledWebsitePath)
    }
}

tasks.processTestResources {
    filesMatching(websiteTestResources) {
        path = "$httpResourcePath/$path"
    }
}

tasks.named<Jar>("sourcesJar") {
    from(buildWebsite) {
        into(bundledWebsitePath)
    }
}

val builtJar = "-D$builtJarProperty=" + layout.buildDirectory.file(builtJarPath).get().asFile.absolutePath
tasks.test {
    dependsOn(tasks.jar)
    jvmArgs(builtJar)
}
tasks.named("pitest") {
    dependsOn(tasks.jar)
}
