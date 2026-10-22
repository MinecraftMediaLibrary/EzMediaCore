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

    private companion object {

        const val LINE_SEPARATOR: String = "\n"
    }

    @Test
    fun shouldAcceptCommentsAroundAConstantMessage(): Unit {
        val sources: List<String> = listOf(
            "LOGGER.info(/* why this message */ STARTED);",
            "LOGGER.info(STARTED /* why this message */);",
            "LOGGER.info(// why this message\n STARTED);",
        )
        val found: List<ConcatenatedLogMessage> = sources.flatMap { LogMessages.find(it) }
        assertEquals(emptyList<ConcatenatedLogMessage>(), found)
    }

    @Test
    fun shouldNotTreatQuotedCommentTextAsStringConcatenation(): Unit {
        val source: String = "LOGGER.info(STARTED, count + 1 /* \"units\" */);"
        val found: List<ConcatenatedLogMessage> = LogMessages.find(source)
        assertEquals(emptyList<ConcatenatedLogMessage>(), found)
    }

    @Test
    fun shouldRejectLiteralConcatenationBesideAComment(): Unit {
        val source: String = "LOGGER.info(/* explanation */ \"\" + STARTED);"
        val found: List<ConcatenatedLogMessage> = LogMessages.find(source)
        assertEquals(listOf(ConcatenatedLogMessage(1, "LOGGER.info")), found)
    }

    @Test
    fun shouldAcceptAMessageInAConstant(): Unit {
        val source: String = """
            final class Example {
              private static final String FAILED_TO_START = "Failed to start {}";
              void run() {
                LOGGER.error(FAILED_TO_START, name, exception);
                LOGGER.info(Messages.STARTED);
                this.logger.warn(NOT_STOPPED, thread, 50L);
              }
            }
        """.trimIndent()
        val found: List<ConcatenatedLogMessage> = LogMessages.find(source)
        assertEquals(emptyList<ConcatenatedLogMessage>(), found)
    }

    @Test
    fun shouldAcceptAMessageWrittenInline(): Unit {
        val source: String = """
            final class Example {
              void run() {
                LOGGER.info("Started {}", name);
              }
            }
        """.trimIndent()
        val found: List<ConcatenatedLogMessage> = LogMessages.find(source)
        assertEquals(emptyList<ConcatenatedLogMessage>(), found)
    }

    @Test
    fun shouldReportEveryLevelOnEveryKindOfLogger(): Unit {
        val source: String = listOf(
            "log.trace(\"a\" + name);",
            "logger.debug(\"b\" + name);",
            "LOG.info(\"c\" + name);",
            "AUDIT_LOGGER.warn(\"d\" + name);",
            "pluginLogger.error(\"e\" + name);",
        ).joinToString(LINE_SEPARATOR)
        val lines: List<Int> = LogMessages.find(source).map { it.line }
        assertEquals(listOf(1, 2, 3, 4, 5), lines)
    }

    @Test
    fun shouldAcceptAMessageThatIsNotAConstant(): Unit {
        val source: String = listOf(
            "LOGGER.error(message, error);",
            "LOGGER.warn(prefix() + NAME);",
            "LOGGER.info(Failed.name());",
        ).joinToString(LINE_SEPARATOR)
        val found: List<ConcatenatedLogMessage> = LogMessages.find(source)
        assertEquals(emptyList<ConcatenatedLogMessage>(), found)
    }

    @Test
    fun shouldReportAnArgumentThatJoinsStrings(): Unit {
        val source: String = listOf(
            "LOGGER.warn(FAILED, \"at \" + place);",
            "LOGGER.warn(FAILED, count + 1, String.join(\",\", names));",
            "LOGGER.warn(FAILED, place.concat(\"x\" + y));",
        ).joinToString(LINE_SEPARATOR)
        val lines: List<Int> = LogMessages.find(source).map { it.line }
        assertEquals(listOf(1), lines)
    }

    @Test
    fun shouldReadEachArgumentOnItsOwnAndPastNestedCalls(): Unit {
        val source: String = listOf(
            "LOGGER.info(\"Started {} of {}\", count + 1, total);",
            "LOGGER.info(format(name), \"at \" + place);",
            "LOGGER.info(name() + \" started\");",
        ).joinToString(LINE_SEPARATOR)
        val lines: List<Int> = LogMessages.find(source).map { it.line }
        assertEquals(listOf(2, 3), lines)
    }

    @Test
    fun shouldReadACallAcrossLinesAndInsideALambda(): Unit {
        val source: String = """
            final class Example {
              void run() {
                budget.log(() -> LOGGER.warn(
                  "The browser could not load " + url,
                  text
                ), Example::skipped);
              }
            }
        """.trimIndent()
        val found: List<ConcatenatedLogMessage> = LogMessages.find(source)
        assertEquals(listOf(ConcatenatedLogMessage(3, "LOGGER.warn")), found)
    }

    @Test
    fun shouldIgnoreCallsThatAreNotLogCalls(): Unit {
        val source: String = listOf(
            "verify(logger).warn(\"checked by \" + test);",
            "counter.info(\"not a \" + logger);",
            "LOGGER.isDebugEnabled();",
            "log.info();",
            "// LOGGER.info(\"in a \" + comment);",
            "String text = \"LOGGER.info(\\\"in a \\\" + string)\";",
        ).joinToString(LINE_SEPARATOR)
        val found: List<ConcatenatedLogMessage> = LogMessages.find(source)
        assertEquals(emptyList<ConcatenatedLogMessage>(), found)
    }

    @Test
    fun shouldReadParenthesesAndCommasInLiteralsAsText(): Unit {
        val source: String = "LOGGER.info(STARTED, \"(,\", ')', \"at \" + name);\nLOGGER.info(\"x\");"
        val lines: List<Int> = LogMessages.find(source).map { it.line }
        assertEquals(listOf(1), lines)
    }

    @Test
    fun shouldReadNoArgumentsFromACallThatTheSourceNeverCloses(): Unit {
        val source: String = "LOGGER.info(\"Started \" + name"

        val found: List<ConcatenatedLogMessage> = LogMessages.find(source)
        assertEquals(emptyList<ConcatenatedLogMessage>(), found)
    }
}
