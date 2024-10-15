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
package rewrite.resourcepack;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;


// https://gist.github.com/ItsPepperpot/c927e635fb9609cfa2a97c93327f33f2 for example

public interface PackWrapper {

  void wrap() throws IOException;

  void internalWrap() throws IOException;

  void onPackStartWrap();

  void onPackFinishWrap();

  void addFile( final String path,  final Path file) throws IOException;

  void addFile( final String path, final byte[] file);

  void removeFile( final String path);
  
  Map<String, byte[]> listFiles();

  Path getResourcepackFilePath();

  Path getIconPath();

  String getDescription();

  String getPackMcmeta();

  int getPackFormat();
}
