package rewrite.pipeline.input.parser;

import com.google.gson.Gson;
import rewrite.json.GsonProvider;
import rewrite.pipeline.input.Input;
import rewrite.pipeline.input.parser.extractor.URLParseDump;
import rewrite.pipeline.input.parser.strategy.FormatStrategy;
import rewrite.task.CommandTask;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class URLInputParser {

  private final String raw;
  private URLParseDump dump;

  public URLInputParser(final String raw) {
    this.raw = raw;
  }

  public CompletableFuture<Boolean> isLiveStream() {
    return this.retrieveDumpInternal().thenApply(dump -> dump.is_live);
  }

  public CompletableFuture<Input> retrieveInput(final FormatStrategy strategy) {
    return this.retrieveDumpInternal()
            .thenApply(ignored -> strategy.getNeededInput(this.dump));
  }

  private CompletableFuture<URLParseDump> retrieveDumpInternal() {
    return this.dump == null ? this.retrieveJSONDump() : CompletableFuture.completedFuture(this.dump);
  }

  public CompletableFuture<URLParseDump> retrieveJSONDump() {
    return this.retrieveJSONDump(Executors.newSingleThreadExecutor());
  }

  public CompletableFuture<URLParseDump> retrieveJSONDump(final ExecutorService service) {
    return this.getJSONDump(service).thenApply(this::parseJSON).thenApply(this::assignAndReturn);
  }

  private URLParseDump assignAndReturn(final URLParseDump dump) {
    this.dump = dump;
    return dump;
  }

  private URLParseDump parseJSON(final String json) {
    final Gson gson = GsonProvider.getSimple();
    return gson.fromJson(json, URLParseDump.class);
  }

  private CompletableFuture<String> getJSONDump(final ExecutorService service) {
    final Path executable = Path.of("");
    final String executablePath = executable.toString();
    final CommandTask task = new CommandTask(executablePath, "--dump-json", this.raw);
    return CompletableFuture.supplyAsync(() -> this.getTaskOutput(task), service);
  }

  private String getTaskOutput(final CommandTask task) {
    try {
      task.run();
      return task.getOutput();
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }
}
