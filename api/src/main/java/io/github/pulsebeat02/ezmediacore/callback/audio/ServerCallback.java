package io.github.pulsebeat02.ezmediacore.callback.audio;

import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Nullable;

public interface ServerCallback {

  @Nullable CompletableFuture<Void> getServerFuture();
}
