[![Build Status](https://img.shields.io/circleci/build/github/PulseBeat02/MinecraftMediaLibrary?style=for-the-badge)](https://app.circleci.com/pipelines/github/PulseBeat02/MinecraftMediaLibrary)
[![Language](https://img.shields.io/badge/Made%20with-Java-1f425f.svg?style=for-the-badge)](https://www.java.com/en/)

[![Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/PulseBeat02/MinecraftMediaLibrary)

<img src="https://i.imgur.com/48CJD9j.png" alt="drawing" width="1000"/>

---

## Introduction
**MinecraftMediaLibrary** is a library written along the Spigot API and net.minecraft.server classes to provide helpful
and useful classes for other plugins to take advantage of. One of the most important features perhaps is its ability to
play **videos** on a Minecraft Spigot server. It uses a [very optimized dithering method](https://github.com/PulseBeat02/MinecraftMediaLibrary/blob/b47e10869cbcea03889670765aa3ef66d6ba171a/MinecraftMediaLibrary/src/main/java/com/github/pulsebeat02/minecraftmedialibrary/video/dither/FloydImageDither.java#L177) (
credits to **BananaPuncher714** and **Jetp250** for helping out with the Floyd Steinberg Dithering) along side with
**VLCJ Integration** if necessary to parse the video even quicker. As a result, frames can reach up to **35** times per
second at times with very good quality on maps if necessary. As a reference, a *smooth* animation is one which is 24
frames only.

The plugin takes advantages of maps to handle its video playback. Currently, it uses maps with ids **0 to 25** to
display the video. However, it is likely in the near future I will add an implementation which allows you to change
this.

![Swag](http://ForTheBadge.com/images/badges/built-with-swag.svg)
## Jump Guide
Due to financial concerns, I am unable to provide a Maven central repository to host my artifacts. However, the library
is usable if you use Jitpack.

Repository:
```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

Dependency:
```xml
	<dependency>
	    <groupId>com.github.PulseBeat02</groupId>
	    <artifactId>MinecraftMediaLibrary</artifactId>
	    <version>master-SNAPSHOT</version>
	</dependency>
```

Most of the code is documented which can be found [here](https://pulsebeat02.github.io/MinecraftMediaLibrary/).

## Terms of Usage
Although MinecraftMediaLibrary is open source, I do provide some restrictions. You are to not:

1) Use the code for comerical purposes. Or to use the library to develop a plugin being sold to others. Requires permission by the author (PulseBeat_02) first.
2) Rename the project to something else, obfuscate the code, and sell it. Similar to rule one, but this one is more focused around not copying someone else's work.

However, you are welcome to fork the project, use it for your free libraries. I know that these restrictions may seem super limiting, but they are put into place because I spent a lot of time on this project, and I don't want others to copy work without crediting.
