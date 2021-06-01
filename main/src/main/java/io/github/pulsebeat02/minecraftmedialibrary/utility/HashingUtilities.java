/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
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

package io.github.pulsebeat02.minecraftmedialibrary.utility;

import io.github.pulsebeat02.minecraftmedialibrary.dependency.RepositoryDependency;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Hashing Utilities for getting the hash of certain artifacts and files. */
public final class HashingUtilities {

  /**
   * Checks if the hash of the file matches the dependency.
   *
   * @param file the file
   * @param dependency the dependency
   * @return checks that the hash of the file matches the dependency
   */
  public static boolean validateDependency(
      @NotNull final Path file, @NotNull final RepositoryDependency dependency) {
    return getHash(file).equals(getDependencyHash(dependency));
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
  public static String toHexString(@NotNull final byte[] bytes) {
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

  /**
   * Gets the dependency hash from an artifact.
   *
   * @param dependency the dependency
   * @return the hash
   */
  @NotNull
  public static String getDependencyHash(@NotNull final RepositoryDependency dependency) {
    return getHashFromUrl(getDependencyHashUrl(dependency));
  }

  /**
   * Gets the dependency hash from an artifact.
   *
   * @param groupId the group id
   * @param artifactId the artifact id
   * @param version the version
   * @param base the base
   * @return the hash
   */
  @NotNull
  public static String getDependencyHash(
      @NotNull final String groupId,
      @NotNull final String artifactId,
      @NotNull final String version,
      @NotNull final String base) {
    return getHashFromUrl(getDependencyHashUrl(groupId, artifactId, version, base));
  }

  /**
   * Retrieves the hash from a url leading to the hash file.
   *
   * @param url the url
   * @return the hash
   */
  @NotNull
  public static String getHashFromUrl(@NotNull final String url) {
    try (final BufferedReader in =
        new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
      return in.readLine();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return "";
  }

  /**
   * Gets the dependency hash url for an artifact.
   *
   * @param groupId the group id
   * @param artifactId the artifact id
   * @param version the version
   * @param base the base
   * @return the url
   */
  @NotNull
  public static String getDependencyHashUrl(
      @NotNull final String groupId,
      @NotNull final String artifactId,
      @NotNull final String version,
      @NotNull final String base) {
    return String.format(
        "%s%s-%s.jar.sha1",
        DependencyUtilities.getDependencyUrl(groupId, artifactId, version, base),
        artifactId,
        version);
  }

  /**
   * Gets the dependency hash url for an artifact.
   *
   * @param dependency the dependency
   * @return the url
   */
  @NotNull
  public static String getDependencyHashUrl(@NotNull final RepositoryDependency dependency) {
    return String.format(
        "%s%s-%s.jar.sha1",
        DependencyUtilities.getDependencyUrl(dependency),
        dependency.getArtifact(),
        dependency.getVersion());
  }
}
