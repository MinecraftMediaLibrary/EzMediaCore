/*............................................................................................
 . Copyright © 2021 PulseBeat_02                                                             .
 .                                                                                           .
 . Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
 . software and associated documentation files (the “Software”), to deal in the Software     .
 . without restriction, including without limitation the rights to use, copy, modify, merge, .
 . publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
 . persons to whom the Software is furnished to do so, subject to the following conditions:  .
 .                                                                                           .
 . The above copyright notice and this permission notice shall be included in all copies     .
 . or substantial portions of the Software.                                                  .
 .                                                                                           .
 . THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
 .  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
 .   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
 .   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
 .   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
 .   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
 .   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
 .   SOFTWARE.                                                                               .
 ............................................................................................*/

package com.github.pulsebeat02.minecraftmedialibrary.exception;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown if the operating system the current environment is using is unsupported by the library.
 */
public class UnsupportedOperatingSystemException extends AssertionError {

  private static final long serialVersionUID = 1682368011870345698L;

  /**
   * Instantiates a new UnsupportedOperatingSystemException.
   *
   * @param message the exception message
   */
  public UnsupportedOperatingSystemException(@NotNull final String message) {
    super(message);
  }

  /**
   * Gets the cause of the exception.
   *
   * @return this
   */
  @Override
  public synchronized Throwable getCause() {
    return this;
  }

  /**
   * Initializes the cause of the exception.
   *
   * @param cause cause
   * @return this
   */
  @Override
  public synchronized Throwable initCause(@NotNull final Throwable cause) {
    return this;
  }

  /**
   * Fills in stack trace for exception.
   *
   * @return throwable stack trace
   */
  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
