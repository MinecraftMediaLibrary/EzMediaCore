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



public final class Format {

  @SerializedName("acodec")
  @Expose
  private String acodec;

  @SerializedName("ext")
  @Expose
  private String ext;

  @SerializedName("format")
  @Expose
  private String format;

  @SerializedName("format_id")
  @Expose
  private String formatId;

  @SerializedName("fps")
  @Expose
  private double fps;

  @SerializedName("http_headers")
  @Expose
  private FormatHttpHeaders httpHeaders;

  @SerializedName("manifest_url")
  @Expose
  private String manifestUrl;

  @SerializedName("preference")
  @Expose
  private Object preference;

  @SerializedName("protocol")
  @Expose
  private String protocol;

  @SerializedName("tbr")
  @Expose
  private double tbr;

  @SerializedName("url")
  @Expose
  private String url;

  @SerializedName("vcodec")
  @Expose
  private String vcodec;

  @SerializedName("height")
  @Expose
  private int height;

  @SerializedName("width")
  @Expose
  private int width;

  public  String getAcodec() {
    return this.acodec;
  }

  public  String getExt() {
    return this.ext;
  }

  public  String getFormat() {
    return this.format;
  }

  public  String getFormatId() {
    return this.formatId;
  }

  public double getFps() {
    return this.fps;
  }

  public  FormatHttpHeaders getHttpHeaders() {
    return this.httpHeaders;
  }

  public  String getManifestUrl() {
    return this.manifestUrl;
  }

  public  Object getPreference() {
    return this.preference;
  }

  public  String getProtocol() {
    return this.protocol;
  }

  public double getTbr() {
    return this.tbr;
  }

  public  String getUrl() {
    return this.url;
  }

  public  String getVcodec() {
    return this.vcodec;
  }

  public int getHeight() {
    return this.height;
  }

  public int getWidth() {
    return this.width;
  }
}
