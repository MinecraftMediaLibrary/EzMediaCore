/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;

public final class ExecutorProvider {

  public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE;

  public static final ExecutorService LOGGER_POOL;
  public static final ExecutorService HTTP_REQUEST_POOL;
  public static final ExecutorService MAP_UPDATE_POOL;
  public static final ExecutorService EXTERNAL_PROCESS_POOL;
  public static final ExecutorService SHARED_RESULT_POOL;

  public static final ExecutorService ENCODER_HANDLER;
  public static final ExecutorService AUDIO_HANDLER;
  public static final ExecutorService FRAME_HANDLER;

  static {
    LOGGER_POOL = Executors.newCachedThreadPool();
    SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
    HTTP_REQUEST_POOL = Executors.newCachedThreadPool();
    MAP_UPDATE_POOL = Executors.newCachedThreadPool();
    EXTERNAL_PROCESS_POOL = Executors.newSingleThreadExecutor();
    SHARED_RESULT_POOL = new ForkJoinPool();
    ENCODER_HANDLER = Executors.newFixedThreadPool(8);
    AUDIO_HANDLER = Executors.newCachedThreadPool();
    FRAME_HANDLER = Executors.newCachedThreadPool();
  }

  private ExecutorProvider() {}
}
