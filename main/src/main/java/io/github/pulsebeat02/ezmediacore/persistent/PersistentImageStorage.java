package io.github.pulsebeat02.ezmediacore.persistent;

import com.google.common.reflect.TypeToken;
import io.github.pulsebeat02.ezmediacore.image.Image;
import io.github.pulsebeat02.ezmediacore.json.GsonProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PersistentImageStorage extends PersistentObject<Image> {

  public PersistentImageStorage(@NotNull final Path path) {
    super(path);
  }

  @Override
  public void serialize(@NotNull final Collection<Image> list) throws IOException {
    super.serialize(list);
    GsonProvider.getPretty().toJson(list, Files.newBufferedWriter(this.getStorageFile()));
  }

  @Override
  public List<Image> deserialize() throws IOException {
    super.deserialize();
    return GsonProvider.getSimple()
        .fromJson(
            Files.newBufferedReader(this.getStorageFile()),
            new TypeToken<List<Image>>() {
            }.getType());
  }
}
