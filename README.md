[![Build Status](https://img.shields.io/circleci/build/github/MinecraftMediaLibrary/EzMediaCore?style=for-the-badge)](https://app.circleci.com/pipelines/github/MinecraftMediaLibrary/EzMediaCore) [![Code Coverage](https://img.shields.io/codefactor/grade/github/MinecraftMediaLibrary/EzMediaCore?style=for-the-badge)](https://www.codefactor.io/repository/github/minecraftmedialibrary/ezmediacore) [![Dependency Status](https://img.shields.io/librariesio/github/MinecraftMediaLibrary/EzMediaCore?style=for-the-badge)](https://libraries.io/github/MinecraftMediaLibrary/EzMediaCore) [![Documentation Status](https://img.shields.io/readthedocs/minecraftmedialibrary-wiki/latest?style=for-the-badge)](https://minecraftmedialibrary-wiki.readthedocs.io/en/latest/) [![Discord](https://img.shields.io/discord/817501569108017223?style=for-the-badge)](https://discord.gg/qVhhbCWQQV) [![Language](https://img.shields.io/badge/Made%20with-Java-1f425f.svg?style=for-the-badge)](https://www.java.com/en/)

**EzMediaCore** is a library written in Java using the Bukkit API and NMS classes to provide a
helpful tool for other media plugins to take advantage of. One of the most fascinating features
include the ability to render **videos** on a Spigot server. It uses optimized dithering algorithms,
hardware acceleration, and native code to speed up the process. The library takes advantage of
**VLC Media Player**, a speedy, universal program that plays all sorts of media for users.

https://user-images.githubusercontent.com/40838203/132433665-a675fc35-e31f-4044-a960-ce46a8fb7df5.mp4

Frame rates may reach up to **35**, at times with extremely great quality. As a comparison, a
*smooth* animation is approximately **25** frames. The library is capable of playing media in all
sorts of ways, such as maps, entities, chat, scoreboards, debug markers.

---

### Prerequisites

- Java 16
- Minecraft versions **1.16.5** and **1.17** supported

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

Take a look
at [this module](https://github.com/MinecraftMediaLibrary/EzMediaCore/tree/master/deluxemediaplugin)
for examples on how to use the library. It is the plugin I used that displayed the video.

---

### Building Locally

1) Run [BuildTools](https://www.spigotmc.org/wiki/buildtools/) for versions 1.16.5 and 1.17.1.
2) Clone the [repository](https://github.com/MinecraftMediaLibrary/EzMediaCore) by using the
   following Git link: `https://github.com/MinecraftMediaLibrary/EzMediaCore.git`
3) Run `gradlew shadowJar` on the parent project to build a jar for the plugin.

---

### Public Testing Server

Thanks to [Mishal321](https://github.com/mishal321), they freely gave us a public test server.
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
- [Mishal321](https://github.com/mishal321), for allowing us to use his testing server.
- [Yugi](https://github.com/Vshnv) for helping me setup
  [Slimjar](https://github.com/SlimJar/slimjar).
- And also developers of the most important libraries I use, including:
    - [VLCJ](https://github.com/caprica/vlcj) made by [caprica](https://github.com/caprica).
    - [VLC Media Player](https://www.videolan.org/vlc/) for native backend support.
    - [Jaffree](https://github.com/kokorin/Jaffree) made by [kokorin](https://github.com/kokorin)
      for allowing the library to use the NUT container provided by FFmpeg.
    - [FFmpeg](https://www.ffmpeg.org/) for native encoding related operations and backend support.

---
