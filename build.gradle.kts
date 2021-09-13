group = "io.github.pulsebeat02"
version = "1.0.0"

plugins {
    java
    `java-library`
    id("com.github.hierynomus.license-base") version "0.16.1"
}

subprojects {

    apply(plugin = "java-library")
    apply(plugin = "com.github.hierynomus.license-base")

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
        includes(listOf("**/*.java", "**/*.kts"))
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:22.0.0")
    }

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.maven.apache.org/maven2/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://libraries.minecraft.net/")
        maven("https://jitpack.io")
        maven("https://repo.codemc.org/repository/maven-public")
        maven("https://libraries.minecraft.net")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://m2.dv8tion.net/releases")
        maven("https://repo.vshnv.tech/releases/")
        maven("https://repo.mattstudios.me/artifactory/public/")
    }
}