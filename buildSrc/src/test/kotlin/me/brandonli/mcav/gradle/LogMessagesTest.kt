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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LogMessagesTest {

    @Test
    fun acceptsCommentsAroundAConstantMessage() {
        for (source in listOf(
            "LOGGER.info(/* why this message */ STARTED);",
            "LOGGER.info(STARTED /* why this message */);",
            "LOGGER.info(// why this message\n STARTED);",
        )) {
            assertEquals(emptyList<ConcatenatedLogMessage>(), LogMessages.find(source), source)
        }
    }

    @Test
    fun doesNotTreatQuotedCommentTextAsStringConcatenation() {
        val source = "LOGGER.info(STARTED, count + 1 /* \"units\" */);"
        assertEquals(emptyList<ConcatenatedLogMessage>(), LogMessages.find(source))
    }

    @Test
    fun stillRejectsLiteralConcatenationBesideAComment() {
        val source = "LOGGER.info(/* explanation */ \"\" + STARTED);"
        assertEquals(listOf(ConcatenatedLogMessage(1, "LOGGER.info")), LogMessages.find(source))
    }

    @Test
    fun acceptsAMessageInAConstant() {
        val source = """
            final class Example {
              private static final String FAILED_TO_START = "Failed to start {}";
              void run() {
                LOGGER.error(FAILED_TO_START, name, exception);
                LOGGER.info(Messages.STARTED);
                this.logger.warn(NOT_STOPPED, thread, 50L);
              }
            }
        """.trimIndent()
        assertEquals(emptyList<ConcatenatedLogMessage>(), LogMessages.find(source))
    }

    @Test
    fun acceptsAMessageWrittenInline() {
        val source = """
            final class Example {
              void run() {
                LOGGER.info("Started {}", name);
              }
            }
        """.trimIndent()
        assertEquals(emptyList<ConcatenatedLogMessage>(), LogMessages.find(source))
    }

    @Test
    fun reportsEveryLevelOnEveryKindOfLogger() {
        val source = listOf(
            "log.trace(\"a\" + name);",
            "logger.debug(\"b\" + name);",
            "LOG.info(\"c\" + name);",
            "AUDIT_LOGGER.warn(\"d\" + name);",
            "pluginLogger.error(\"e\" + name);",
        ).joinToString("\n")
        assertEquals(listOf(1, 2, 3, 4, 5), LogMessages.find(source).map { it.line })
    }

    @Test
    fun acceptsAMessageThatIsNotAConstant() {
        val source = listOf(
            "LOGGER.error(message, error);",
            "LOGGER.warn(prefix() + NAME);",
            "LOGGER.info(Failed.name());",
        ).joinToString("\n")
        assertEquals(emptyList<ConcatenatedLogMessage>(), LogMessages.find(source))
    }

    @Test
    fun reportsAnArgumentThatJoinsStrings() {
        val source = listOf(
            "LOGGER.warn(FAILED, \"at \" + place);",
            "LOGGER.warn(FAILED, count + 1, String.join(\",\", names));",
            "LOGGER.warn(FAILED, place.concat(\"x\" + y));",
        ).joinToString("\n")
        assertEquals(listOf(1), LogMessages.find(source).map { it.line })
    }

    @Test
    fun readsACallAcrossLinesAndInsideALambda() {
        val source = """
            final class Example {
              void run() {
                budget.log(() -> LOGGER.warn(
                  "The browser could not load " + url,
                  text
                ), Example::skipped);
              }
            }
        """.trimIndent()
        assertEquals(listOf(ConcatenatedLogMessage(3, "LOGGER.warn")), LogMessages.find(source))
    }

    @Test
    fun ignoresCallsThatAreNotLogCalls() {
        val source = listOf(
            "verify(logger).warn(\"checked by \" + test);",
            "counter.info(\"not a \" + logger);",
            "LOGGER.isDebugEnabled();",
            "log.info();",
            "// LOGGER.info(\"in a \" + comment);",
            "String text = \"LOGGER.info(\\\"in a \\\" + string)\";",
        ).joinToString("\n")
        assertEquals(emptyList<ConcatenatedLogMessage>(), LogMessages.find(source))
    }

    @Test
    fun readsParenthesesAndCommasInLiteralsAsText() {
        val source = "LOGGER.info(STARTED, \"(,\", ')', \"at \" + name);\nLOGGER.info(\"x\");"
        assertEquals(listOf(1), LogMessages.find(source).map { it.line })
    }
}
