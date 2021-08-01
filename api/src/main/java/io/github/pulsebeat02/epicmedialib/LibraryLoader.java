package io.github.pulsebeat02.epicmedialib;

import java.util.concurrent.ExecutionException;

public interface LibraryLoader extends LibraryInjectable {

  void start() throws ExecutionException, InterruptedException;
}
