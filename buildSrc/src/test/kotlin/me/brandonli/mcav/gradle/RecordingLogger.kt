/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.brandonli.mcav.gradle

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import org.gradle.api.logging.Logger
import org.slf4j.helpers.FormattingTuple
import org.slf4j.helpers.MessageFormatter

class RecordingLogger {

    val messages: MutableList<String> = mutableListOf<String>()

    val logger: Logger = this.createLogger()

    private fun createLogger(): Logger {
        val loader: ClassLoader = Logger::class.java.classLoader
        val interfaces: Array<Class<*>> = arrayOf(Logger::class.java)
        val handler: InvocationHandler = InvocationHandler { _, method, arguments -> this.record(method, arguments) }
        return Proxy.newProxyInstance(loader, interfaces, handler) as Logger
    }

    private fun record(method: Method, arguments: Array<out Any?>?): Any? {
        if (method.returnType == Boolean::class.javaPrimitiveType) {
            return false
        }

        val logged: List<Any?> = arguments.orEmpty().flatMap(this::valuesOf)
        val template: String = logged.first() as String
        val values: Array<Any?> = logged.drop(1).toTypedArray()
        val formatted: FormattingTuple = MessageFormatter.arrayFormat(template, values)
        this.messages += "${method.name}: ${formatted.message}"

        return null
    }

    private fun valuesOf(argument: Any?): List<Any?> {
        return if (argument is Array<*>) argument.toList() else listOf(argument)
    }
}
