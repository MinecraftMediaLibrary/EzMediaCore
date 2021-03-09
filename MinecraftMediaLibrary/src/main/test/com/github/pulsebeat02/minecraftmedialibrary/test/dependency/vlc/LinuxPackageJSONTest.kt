/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/3/2021
 * ============================================================================
 */
package com.github.pulsebeat02.minecraftmedialibrary.test.dependency.vlc

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxOSPackages
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackage
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.nio.charset.StandardCharsets

object LinuxPackageJSONTest {

    private var MAP_STRING_LIST_LINUX_PACKAGE_TYPE_TOKEN: TypeToken<Map<String, List<LinuxPackage>>>? = null
    private var MAP_STRING_LINUX_OS_PACKAGE_TYPE_TOKEN: TypeToken<Map<String, LinuxOSPackages>>? = null
    private var GSON: Gson? = null
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val packages = GSON!!.fromJson<Map<String, List<LinuxPackage>>>(fileContents, MAP_STRING_LINUX_OS_PACKAGE_TYPE_TOKEN!!.type)
    }

    @get:Throws(IOException::class)
    private val fileContents: String
        private get() {
            val name = "linux-package-installation.json"
            val loader = LinuxPackageJSONTest::class.java.classLoader
            val input = loader.getResourceAsStream(name)
            return if (input == null) {
                throw IllegalArgumentException("file not found! $name")
            } else {
                IOUtils.toString(input, StandardCharsets.UTF_8.name())
            }
        }

    private class LinuxOSPackagesAdapter : TypeAdapter<LinuxOSPackages>() {
        override fun write(out: JsonWriter, linuxOSPackages: LinuxOSPackages) {
            GSON!!.toJson(
                    linuxOSPackages.links.asMap(),
                    MAP_STRING_LIST_LINUX_PACKAGE_TYPE_TOKEN!!.type,
                    out)
        }

        override fun read(`in`: JsonReader): LinuxOSPackages {
            val map = GSON!!.fromJson<Map<String, Collection<LinuxPackage>>>(`in`, MAP_STRING_LIST_LINUX_PACKAGE_TYPE_TOKEN!!.type)
            val multimap: ListMultimap<String, LinuxPackage> = ArrayListMultimap.create()
            map.forEach { (k: String?, iterable: Collection<LinuxPackage>?) -> multimap.putAll(k, iterable) }
            return LinuxOSPackages(multimap)
        }
    }

    init {
        MAP_STRING_LIST_LINUX_PACKAGE_TYPE_TOKEN = object : TypeToken<Map<String, List<LinuxPackage>>>() {}
        MAP_STRING_LINUX_OS_PACKAGE_TYPE_TOKEN = object : TypeToken<Map<String, LinuxOSPackages>>() {}
        GSON = GsonBuilder()
                .registerTypeAdapter(LinuxOSPackages::class.java, LinuxOSPackagesAdapter())
                .setPrettyPrinting()
                .create()
    }
}