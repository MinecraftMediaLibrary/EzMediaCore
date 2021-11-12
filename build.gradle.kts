group = "io.github.pulsebeat02"
version = "1.0.0"

plugins {
    java
    `java-library`
    id("com.github.hierynomus.license-base") version "0.16.1"
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
        sourceCompatibility = JavaVersion.VERSION_16
        targetCompatibility = JavaVersion.VERSION_16
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
        compileOnly("org.jetbrains:annotations:22.0.0")
    }

    repositories {
        mavenCentral()
        mavenLocal()
        setOf(
            "https://repo.maven.apache.org/maven2/",
            "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
            "https://libraries.minecraft.net/",
            "https://jitpack.io",
            "https://repo.codemc.org/repository/maven-public",
            "https://libraries.minecraft.net",
            "https://oss.sonatype.org/content/repositories/snapshots/",
            "https://papermc.io/repo/repository/maven-public/",
            "https://m2.dv8tion.net/releases",
            "https://repo.vshnv.tech/releases/",
            "https://repo.mattstudios.me/artifactory/public/"
        ).forEach {
            maven(it)
        }
    }
}
