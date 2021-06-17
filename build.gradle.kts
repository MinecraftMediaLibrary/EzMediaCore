description = "minecraftmedialibrary-parent"
version = "1.4.0"

plugins {
    java
    `java-library`
}

subprojects {

    apply(plugin = "java-library")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    repositories {
        maven("https://repo.maven.apache.org/maven2/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://libraries.minecraft.net/")
        maven("https://jitpack.io")
        maven("https://repo.codemc.org/repository/maven-public")
        maven("https://libraries.minecraft.net")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.vshnv.tech/")
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}