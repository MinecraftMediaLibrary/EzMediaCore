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

import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter
import org.gradle.api.tasks.testing.Test

/**
 * Tells whether a test task runs only some of its tests.
 */
object TestFilters {

    /**
     * Returns whether `--tests`, a filter pattern, or an include or exclude pattern narrows [test].
     *
     * @param test the test task to check
     * @return whether some of its tests are left out
     */
    fun isFiltered(test: Test): Boolean {
        val filter: DefaultTestFilter = test.filter as DefaultTestFilter
        return filter.commandLineIncludePatterns.isNotEmpty() || filter.includePatterns.isNotEmpty() ||
            filter.excludePatterns.isNotEmpty() || test.includes.isNotEmpty() || test.excludes.isNotEmpty()
    }
}
