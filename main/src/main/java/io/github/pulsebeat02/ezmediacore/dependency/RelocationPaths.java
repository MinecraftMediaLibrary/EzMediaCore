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
package io.github.pulsebeat02.ezmediacore.dependency;

import io.github.slimjar.relocation.RelocationRule;
import org.jetbrains.annotations.NotNull;

public enum RelocationPaths {
  VLCJ("uk{}co{}caprica{}vlcj", "io{}github{}pulsebeat02{}ezmediacore{}lib{}vlcj"),
  VLCJ_NATIVES(
      "uk{}co{}caprica{}vlcj{}binding", "io{}github{}pulsebeat02{}ezmediacore{}lib{}vlcj{}binding"),

  YOUTUBE_DOWNLOADER(
      "com{}github{}kiulian{}downloader", "io{}github{}pulsebeat02{}ezmediacore{}lib{}youtube"),
  FAST_JSON("com{}alibaba{}fastjson", "io{}github{}pulsebeat02{}ezmediacore{}lib{}fastjson"),

  JAFFREE("com{}github{}kokorin", "io{}github{}pulsebeat02{}ezmediacore{}lib{}kokorin"),
  JCODEC("org{}jcodec", "io{}github{}pulsebeat02{}ezmediacore{}lib{}jcodec"),

  COMMONS_COMPRESSION(
      "org{}apache{}commons{}compress", "io{}github{}pulsebeat02{}ezmediacore{}lib{}compress"),
  COMPRESSION(
      "org{}rauschig{}jarchivelib", "io{}github{}pulsebeat02{}ezmediacore{}lib{}jarchivelib"),
  XZ("org{}tukaani.xz", "io{}github{}pulsebeat02{}ezmediacore{}lib{}xz"),

  SPOTIFY("com{}wrapper{}spotify", "io{}github{}pulsebeat02{}ezmediacore{}lib{}spotify"),

  CAFFEINE(
      "com{}github{}benmanes{}caffeine", "io{}github{}pulsebeat02{}ezmediacore{}lib{}caffeine"),

  JDA("net{}dv8tion{}jda", "io{}github{}pulsebeat02{}ezmediacore{}lib{}jda"),
  TROVE4J("gnu{}trove", "io{}github{}pulsebeat02{}ezmediacore{}lib{}trove"),
  JACKSON_DATABIND(
      "com{}fasterxml{}jackson{}databind", "io{}github{}pulsebeat02{}ezmediacore{}lib{}jackson"),

  LAVAPLAYER(
      "com{}sedmelluq{}discord{}lavaplayer",
      "io{}github{}pulsebeat02{}ezmediacore{}lib{}lavaplayer"),
  JSOUP("org{}jsoup", "io{}github{}pulsebeat02{}ezmediacore{}lib{}jsoup"),
  BASE64("net{}iharder", "io{}github{}pulsebeat02{}base64");

  private final RelocationRule relocation;

  RelocationPaths(@NotNull final String before, @NotNull final String after) {
    this.relocation =
        new RelocationRule(before.replaceAll("\\{}", "."), after.replaceAll("\\{}", "."));
  }

  public RelocationRule getRelocation() {
    return this.relocation;
  }
}
