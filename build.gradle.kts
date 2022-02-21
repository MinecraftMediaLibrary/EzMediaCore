group = "io.github.pulsebeat02"
version = "1.0.0"

plugins {
    java
    `java-library`
    id("com.github.hierynomus.license-base") version "0.16.1"
    id("org.ajoberstar.grgit") version "5.0.0"
}

subprojects {

    apply {
        setOf(
            "java",
            "java-library",
            "com.github.hierynomus.license-base"
        ).forEach {
            plugin(it)
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }
    }

    sourceSets {
        main {
            java.srcDir("src/main/java")
            resources.srcDir("src/main/resources")
        }
    }

    license {
        header = rootProject.file("header.txt")
        encoding = "UTF-8"
        mapping("java", "SLASHSTAR_STYLE")
        includes(setOf("**/*.java", "**/*.kts"))
    }

    dependencies {
        setOf("org.jetbrains:annotations:23.0.0").forEach {
            compileOnly(it)
            testImplementation(it)
        }
    }

    repositories {
        mavenCentral()
        mavenLocal()
        setOf(
            "https://repo.maven.apache.org/maven2/",
            "https://papermc.io/repo/repository/maven-public/",
            "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
            "https://oss.sonatype.org/content/repositories/snapshots/",
            "https://oss.sonatype.org/content/repositories/central/",
            "https://libraries.minecraft.net/",
            "https://jitpack.io/",
            "https://repo.codemc.org/repository/maven-public/",
            "https://m2.dv8tion.net/releases/",
            "https://repo.vshnv.tech/releases/",
            "https://repo.mattstudios.me/artifactory/public/",
            "https://pulsebeat02.jfrog.io/artifactory/minecraftmedialibrary/"
        ).forEach {
            maven(it)
        }
    }

    task<Wrapper>("wrapper") {
        gradleVersion = "7.3.3"
    }
}