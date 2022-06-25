package io.github.pulsebeat02.ezmediacore.utility.http;

import io.github.pulsebeat02.ezmediacore.utility.io.PathUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.regex.Pattern;

public final class HttpUtils {

  private static final Pattern INSECURE_URI;
  private static final Pattern VALID_FILE_NAME;

  public static final String BASE_FOLDER_HTML_CONTENT;
  public static final String FILE_HTML_CONTENT;
  public static final String HTML_BODY;

  static {
    INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    VALID_FILE_NAME = Pattern.compile("[^-._]?[^<>&\"]*");
    BASE_FOLDER_HTML_CONTENT =
            """
                    <!DOCTYPE html>\r
                    <html><head><meta charset='utf-8' /><title>Listing of: %s</title></head><body>\r
                    <h3>Listing of: %s</h3>\r
                    <ul><li><a href="../">..</a></li>\r
                    """;
    FILE_HTML_CONTENT = "<li><a href=\"" + "%s" + "\">" + "%s" + "</a></li>\r\n";
    HTML_BODY = "</ul></body></html>\r\n";
  }

  private HttpUtils() {}

  public static boolean checkTreeAttack(@NotNull final String result) {
    return result.contains("/.")
        || result.contains("./")
        || result.charAt(0) == '.'
        || result.charAt(result.length() - 1) == '.'
        || INSECURE_URI.matcher(result).matches();
  }

  public static boolean isValidFileName(@NotNull final Path path) {
    return VALID_FILE_NAME.matcher(PathUtils.getName(path)).matches();
  }

  public static String createBaseHtmlContent(@NotNull final Path directory) {
    return BASE_FOLDER_HTML_CONTENT.formatted(directory, directory);
  }

  public static String createFileHtmlContent(@NotNull final Path file) {
    return FILE_HTML_CONTENT.formatted(file, file);
  }
}
