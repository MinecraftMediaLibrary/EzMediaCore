package io.github.pulsebeat02.ezmediacore.pipeline.input.parser.extractor;

import java.util.ArrayList;

public class Format {
  public String format_id;
  public String format_note;
  public String ext;
  public String protocol;
  public String acodec;
  public String vcodec;
  public String url;
  public int width;
  public int height;
  public double fps;
  public int rows;
  public int columns;
  public ArrayList<Fragment> fragments;
  public String resolution;
  public double aspect_ratio;
  public int filesize_approx;
  public HttpHeaders http_headers;
  public String audio_ext;
  public String video_ext;
  public double vbr;
  public double abr;
  public double tbr;
  public String format;
  public Object format_index;
  public String manifest_url;
  public String language;
  public Object preference;
  public double quality;
  public boolean has_drm;
  public int source_preference;
  public int asr;
  public int filesize;
  public int audio_channels;
  public int language_preference;
  public String dynamic_range;
  public String container;
  public DownloaderOptions downloader_options;
}
