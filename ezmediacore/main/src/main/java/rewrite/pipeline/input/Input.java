package rewrite.pipeline.input;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface Input {
  Input EMPTY = () -> CompletableFuture.completedFuture("");
  CompletableFuture<String> getMediaRepresentation();
}
