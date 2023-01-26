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
package io.github.pulsebeat02.ezmediacore.jlibdl.component;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public final class MediaHttpHeaders {

  @SerializedName("Accept")
  @Expose
  private String accept;

  @SerializedName("Accept-Charset")
  @Expose
  private String acceptCharset;

  @SerializedName("Accept-Encoding")
  @Expose
  private String acceptEncoding;

  @SerializedName("Accept-Language")
  @Expose
  private String acceptLanguage;

  @SerializedName("User-Agent")
  @Expose
  private String userAgent;

  public @Nullable String getAccept() {
    return this.accept;
  }

  public @Nullable String getAcceptCharset() {
    return this.acceptCharset;
  }

  public @Nullable String getAcceptEncoding() {
    return this.acceptEncoding;
  }

  public @Nullable String getAcceptLanguage() {
    return this.acceptLanguage;
  }

  public @Nullable String getUserAgent() {
    return this.userAgent;
  }
}
