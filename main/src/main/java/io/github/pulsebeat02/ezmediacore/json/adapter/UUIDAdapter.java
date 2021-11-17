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
package io.github.pulsebeat02.ezmediacore.json.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.pulsebeat02.ezmediacore.utility.manipulation.FastUUIDUtils;
import java.io.IOException;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public final class UUIDAdapter extends TypeAdapter<UUID> {

  @Override
  public void write(@NotNull final JsonWriter out, @NotNull final UUID value) throws IOException {
    out.beginObject();
    out.name("uuid").value(value.toString());
    out.endObject();
  }

  @Override
  public @NotNull UUID read(@NotNull final JsonReader in) throws IOException {
    in.beginObject();
    String id = "";
    while (in.hasNext()) {
      if (in.nextName().equals("uuid")) {
        id = in.nextString();
        break;
      }
    }
    in.close();
    return FastUUIDUtils.parseUUID(id);
  }
}
