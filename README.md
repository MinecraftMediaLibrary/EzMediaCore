[![Build Status](https://img.shields.io/circleci/build/github/MinecraftMediaLibrary/EzMediaCore?style=for-the-badge)](https://app.circleci.com/pipelines/github/MinecraftMediaLibrary/EzMediaCore) [![Code Coverage](https://img.shields.io/codefactor/grade/github/MinecraftMediaLibrary/EzMediaCore?style=for-the-badge)](https://www.codefactor.io/repository/github/minecraftmedialibrary/ezmediacore) [![Dependency Status](https://img.shields.io/librariesio/github/MinecraftMediaLibrary/EzMediaCore?style=for-the-badge)](https://libraries.io/github/MinecraftMediaLibrary/EzMediaCore) [![Documentation Status](https://img.shields.io/readthedocs/minecraftmedialibrary-wiki/latest?style=for-the-badge)](https://minecraftmedialibrary-wiki.readthedocs.io/en/latest/) [![Discord](https://img.shields.io/discord/817501569108017223?style=for-the-badge)](https://discord.gg/qVhhbCWQQV) [![Language](https://img.shields.io/badge/Made%20with-Java-1f425f.svg?style=for-the-badge)](https://www.java.com/en/)

# Rewrite Progress
The library is currently being rewritten! I am rewriting each package of code slowly and then formulating it all together. Stay tuned for updates!

---

**EzMediaCore** is a library written in Java using the Spigot API and NMS classes to provide a
helpful tool for other media plugins to take advantage of. One of the most fascinating features
include the ability to render **videos** on a Spigot server.

In order to maintain fast processing speeds, algorithms such as dithering, hardware acceleration,
and use of native code is utilized. The library primarily uses **VLC Media Player** and **FFmpeg**,
for blazing fast frame delivery and support for many sorts of media formats (including streams).

https://user-images.githubusercontent.com/40838203/132433665-a675fc35-e31f-4044-a960-ce46a8fb7df5.mp4

Frame rates can reach up to the **30**'s or **40**'s with extremely great quality. As a comparison,
a
*smooth* animation is approximately **25** frames. The library is capable of playing media in many
sorts of ways, such as maps, entities, chat, scoreboards, debug markers. It is also capable of
providing audio through a resourcepack, website, and Discord bot (primarily for playing streams).

---

### Prerequisites

- Java 17
- Minecraft versions **1.18.2** and **1.19** supported.

Add the following to your gradle configuration:

```kotlin  
repositories {  
    maven("https://pulsebeat02.jfrog.io/artifactory/minecraftmedialibrary/")
}  
```  

```groovy  
dependencies {  
    implementation("io.github.pulsebeat02:EzMediaCore:v1.0.0")
}  
```

Take a look
at [this module](https://github.com/MinecraftMediaLibrary/EzMediaCore/tree/master/deluxemediaplugin)
for examples on how to use the library. It is the plugin I used that displayed the video.

---

### Building Locally

1) Run [BuildTools](https://www.spigotmc.org/wiki/buildtools/) for versions 1.18.2 and 1.19.
2) Clone the [repository](https://github.com/MinecraftMediaLibrary/EzMediaCore) by using the
   following Git link: `https://github.com/MinecraftMediaLibrary/EzMediaCore.git`
3) Run `gradlew shadowJar` on the parent project to build a jar for the plugin.

---

### Partnerships

[![BisectHosting](https://www.bisecthosting.com/partners/custom-banners/ba551f73-9616-4d2c-b40b-293493ca5124.png)](https://bisecthosting.com/pulse)

I'd like to also thank [BisectHosting](https://bisecthosting.com/pulse) for sponsoring open-source
projects such as this one as well. Use code `pulse` on your next purchase to get 25% off your next
server!

---

### Contributors / Acknowledgements

| Developer                                                          | Contribution                                         |
|--------------------------------------------------------------------|------------------------------------------------------|
| [BananaPuncher714](https://github.com/BananaPuncher714)            | Created the original MinecraftVideo plugin           |
| [Jetp250](https://github.com/jetp250)                              | Developed Floyd Steinberg dithering                  |
| [Emilyy](https://github.com/emilyy-dev)      | Assisted with implementation and testing             |
| [Conclure](https://github.com/Conclure) | Assisted with Maven to Gradle migration              |
| [itxfrosty](https://github.com/itxfrosty)                          | Developed a Discord bot for music integration        |
| [Rouge_Ram](https://rogueram.xyz/index.html)                       | Developed a Discord bot used in Pulse Development!   |

| Sponsor                 | Donation                 |
|-------------------------|--------------------------|
| Vijay Pondini           | $10.00                   |
| Matthew Holden          | $6.00                    |

| Project / Dependency                                                                        | Contribution                       |
|---------------------------------------------------------------------------------------------|------------------------------------|
| [VLCJ](https://github.com/caprica/vlcj) made by [caprica](https://github.com/caprica)       | VLC Media Player Bindings          |
| [VLC Media Player](https://www.videolan.org/vlc/)                                           | Native Backend Support             |
| [Jaffree](https://github.com/kokorin/Jaffree) made by [kokorin](https://github.com/kokorin) | FFmpeg Integration (NUT Container) |
| [FFmpeg](https://www.ffmpeg.org/)                                                           | Native Media Encoding and Decoding |

### Projects that use EzMediaCore Code

| Project                                                   | Description                            |
|-----------------------------------------------------------|----------------------------------------|
| [MakiDesktop](https://github.com/ayunami2000/MakiDesktop) | Controlling VNC through Minecraft Maps |
| [MakiScreen](https://github.com/makifoxgirl/MakiScreen)   | Streaming OBS onto Minecraft Maps      |

---
