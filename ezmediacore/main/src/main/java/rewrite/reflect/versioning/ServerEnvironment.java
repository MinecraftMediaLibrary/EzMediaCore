package rewrite.reflect.versioning;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;

public final class ServerEnvironment {

  private static final String PATTERN = "(?i)\\(MC: (\\d)\\.(\\d++)\\.?(\\d++)?(?: (Pre-Release|Release Candidate) )?(\\d)?\\)";
  private static final Pattern VERSION_PATTERN = Pattern.compile(PATTERN);
  private static final String PACKAGE_PATTERN = "v1_%s_R%s";

  private static final String NMS_REVISION;

  static {
    final String raw = Bukkit.getVersion();
    final Matcher matcher = VERSION_PATTERN.matcher(raw);
    if (matcher.find()) {
      final MatchResult matchResult = matcher.toMatchResult();
      final String version = matchResult.group(2);
      final String patchVersion = matchResult.group(3);
      NMS_REVISION = PACKAGE_PATTERN.formatted(version, patchVersion);
    } else {
      throw new UnsupportedOperationException("The current server version is not supported!");
    }
  }

  public static String getNMSRevision() {
    return NMS_REVISION;
  }
}
