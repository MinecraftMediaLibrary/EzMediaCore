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
package rewrite.persistent;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.github.pulsebeat02.ezmediacore.image.Image;
import rewrite.json.GsonProvider;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class PersistentImageStorage extends PersistentObject<Image> {

  public PersistentImageStorage( final Path path) {
    super(path);
  }

  @Override
  public void serialize( final Collection<Image> list) throws IOException {
    super.serialize(list);
    this.serializeWriter(list);
  }

  private void serializeWriter( final Collection<Image> list) throws IOException {
    try (final BufferedWriter writer = this.createWriter()) {
      this.write(list, writer);
    }
  }

  private BufferedWriter createWriter() throws IOException {
    final Path path = this.getStorageFile();
    return Files.newBufferedWriter(path);
  }

  private void write( final Collection<Image> list,  final BufferedWriter writer) {
    final Gson gson = GsonProvider.getSimple();
    gson.toJson(list, writer);
  }

  @Override
  public  List<Image> deserialize() throws IOException {
    super.deserialize();
    return this.deserializeReader();
  }

  private List<Image> deserializeReader() throws IOException {
    try (final BufferedReader reader = this.createReader()) {
      final List<Image> images = this.read(reader);
      return images == null ? new ArrayList<>() : images;
    }
  }

  private  BufferedReader createReader() throws IOException {
    return Files.newBufferedReader(this.getStorageFile());
  }

  private List<Image> read( final BufferedReader reader) {
    final Type type = new TypeToken<List<Image>>() {}.getType();
    final Gson gson = GsonProvider.getSimple();
    return gson.fromJson(reader, type);
  }
}
