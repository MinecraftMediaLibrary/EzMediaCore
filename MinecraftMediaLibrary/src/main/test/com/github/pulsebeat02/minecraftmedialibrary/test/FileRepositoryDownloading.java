package com.github.pulsebeat02.minecraftmedialibrary.test;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackage;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackageDistribution;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

public class FileRepositoryDownloading {

  public static void main(final String[] args) {
    for (final LinuxPackageDistribution dict : LinuxPackageDistribution.values()) {
      for (final Set<LinuxPackage> links : dict.getLinks().values()) {
        for (final LinuxPackage pkg : links) {
          final String link = pkg.getUrl();
          final String fileName = link.substring(link.lastIndexOf("/") + 1);
          try {
            FileUtils.copyURLToFile(new URL(link), new File("linux/" + fileName));
          } catch (final IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
