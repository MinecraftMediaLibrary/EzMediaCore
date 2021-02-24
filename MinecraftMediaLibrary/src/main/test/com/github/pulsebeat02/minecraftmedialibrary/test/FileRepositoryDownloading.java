package com.github.pulsebeat02.minecraftmedialibrary.test;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackageDictionary;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

public class FileRepositoryDownloading {

    public static void main(String[] args) {
        for (LinuxPackageDictionary dict : LinuxPackageDictionary.values()) {
            for (Set<String> links : dict.getLinks().values()) {
                for (String str : links) {
                    String fileName = str.substring(str.lastIndexOf("/") + 1);
                    try {
                        FileUtils.copyURLToFile(new URL(str), new File("linux/" + fileName));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
