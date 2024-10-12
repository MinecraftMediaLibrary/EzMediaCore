/**
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
package io.github.pulsebeat02.ezmediacore.nativelibraryloader.locale;

/** Internal Locale used for messages in exceptions, debug statements, etc. */
public interface Locale {

  UniComponent<String> ERR_LOADING_NATIVE_LIBRARY =
      (arg) ->
          String.format(
              "A severe issue occurred while attempting to load native library %s!%n", arg);

  @FunctionalInterface
  interface NullComponent {

    String build();
  }

  @FunctionalInterface
  interface UniComponent<A0> {
    String build(A0 arg0);
  }

  @FunctionalInterface
  interface BiComponent<A0, A1> {

    String build(A0 arg0, A1 arg1);
  }

  @FunctionalInterface
  interface TriComponent<A0, A1, A2> {

    String build(A0 arg0, A1 arg1, A2 arg2);
  }

  @FunctionalInterface
  interface QuadComponent<A0, A1, A2, A3> {

    String build(A0 arg0, A1 arg1, A2 arg2, A3 arg3);
  }

  @FunctionalInterface
  interface PentaComponent<A0, A1, A2, A3, A4> {

    String build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4);
  }

  @FunctionalInterface
  interface HexaComponent<A0, A1, A2, A3, A4, A5> {

    String build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5);
  }

  @FunctionalInterface
  interface HeptaComponent<A0, A1, A2, A3, A4, A5, A6> {

    String build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6);
  }
}
