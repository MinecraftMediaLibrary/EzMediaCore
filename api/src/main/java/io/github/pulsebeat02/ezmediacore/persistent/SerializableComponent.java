package io.github.pulsebeat02.ezmediacore.persistent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface SerializableComponent<T> {

  void serialize(@NotNull final Collection<T> list) throws IOException;

  List<T> deserialize() throws IOException;

  @NotNull
  Path getStorageFile();
}
