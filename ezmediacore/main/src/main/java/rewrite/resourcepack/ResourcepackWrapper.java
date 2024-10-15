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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gson.JsonObject;
import rewrite.json.GsonProvider;
import io.github.pulsebeat02.ezmediacore.throwable.IllegalPackFormatException;
import io.github.pulsebeat02.ezmediacore.throwable.IllegalPackResourceException;
import rewrite.util.io.FileUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ResourcepackWrapper implements PackWrapper {

  private final Map<String, byte[]> files;
  private final Path path;
  private final String description;
  private final int format;
  private final Path icon;

  ResourcepackWrapper(
       final Path path,
       final String description,
      final int format,
       final Path icon) {
    checkNotNull(path, "Path cannot be null!");
    checkNotNull(description, "Description cannot be null!");
    this.files = new HashMap<>();
    this.path = path;
    this.description = description;
    this.format = format;
    this.icon = icon;
    this.validatePack();
  }

  public static  ResourcepackWrapper ofPack(
       final Path path,  final String description, final int format) {
    return ofPack(path, description, format, null);
  }

  public static  ResourcepackWrapper ofPack(
       final Path path,
       final String description,
       final int format,
       final Path icon) {
    return new ResourcepackWrapper(path, description, format, icon);
  }

  private boolean checkIcon() {
    return this.icon != null && !ResourcepackUtils.validateResourcepackIcon(this.icon);
  }

  private boolean validPackExtension() {
    return ResourcepackUtils.validatePackFormat(this.format);
  }

  @Override
  public void wrap() throws IOException {
    this.onPackStartWrap();
    this.internalWrap();
    this.onPackFinishWrap();
  }

  @Override
  public void internalWrap() throws IOException {
    try (final ZipOutputStream out =
        new ZipOutputStream(new FileOutputStream(this.path.toFile()))) {
      FileUtils.createFileIfNotExists(this.path);
      this.addFiles();
      this.writeFiles(out);
    }
  }

  private void addFiles() throws IOException {
    this.addFile("pack.mcmeta", this.getPackMcmeta().getBytes());
    if (this.icon != null) {
      this.addFile("pack.png", this.icon);
    }
  }

  private void writeFiles( final ZipOutputStream out) throws IOException {
    for (final Map.Entry<String, byte[]> entry : this.files.entrySet()) {
      out.putNextEntry(new ZipEntry(entry.getKey()));
      out.write(entry.getValue());
      out.closeEntry();
    }
  }

  @Override
  public void onPackStartWrap() {}

  @Override
  public void onPackFinishWrap() {}

  @Override
  public void addFile( final String path,  final Path file) throws IOException {
    this.files.put(path, Files.readAllBytes(file));
  }

  @Override
  public void addFile( final String path, final byte[] file) {
    this.files.put(path, file);
  }

  @Override
  public void removeFile( final String path) {
    this.files.remove(path);
  }

  @Override
  public  Map<String, byte[]> listFiles() {
    return this.files;
  }

  @Override
  public  Path getResourcepackFilePath() {
    return this.path;
  }

  @Override
  public  Path getIconPath() {
    return this.icon;
  }

  @Override
  public  String getDescription() {
    return this.description;
  }

  @Override
  public  String getPackMcmeta() {

    final JsonObject mcmeta = new JsonObject();
    final JsonObject pack = new JsonObject();

    pack.addProperty("pack_format", this.format);
    pack.addProperty("description", this.description);
    mcmeta.add("pack", pack);

    return GsonProvider.getSimple().toJson(mcmeta);
  }

  @Override
  public int getPackFormat() {
    return this.format;
  }
}
