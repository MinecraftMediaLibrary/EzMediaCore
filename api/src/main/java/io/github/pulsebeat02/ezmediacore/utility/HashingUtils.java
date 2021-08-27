/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public final class HashingUtils {

  private HashingUtils() {
  }

  public static Optional<byte[]> createHashSHA(@NotNull final Path file) {
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-1");
      final InputStream fis = new FileInputStream(file.toFile());
      int n = 0;
      final byte[] buffer = new byte[8192];
      while (n != -1) {
        n = fis.read(buffer);
        if (n > 0) {
          digest.update(buffer, 0, n);
        }
      }
      return Optional.of(digest.digest());
    } catch (final IOException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  @NotNull
  public static String getHash(@NotNull final Path file) {
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-1");
      try (final InputStream fis = new FileInputStream(file.toFile())) {
        int n = 0;
        final byte[] buffer = new byte[8192];
        while (n != -1) {
          n = fis.read(buffer);
          if (n > 0) {
            digest.update(buffer, 0, n);
          }
        }
      }
      return toHexString(digest.digest());
    } catch (final NoSuchAlgorithmException | IOException e) {
      e.printStackTrace();
    }
    return "";
  }

  /**
   * Converts the bytes to a hexadecimal String
   *
   * @param bytes the bytes
   * @return the hexadecimal String
   */
  @NotNull
  public static String toHexString(final byte[] bytes) {
    final StringBuilder hexString = new StringBuilder();
    for (final byte b : bytes) {
      final String hex = Integer.toHexString(0xFF & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
}
