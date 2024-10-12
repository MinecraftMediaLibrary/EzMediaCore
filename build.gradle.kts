group = "io.github.pulsebeat02"
version = "1.0.0"

plugins {
    java
    id("com.diffplug.spotless") version "7.0.0.BETA2"
    id("com.github.node-gradle.node") version "7.1.0"
}

subprojects {

    apply(plugin = "java")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "com.github.node-gradle.node")

    val targetJavaVersion = 21
    java {
        val javaVersion = JavaVersion.toVersion(targetJavaVersion)
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        if (JavaVersion.current() < javaVersion) {
            toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
        }
    }

    sourceSets {
        main {
            java.srcDir("src/main/java")
            resources.srcDir("src/main/resources")
        }
    }

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.maven.apache.org/maven2/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/central/")
        maven("https://libraries.minecraft.net/")
        maven("https://jitpack.io/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://m2.dv8tion.net/releases/")
    }

    tasks {

        withType<JavaCompile>().configureEach {
            options.compilerArgs.add("-parameters")
            options.encoding = "UTF-8"
            options.release.set(targetJavaVersion)
            options.isFork = true
            options.forkOptions.memoryMaximumSize = "4g"
        }

        node {
            download = true
            version = "22.9.0"
            workDir = file("build/nodejs")
        }

        val windows = System.getProperty("os.name").lowercase().contains("windows")
        fun setupNodeEnvironment(): File {
            val npmExec = if (windows) "node.exe" else "bin/node"
            val folder = node.resolvedNodeDir.get()
            val executable = folder.file(npmExec).asFile
            return executable
        }

        whenTaskAdded {
            if (name == "spotlessJava") {
                dependsOn("nodeSetup", "npmSetup")
            }
        }

        spotless {
            java {
                importOrder()
                removeUnusedImports()
                prettier(mapOf("prettier" to "3.3.3", "prettier-plugin-java" to "2.6.4"))
                    .config(mapOf("parser" to "java",
                        "tabWidth" to 2,
                        "plugins" to listOf("prettier-plugin-java"),
                        "printWidth" to 140))
                    .nodeExecutable(provider { setupNodeEnvironment() })
            }
        }
    }
}

