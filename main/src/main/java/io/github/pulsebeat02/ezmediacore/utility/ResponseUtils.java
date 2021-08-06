package io.github.pulsebeat02.ezmediacore.utility;

import com.github.kiulian.downloader.downloader.response.Response;
import io.github.pulsebeat02.ezmediacore.Logger;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public final class ResponseUtils {

  private ResponseUtils() {
  }

  public static <T> Optional<T> getResponseResult(@NotNull final Response<T> response) {
    switch (response.status()) {
      case error, canceled -> {
        Logger.info(response.error().getMessage());
        return Optional.empty();
      }
      default -> {
        return Optional.of(response.data());
      }
    }
  }

}
