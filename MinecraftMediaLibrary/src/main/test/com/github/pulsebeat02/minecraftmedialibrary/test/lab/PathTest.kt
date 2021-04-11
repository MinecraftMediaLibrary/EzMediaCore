package com.github.pulsebeat02.minecraftmedialibrary.test.lab

import java.nio.file.Paths

fun main() {
    val parent = Paths.get("a/b/c/d")
    val child = Paths.get("a/b/c/d/e")
    println(parent.relativize(child))
}