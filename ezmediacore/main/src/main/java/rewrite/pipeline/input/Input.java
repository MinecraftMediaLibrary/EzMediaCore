package rewrite.pipeline.input;

@FunctionalInterface
public interface Input {
  Input EMPTY = () -> "";
  String getMediaRepresentation();
}
