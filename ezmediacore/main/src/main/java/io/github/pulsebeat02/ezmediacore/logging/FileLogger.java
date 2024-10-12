package io.github.pulsebeat02.ezmediacore.logging;

import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.utility.io.FileUtils;
import io.github.pulsebeat02.ezmediacore.utility.misc.OSType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class FileLogger {

  private final ReentrantReadWriteLock lock;
  private final Path path;

  private PrintWriter writer;

  public FileLogger(final Path path) {
    this.lock = new ReentrantReadWriteLock();
    this.path = path;
  }

  public synchronized void start() throws IOException {
    this.createDirectories();
    this.createFile();
    this.setPermissions();
    this.createWriter();
  }

  public synchronized void close() {
    this.writer.flush();
    this.writer.close();
  }

  public synchronized void printLine(final String line) {
    CompletableFuture.runAsync(this.print(line), ExecutorProvider.LOGGER_POOL);
  }

  private synchronized void createWriter() throws IOException {
    this.writer = new PrintWriter(Files.newBufferedWriter(this.path), true);
  }

  private synchronized void createFile() {
    try {
      FileUtils.createFileIfNotExists(this.path);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private synchronized void createDirectories() {
    final Path parent = this.path.getParent();
    try {
      Files.createDirectories(parent);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private synchronized void setPermissions() throws IOException {
    if (OSType.isCurrentOSUnix()) {
      final Path absolute = this.path.toAbsolutePath();
      final String raw = absolute.toString();
      final String[] args = new String[]{"chmod", "-R", "777", raw};
      Runtime.getRuntime().exec(args);
    }
  }

  private synchronized Runnable print( final String line) {
    final Lock write = this.lock.writeLock();
    return () -> {
      write.lock();
      this.writer.write(line);
      this.writer.write(System.lineSeparator());
      this.writer.flush();
      write.unlock();
    };
  }
}
