[![Build Status](https://img.shields.io/circleci/build/github/MinecraftMediaLibrary/EzMediaCore?style=for-the-badge)](https://app.circleci.com/pipelines/github/MinecraftMediaLibrary/EzMediaCore)
[![Documentation Status](https://img.shields.io/readthedocs/minecraftmedialibrary-wiki/latest?style=for-the-badge)](https://minecraftmedialibrary-wiki.readthedocs.io/en/latest/)
[![Discord](https://img.shields.io/discord/817501569108017223?style=for-the-badge)](https://discord.gg/qVhhbCWQQV)
[![Language](https://img.shields.io/badge/Made%20with-Java-1f425f.svg?style=for-the-badge)](https://www.java.com/en/)

![Swag](http://ForTheBadge.com/images/badges/built-with-swag.svg)

---

## Update

I'm currently still developing this library and plugin! I am also looking for any possible
developers who want to join me and help me test and fix bugs in the library. Join my Discord for
status updates about the library and plugin. I am trying my quickest to get the code fixed up and
ready for the public.

---

## Introduction

**EzMediaCore** is a library written from the Bukkit API and NMS classes to provide a helpful
framework for other plugins to take advantage of. One of the most important features perhaps is its
ability to play **videos** on a Minecraft Spigot server. It uses a very optimized dithering method
alongside **VLC Media Player**
for extra hardware acceleration to play the video at higher frame rates. As a result, rates can
reach up to **35** frames per second at times with extremely great quality on maps. As a comparison,
a *smooth* animation is approximately **25** frames. Here is a demo of what video playback looks
like:

[![Video Player](http://img.youtube.com/vi/9oIns_Kp_sk/0.jpg)](https://www.youtube.com/watch?v=9oIns_Kp_sk&t=30s)

The plugin takes advantages of maps, entities, scoreboard, and other possible methods to handle
video playback.

## First Steps

### Prerequisites

- **Java 16** is required
- Only Minecraft versions **1.16.5** and **1.17** supported

Add the following to your build.gradle configuration:

```groovy
repositories {
    maven {
        url 'https://jitpack.io'
    }
}
```

```groovy
dependencies {
    implementation 'com.github.MinecraftMediaLibrary:EzMediaCore:master-SNAPSHOT'
}
```

---

For detailed documentation with explanations, visit
the [readthedocs page](https://minecraftmedialibrary-wiki.readthedocs.io/en/latest/) (which is still
in progress). It is currently outdated, and requires some rewriting.

However, if you want examples, you can take a look
at [this module](https://github.com/MinecraftMediaLibrary/EzMediaCore/tree/master/deluxemediaplugin)
where I wrote comments for every piece of code in the plugin which used the library. It is the
plugin I used that displayed the video.

---

## We Have a Public Server!

Thanks to [Mishal321](https://github.com/mishal321), they freely gave us a public test server.
[Join our Discord server](https://discord.gg/qVhhbCWQQV) and you are able to access the IP to the
testing server!

---

## Contributors/Acknowledgements

- [BananaPuncher714](https://github.com/BananaPuncher714) and [Jetp250](https://github.com/jetp250)
  for helping lead the spark for the project, as well as code the Floyd-Steinberg implementation in
  Java.
- [Emilyy](https://github.com/emilyy-dev) for helping me with asynchronous tasks and implementation.
  Also the plugin too.
- [Conclure](https://github.com/Conclure) for helping me migrate from maven to gradle.
- [Yugi](https://github.com/Vshnv) for helping me
  setup [Slimjar](https://github.com/SlimJar/slimjar).
- [Matt](https://github.com/ipsk) for
  his [GUI Framework](https://github.com/TriumphTeam/gui/tree/development).
- And also developers of the libraries I use. Without these libraries, the development would've been
  a much more pain-staking process.

---
