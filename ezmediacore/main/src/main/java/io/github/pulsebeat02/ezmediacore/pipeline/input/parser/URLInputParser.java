package io.github.pulsebeat02.ezmediacore.pipeline.input.parser;

import com.google.gson.Gson;
import io.github.pulsebeat02.ezmediacore.capabilities.Capabilities;
import io.github.pulsebeat02.ezmediacore.capabilities.YTDLPCapability;
import io.github.pulsebeat02.ezmediacore.json.GsonProvider;
import io.github.pulsebeat02.ezmediacore.pipeline.input.Input;
import io.github.pulsebeat02.ezmediacore.pipeline.input.parser.extractor.URLParseDump;
import io.github.pulsebeat02.ezmediacore.pipeline.input.parser.strategy.FormatStrategy;
import io.github.pulsebeat02.ezmediacore.util.task.CommandTask;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class URLInputParser {

  private final String raw;
  private final ExecutorService service;
  private URLParseDump dump;

  public URLInputParser(final String raw) {
    this(raw, Executors.newSingleThreadExecutor());
  }

  public URLInputParser(final String raw, final ExecutorService service) {
    this.raw = raw;
    this.service = service;
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
    if (this.dump != null) {
      return CompletableFuture.completedFuture(this.dump);
    }
    return this.getJSONDump().thenApply(this::parseJSON).thenApply(this::assignAndReturn);
  }

  private URLParseDump assignAndReturn(final URLParseDump dump) {
    this.dump = dump;
    return dump;
  }

  private URLParseDump parseJSON(final String json) {
    final Gson gson = GsonProvider.getSimple();
    return gson.fromJson(json, URLParseDump.class);
  }

  private CompletableFuture<String> getJSONDump() {
    try (this.service) {
      final YTDLPCapability capability = Capabilities.YT_DLP;
      final Path executable = capability.getBinaryPath();
      final String executablePath = executable.toString();
      final CommandTask task = new CommandTask(executablePath, "--dump-json", this.raw);
      return CompletableFuture.supplyAsync(() -> this.getTaskOutput(task), this.service);
    }
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
