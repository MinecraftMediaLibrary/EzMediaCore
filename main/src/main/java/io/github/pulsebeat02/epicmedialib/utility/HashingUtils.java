package io.github.pulsebeat02.epicmedialib.utility;

import io.github.pulsebeat02.epicmedialib.dependency.DependencyInfo;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public final class HashingUtils {

  private static final Map<String, String> ARTIFACT_HASHES;

  static {
    ARTIFACT_HASHES = new HashMap<>();
  }

  private HashingUtils() {}

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

  /**
   * Checks if the hash of the file matches the dependency.
   *
   * @param file the file
   * @param dependency the dependency
   * @return checks that the hash of the file matches the dependency
   */
  public static boolean validateDependency(
      @NotNull final Path file, @NotNull final DependencyInfo dependency) {
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

  /**
   * Gets the dependency hash from an artifact.
   *
   * @param dependency the dependency
   * @return the hash
   */
  @NotNull
  public static String getDependencyHash(@NotNull final DependencyInfo dependency) {
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
    if (ARTIFACT_HASHES.containsKey(url)) {
      return ARTIFACT_HASHES.get(url);
    }
    try (final BufferedReader in =
        new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
      final String hash = in.readLine();
      ARTIFACT_HASHES.put(url, hash);
      return hash;
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
        DependencyUtils.getDependencyUrl(groupId, artifactId, version, base), artifactId, version);
  }

  /**
   * Gets the dependency hash url for an artifact.
   *
   * @param dependency the dependency
   * @return the url
   */
  @NotNull
  public static String getDependencyHashUrl(@NotNull final DependencyInfo dependency) {
    return String.format(
        "%s%s-%s.jar.sha1",
        DependencyUtils.getDependencyUrl(dependency),
        dependency.getArtifact(),
        dependency.getVersion());
  }
}
