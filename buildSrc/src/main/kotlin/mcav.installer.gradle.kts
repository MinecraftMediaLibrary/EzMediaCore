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

import com.github.jengelman.gradle.plugins.shadow.transformers.ApacheNoticeResourceTransformer
import me.brandonli.mcav.gradle.MCAVPublishingExtension
import me.brandonli.mcav.gradle.libraryOf
import me.brandonli.mcav.gradle.libs

plugins {
    id("mcav.module")
    id("mcav.publishing")
    id("com.gradleup.shadow")
}

dependencies {
    implementation(libs.libraryOf("maven-resolver-supplier"))
}

extensions.configure<MCAVPublishingExtension> {
    bundledJar = tasks.shadowJar
}

tasks.jar {
    archiveClassifier = "plain"
}

tasks.shadowJar {
    archiveClassifier = ""
    mergeServiceFiles()
    filesMatching(listOf("META-INF/NOTICE", "META-INF/NOTICE.txt", "META-INF/NOTICE.md")) {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    transform(ApacheNoticeResourceTransformer::class.java)
    inputs.property("noticeDuplicates", DuplicatesStrategy.INCLUDE.name)
    listOf("com.ctc", "com.google", "jakarta.inject", "org.apache", "org.codehaus", "org.eclipse", "org.objectweb", "org.slf4j")
        .forEach { prefix -> relocate(prefix, "me.brandonli.mcav.libs.$prefix") }
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

val builtJar = "-Dmcav.installer.jar=" + layout.buildDirectory.file("libs/mcav-installer.jar").get().asFile.absolutePath
tasks.test {
    jvmArgs("--add-opens", "java.base/java.net=ALL-UNNAMED")
    dependsOn(tasks.shadowJar)
    jvmArgs(builtJar)
}
tasks.named("pitest") {
    dependsOn(tasks.shadowJar)
}
