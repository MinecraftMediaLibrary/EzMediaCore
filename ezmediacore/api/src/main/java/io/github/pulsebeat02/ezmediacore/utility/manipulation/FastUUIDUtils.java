/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.utility.manipulation;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Jon Chambers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.Arrays;
import java.util.UUID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class for quickly and efficiently parsing {@link java.util.UUID} instances from strings
 * and writing UUID instances as strings. The methods contained in this class are optimized for
 * speed and to minimize garbage collection pressure. In benchmarks, {@link
 * #parseUUID(CharSequence)} is a little more than 14 times faster than {@link
 * UUID#fromString(String)}, Under Java 9 and newer, {@link #parseUUID(CharSequence)} is about six
 * times faster than the JDK implementation.
 *
 * @author <a href="https://github.com/jchambers/">Jon Chambers</a>
 */
public class FastUUIDUtils {

  private static final int UUID_STRING_LENGTH = 36;
  private static final long[] HEX_VALUES = new long[128];

  static {
    Arrays.fill(HEX_VALUES, -1);
    HEX_VALUES['0'] = 0x0;
    HEX_VALUES['1'] = 0x1;
    HEX_VALUES['2'] = 0x2;
    HEX_VALUES['3'] = 0x3;
    HEX_VALUES['4'] = 0x4;
    HEX_VALUES['5'] = 0x5;
    HEX_VALUES['6'] = 0x6;
    HEX_VALUES['7'] = 0x7;
    HEX_VALUES['8'] = 0x8;
    HEX_VALUES['9'] = 0x9;
    HEX_VALUES['a'] = 0xa;
    HEX_VALUES['b'] = 0xb;
    HEX_VALUES['c'] = 0xc;
    HEX_VALUES['d'] = 0xd;
    HEX_VALUES['e'] = 0xe;
    HEX_VALUES['f'] = 0xf;
    HEX_VALUES['A'] = 0xa;
    HEX_VALUES['B'] = 0xb;
    HEX_VALUES['C'] = 0xc;
    HEX_VALUES['D'] = 0xd;
    HEX_VALUES['E'] = 0xe;
    HEX_VALUES['F'] = 0xf;
  }

  private FastUUIDUtils() {}

  /**
   * Parses a UUID from the given character sequence. The character sequence must represent a UUID
   * as described in {@link UUID#toString()}.
   *
   * @param uuidSequence the character sequence from which to parse a UUID
   * @return the UUID represented by the given character sequence
   * @throws IllegalArgumentException if the given character sequence does not conform to the string
   *     representation as described in {@link UUID#toString()}
   */
  @Contract("_ -> new")
  public static @NotNull UUID parseUUID(@NotNull final CharSequence uuidSequence) {

    long mostSignificantBits = HEX_VALUES[uuidSequence.charAt(0)] << 60;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(1)] << 56;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(2)] << 52;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(3)] << 48;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(4)] << 44;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(5)] << 40;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(6)] << 36;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(7)] << 32;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(9)] << 28;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(10)] << 24;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(11)] << 20;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(12)] << 16;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(14)] << 12;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(15)] << 8;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(16)] << 4;
    mostSignificantBits |= HEX_VALUES[uuidSequence.charAt(17)];
    long leastSignificantBits = HEX_VALUES[uuidSequence.charAt(19)] << 60;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(20)] << 56;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(21)] << 52;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(22)] << 48;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(24)] << 44;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(25)] << 40;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(26)] << 36;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(27)] << 32;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(28)] << 28;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(29)] << 24;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(30)] << 20;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(31)] << 16;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(32)] << 12;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(33)] << 8;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(34)] << 4;
    leastSignificantBits |= HEX_VALUES[uuidSequence.charAt(35)];

    return new UUID(mostSignificantBits, leastSignificantBits);
  }
}
