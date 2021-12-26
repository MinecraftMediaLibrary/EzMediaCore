[![Build Status](https://img.shields.io/circleci/build/github/MinecraftMediaLibrary/EzMediaCore?style=for-the-badge)](https://app.circleci.com/pipelines/github/MinecraftMediaLibrary/EzMediaCore) [![Code Coverage](https://img.shields.io/codefactor/grade/github/MinecraftMediaLibrary/EzMediaCore?style=for-the-badge)](https://www.codefactor.io/repository/github/minecraftmedialibrary/ezmediacore) [![Dependency Status](https://img.shields.io/librariesio/github/MinecraftMediaLibrary/EzMediaCore?style=for-the-badge)](https://libraries.io/github/MinecraftMediaLibrary/EzMediaCore) [![Documentation Status](https://img.shields.io/readthedocs/minecraftmedialibrary-wiki/latest?style=for-the-badge)](https://minecraftmedialibrary-wiki.readthedocs.io/en/latest/) [![Discord](https://img.shields.io/discord/817501569108017223?style=for-the-badge)](https://discord.gg/qVhhbCWQQV) [![Language](https://img.shields.io/badge/Made%20with-Java-1f425f.svg?style=for-the-badge)](https://www.java.com/en/)

<h1 align="center"><img height="35" src="https://emoji.gg/assets/emoji/7333-parrotdance.gif"> EzMediaCore</h1>

---

**EzMediaCore** is a library written in Java using the Bukkit API and NMS classes to provide a
helpful tool for other media plugins to take advantage of. One of the most fascinating features
include the ability to render **videos** on a Spigot server. 

In order to maintain fast processing speeds, algorithms such as dithering, hardware acceleration,
and use of native code is utilized. The library primarily uses **VLC Media Player** and **FFmpeg**,
for blazing fast frame delivery and support for many sorts of media formats (including streams).

https://user-images.githubusercontent.com/40838203/132433665-a675fc35-e31f-4044-a960-ce46a8fb7df5.mp4

Frame rates can reach up to the **30**'s or **40**'s with extremely great quality. As a comparison, a
*smooth* animation is approximately **25** frames. The library is capable of playing media in many
sorts of ways, such as maps, entities, chat, scoreboards, debug markers. It is also capable of
providing audio through a resourcepack and website (primarily for playing streams). Discord support
is current being implemented, but will come soon!

---

### Prerequisites

- Java 17
- Minecraft versions **1.17.1** and **1.18.1** supported.

Add the following to your gradle configuration:

```groovy  
repositories {  
    maven {  
        url 'https://pulsebeat02.jfrog.io/artifactory/pulse-gradle-release-local'  
    }  
}  
```  

```groovy  
dependencies {  
    implementation 'io.github.pulsebeat02:EzMediaCore:v1.0.0'  
}  
```

Take a look
at [this module](https://github.com/MinecraftMediaLibrary/EzMediaCore/tree/master/deluxemediaplugin)
for examples on how to use the library. It is the plugin I used that displayed the video.

---

### Building Locally

1) Run [BuildTools](https://www.spigotmc.org/wiki/buildtools/) for versions 1.17.1, and 1.18.1.
2) Clone the [repository](https://github.com/MinecraftMediaLibrary/EzMediaCore) by using the
   following Git link: `https://github.com/MinecraftMediaLibrary/EzMediaCore.git`
3) Run `gradlew shadowJar` on the parent project to build a jar for the plugin.

---

### Public Testing Server

Thanks to [Fallhost](https://fallhost.com/), they freely gave us a public test server.
[Join our Discord server](https://discord.gg/qVhhbCWQQV) and you are able to access the IP to the
testing server!

---

### Contributors/Acknowledgements

- [BananaPuncher714](https://github.com/BananaPuncher714) for creating MinecraftVideo, leading the
  spark, and also providing a Floyd Steinberg dithering implementation.
- [Jetp250](https://github.com/jetp250) for a fast dithering table class.
- [Emilyy](https://github.com/emilyy-dev) for helping me with asynchronous tasks and implementation.
- [Conclure](https://github.com/Conclure) for helping me migrate from maven to gradle and overall
  big API improvements.
- [itxfrosty](https://github.com/itxfrosty) for helping me develop a Discord bot to play music.
- [Rouge_Ram](https://rogueram.xyz/index.html) for developing a Discord bot to use in the Pulse
  Development Server.
- [Fallhost](https://fallhost.com/), for allowing us to use their testing environment.
- And also developers of the most important libraries I use, including:
    - [VLCJ](https://github.com/caprica/vlcj) made by [caprica](https://github.com/caprica).
    - [VLC Media Player](https://www.videolan.org/vlc/) for native backend support.
    - [Jaffree](https://github.com/kokorin/Jaffree) made by [kokorin](https://github.com/kokorin)
      for allowing the library to use the NUT container provided by FFmpeg.
    - [FFmpeg](https://www.ffmpeg.org/) for native encoding related operations and backend support.
---
