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
package io.github.pulsebeat02.ezmediacore.ffmpeg;

import java.util.Collection;
import java.util.function.Consumer;




public interface FFmpegArgumentPreparation , EnhancedExecution {

  
  FFmpegArgumentPreparation addArgument( final String arg);

  
  FFmpegArgumentPreparation addArgument( final String arg, final int index);

  
  FFmpegArgumentPreparation addArguments( final String key,  final String value);

  
  FFmpegArgumentPreparation addArguments(
       final String key,  final String value, final int index);

  
  FFmpegArgumentPreparation removeArgument( final String arg);

  
  FFmpegArgumentPreparation removeArgument(final int index);

  
  FFmpegArgumentPreparation addMultipleArguments( final String[] arguments);

  
  FFmpegArgumentPreparation addMultipleArguments( final Collection<String> arguments);

  
  FFmpegArgumentPreparation modifyProcess( final Consumer<ProcessBuilder> builder);

  void clearArguments();

  void onBeforeExecution();

  void onAfterExecution();

  boolean isCompleted();

  
  Process getProcess();

  void setProcess( final Process process);
}
