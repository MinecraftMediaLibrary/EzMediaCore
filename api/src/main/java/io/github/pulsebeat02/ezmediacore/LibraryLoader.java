package io.github.pulsebeat02.ezmediacore;

import java.util.concurrent.ExecutionException;

public interface LibraryLoader extends LibraryInjectable {

  void start() throws ExecutionException, InterruptedException;
}
