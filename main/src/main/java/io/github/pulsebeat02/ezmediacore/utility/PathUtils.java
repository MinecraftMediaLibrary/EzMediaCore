package io.github.pulsebeat02.ezmediacore.utility;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.annotations.NotNull;

public final class PathUtils {

  private PathUtils() {}

  /**
   *
   *
   * <pre>
   * Checks if a string is a valid path.
   * Null safe.
   *
   * Calling examples:
   *    isValidPath("c:/test");      //returns true
   *    isValidPath("c:/te:t");      //returns false
   *    isValidPath("c:/te?t");      //returns false
   *    isValidPath("c/te*t");       //returns false
   *    isValidPath("good.txt");     //returns true
   *    isValidPath("not|good.txt"); //returns false
   *    isValidPath("not:good.txt"); //returns false
   * </pre>
   *
   * @param path the path
   * @return whether the path is valid
   */
  public static boolean isValidPath(@NotNull final String path) {
    try {
      Paths.get(path);
    } catch (final InvalidPathException | NullPointerException ex) {
      return false;
    }
    return true;
  }

  public static String getName(@NotNull final Path path) {
    return path.getFileName().toString();
  }
}
