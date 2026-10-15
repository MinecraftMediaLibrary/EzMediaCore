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

import java.io.File
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class FuzzTargetsTest {

    private companion object {

        const val TARGET_METHOD: String = "shouldParse"
        const val TEXT_TARGET_METHOD: String = "shouldParseAnyText"

        const val FUZZ_TEST_SOURCE: String = "ParserFuzzTest.java"
        const val PLAIN_TEST_SOURCE: String = "ParserTest.java"
        const val EARLIER_SOURCE: String = "a/EarlierFuzzTest.java"
        const val LATER_SOURCE: String = "b/LaterFuzzTest.java"

        const val FIRST_ROOT: String = "first"
        const val SECOND_ROOT: String = "second"
        const val EARLIER_PACKAGE: String = "a"
        const val LATER_PACKAGE: String = "b"

        const val PACKAGE_PATH: String = "me/brandonli/mcav/example"

        const val CLASS_NAME: String = "me.brandonli.mcav.example.ParserFuzzTest"
    }

    @TempDir
    lateinit var directory: Path

    @Test
    fun shouldFindEveryFuzzTestMethodInTheOrderOfTheSource(): Unit {
        val source: String = """
            final class ParserFuzzTest {
              @FuzzTest(maxDuration = "30s")
              void shouldParseAnyText(final FuzzedDataProvider data) {
              }

              @FuzzTest
              @Timeout(5)
              public static void shouldParseAnyBytes(final byte[] input) {
              }
            }
        """.trimIndent()

        val methods: List<String> = FuzzTargets.findMethods(source)
        val expected: List<String> = listOf(TEXT_TARGET_METHOD, "shouldParseAnyBytes")
        assertEquals(expected, methods)
    }

    @Test
    fun shouldIgnoreAnnotationsInCommentsAndStrings(): Unit {
        val source: String = """
            final class ParserFuzzTest {
              // @FuzzTest void shouldNotRun(final byte[] input)
              /* @FuzzTest void shouldNotRunEither(final byte[] input) */
              private static final String TEXT = "@FuzzTest void shouldNeverRun(";

              @Test
              void shouldParseOneExample() {
              }
            }
        """.trimIndent()

        val methods: List<String> = FuzzTargets.findMethods(source)
        assertEquals(listOf<String>(), methods)
    }

    @Test
    fun shouldNameTheClassOfEveryFuzzTestFileByItsPathAndSkipOtherFiles(): Unit {
        val root: File = this.directory.toFile()
        val packageDirectory: File = root.resolve(PACKAGE_PATH)
        packageDirectory.mkdirs()
        packageDirectory.resolve(FUZZ_TEST_SOURCE).writeText("@FuzzTest void shouldParse(final byte[] input) {}")
        packageDirectory.resolve(PLAIN_TEST_SOURCE).writeText("@FuzzTest void shouldNotBeFound(final byte[] input) {}")

        val targets: List<FuzzTarget> = FuzzTargets.find(listOf(root))
        val expected: List<FuzzTarget> = listOf(FuzzTarget(CLASS_NAME, TARGET_METHOD))
        assertEquals(expected, targets)
    }

    @Test
    fun shouldOrderTheTargetsOfSeveralDirectoriesByClass(): Unit {
        val first: File = this.directory.resolve(FIRST_ROOT).toFile()
        val second: File = this.directory.resolve(SECOND_ROOT).toFile()
        first.resolve(LATER_PACKAGE).mkdirs()
        second.resolve(EARLIER_PACKAGE).mkdirs()
        first.resolve(LATER_SOURCE).writeText("@FuzzTest void shouldRun(final byte[] input) {}")
        second.resolve(EARLIER_SOURCE).writeText("@FuzzTest void shouldRun(final byte[] input) {}")

        val targets: List<FuzzTarget> = FuzzTargets.find(listOf(first, second))
        val expected: List<FuzzTarget> = listOf(FuzzTarget("a.EarlierFuzzTest", "shouldRun"), FuzzTarget("b.LaterFuzzTest", "shouldRun"))
        assertEquals(expected, targets)
    }

    @Test
    fun shouldFilterAndNameATaskForOneMethod(): Unit {
        val target: FuzzTarget = FuzzTarget(CLASS_NAME, TEXT_TARGET_METHOD)

        val pattern: String = target.filterPattern()
        assertEquals("$CLASS_NAME.shouldParseAnyText", pattern)

        val taskName: String = target.taskName()
        assertEquals("fuzzParserFuzzTestShouldParseAnyText", taskName)

        val text: String = target.toString()
        assertEquals(pattern, text)
    }

    @Test
    fun shouldEqualOnlyATargetOfTheSameClassAndMethod(): Unit {
        val target: FuzzTarget = FuzzTarget(CLASS_NAME, TARGET_METHOD)
        val same: FuzzTarget = FuzzTarget(CLASS_NAME, TARGET_METHOD)
        val otherMethod: FuzzTarget = FuzzTarget(CLASS_NAME, "shouldRead")
        val otherClass: FuzzTarget = FuzzTarget("a.OtherFuzzTest", TARGET_METHOD)

        assertEquals(target, target)
        assertEquals(same, target)

        val sameHash: Int = same.hashCode()
        val targetHash: Int = target.hashCode()
        assertEquals(sameHash, targetHash)

        assertNotEquals(otherMethod, target)
        assertNotEquals(otherClass, target)
        assertNotEquals(target, CLASS_NAME)
    }
}
