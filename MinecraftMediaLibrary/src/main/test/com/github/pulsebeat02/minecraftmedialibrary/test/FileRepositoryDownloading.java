package com.github.pulsebeat02.minecraftmedialibrary.test;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackageDictionary;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

public class FileRepositoryDownloading {

  public static void main(final String[] args) {
    for (final LinuxPackageDictionary dict : LinuxPackageDictionary.values()) {
      for (final Set<String> links : dict.getLinks().values()) {
        for (final String str : links) {
          final String fileName = str.substring(str.lastIndexOf("/") + 1);
          try {
            FileUtils.copyURLToFile(new URL(str), new File("linux/" + fileName));
          } catch (final IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
