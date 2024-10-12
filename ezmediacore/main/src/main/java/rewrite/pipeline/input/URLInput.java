package rewrite.pipeline.input;

import java.nio.file.Path;

public final class URLInput implements Input {

  private final String raw;

  public URLInput(final String raw) {
    this.raw = raw;
  }

  @Override
  public String getMediaRepresentation() {



    return this.mrl;
  }

  private String getJSONDump() {
    final Path executable = Path.of("");
    final String raw = executable.toString();
    
  }


}
