[![Build Status](https://img.shields.io/circleci/build/github/MinecraftMediaLibrary/EpicMediaLib?style=for-the-badge)](https://app.circleci.com/pipelines/github/MinecraftMediaLibrary/EpicMediaLib)
[![Documentation Status](https://img.shields.io/readthedocs/minecraftmedialibrary-wiki/latest?style=for-the-badge)](https://minecraftmedialibrary-wiki.readthedocs.io/en/latest/)
[![Discord](https://img.shields.io/discord/817501569108017223?style=for-the-badge)](https://discord.gg/qVhhbCWQQV)
[![Language](https://img.shields.io/badge/Made%20with-Java-1f425f.svg?style=for-the-badge)](https://www.java.com/en/)
[![Lines of Code](https://img.shields.io/tokei/lines/github/MinecraftMediaLibrary/EpicMediaLib?style=for-the-badge)](https://github.com/MinecraftMediaLibrary/EpicMediaLib)

[![Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/PulseBeat02/MinecraftMediaLibrary)

![Icon](https://github.com/MinecraftMediaLibrary/EpicMediaLib/blob/dev/logo.gif)

![Swag](http://ForTheBadge.com/images/badges/built-with-swag.svg)

---
## Introduction

**MinecraftMediaLibrary** is a library written along the Spigot API and net.minecraft.server classes to provide helpful
and useful classes for other plugins to take advantage of. One of the most important features perhaps is its ability to
play **videos** on a Minecraft Spigot server. It uses a very optimized dithering method alongside **VLC Media Player**
for extra hardware acceleration to play the video at higher frame rates. As a result, rates can reach up to **35** frames 
per second at times with extremely great quality on maps. As a comparison, a *smooth* animation is approximately **25** frames.
Here is a demo of what video playback looks like:

[![Video Player](http://img.youtube.com/vi/9oIns_Kp_sk/0.jpg)](https://www.youtube.com/watch?v=9oIns_Kp_sk&t=30s)

The plugin takes advantages of maps, entities, scoreboard, and other possible methods to handle video playback.

## First Steps

---

Jitpack Repository:

**Maven**

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

**Gradle (Groovy)**

```groovy
repositories {
    maven {
        url 'https://jitpack.io'
    }
}
```

**Gradle (Kotlin)**

```kotlin
repositories {
    maven {
        url("https://jitpack.io")
    }
}
```

---

MinecraftMediaLibrary Dependency:

**Maven**

```xml

<dependency>
    <groupId>com.github.pulsebeat02</groupId>
    <artifactId>epicmedialib</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

**Gradle (Groovy)**

```groovy
dependencies {
    implementation 'com.github.minecraftmedialibrary:epicmedialib:master-SNAPSHOT'
}
```

**Gradle (Kotlin)**

```kotlin
dependencies {
    implementation("com.github.minecraftmedialibrary:epicmedialib:master-SNAPSHOT")
}
```

---

For detailed documentation with explanations, visit
the [readthedocs page](https://minecraftmedialibrary-wiki.readthedocs.io/en/latest/) (which is still in progress).

However, if you want examples, you can take a look
at [this module](https://github.com/MinecraftMediaLibrary/EpicMediaLib/tree/master/deluxemediaplugin)
where I wrote comments for every piece of code in the plugin which used the library. It is the plugin I used that
displayed the video.

---

## We Have a Public Server!

Thanks to [Mishal321](https://github.com/mishal321), they freely gave us a public test server.
[Join our Discord server](https://discord.gg/qVhhbCWQQV) and you are able to access the IP to the testing server!

---

## Contributors/Acknowledgements

- [BananaPuncher714](https://github.com/BananaPuncher714) and [Jetp250](https://github.com/jetp250)
  for helping lead the spark for the project, as well as code the Floyd-Steinberg implementation in Java.
- [Emilyy](https://github.com/emilyy-dev) for helping me with asynchronous tasks and implementation. Also the plugin
  too.
- [Conclure](https://github.com/Conclure) for helping me migrate from maven to gradle.
- [Yugi](https://github.com/Vshnv) for helping me setup [Slimjar](https://github.com/SlimJar/slimjar) and allowing 
  support for futuristic Java versions.
- [Matt](https://github.com/ipsk) for his [GUI Framework](https://github.com/TriumphTeam/gui/tree/development).
- And also developers of the libraries I use. Without these libraries, the development would've been a much more
  pain-staking process.
- 
---
