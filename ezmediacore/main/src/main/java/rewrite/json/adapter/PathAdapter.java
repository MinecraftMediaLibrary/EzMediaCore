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
package rewrite.json.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.nio.file.Path;


public final class PathAdapter extends TypeAdapter<Path> {

  @Override
  public void write( final JsonWriter out,  final Path value) throws IOException {
    out.beginObject();
    out.name("path").value(value.toString());
    out.endObject();
  }

  @Override
  public  Path read( final JsonReader in) throws IOException {
    in.beginObject();
    final String path = this.readPath(in);
    in.endObject();
    return Path.of(path);
  }

  private String readPath( final JsonReader in) throws IOException {
    while (in.hasNext()) {
      if (this.isPathKey(in)) {
        return in.nextString();
      }
    }
    throw new AssertionError("Could not find path value of JSON configuration!");
  }

  private boolean isPathKey(final JsonReader in) throws IOException {
    final String name = in.nextName();
    return name.equals("path");
  }
}
