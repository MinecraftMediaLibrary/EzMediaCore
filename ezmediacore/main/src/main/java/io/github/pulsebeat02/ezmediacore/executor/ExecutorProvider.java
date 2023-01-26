/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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

public final class ExecutorProvider {

  public static final ExecutorService SHARED_RESULT_POOL;
  public static final ExecutorService FRAME_CONSUMER;
  public static final ExecutorService LOGGER_POOL;
  public static final ExecutorService ENCODER_HANDLER;
  public static final ExecutorService RTSP_SERVER;
  public static final ExecutorService HTTP_SERVER;

  static {
    SHARED_RESULT_POOL = new ForkJoinPool();
    FRAME_CONSUMER = Executors.newCachedThreadPool();
    LOGGER_POOL = Executors.newCachedThreadPool();
    ENCODER_HANDLER = Executors.newCachedThreadPool();
    RTSP_SERVER = Executors.newCachedThreadPool();
    HTTP_SERVER = Executors.newCachedThreadPool();
  }

  private ExecutorProvider() {}
}
