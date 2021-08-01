package io.github.pulsebeat02.ezmediacore.resourcepack;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

// https://gist.github.com/ItsPepperpot/c927e635fb9609cfa2a97c93327f33f2 for example

public interface PackWrapper {

  void wrap() throws IOException;

  void internalWrap() throws IOException;

  void onPackStartWrap();

  void onPackFinishWrap();

  void addFile(@NotNull final String path, @NotNull final Path file) throws IOException;

  void addFile(@NotNull final String path, final byte[] file);

  void removeFile(@NotNull final String path);

  @NotNull
  Map<String, byte[]> listFiles();

  @NotNull
  Path getResourcepackFilePath();

  @NotNull
  Path getIconPath();

  @NotNull
  String getDescription();

  @NotNull
  String getPackMcmeta();

  int getPackFormat();
}
