package com.github.pulsebeat02.minecraftmedialibrary.test.lab

import java.nio.file.Paths

fun main() {
    println(
            Paths.get("C:\\Users\\Brandon Li\\Desktop\\server\\plugins\\DeluxeMediaPlugin\\mml\\http").normalize()
                    .relativize(Paths.get("C:\\Users\\Brandon Li\\Desktop\\server\\plugins\\DeluxeMediaPlugin/mml/http/resourcepack.zip")).normalize()
                    .toString())
}