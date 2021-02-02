package com.github.pulsebeat02.minecraftmedialibrary.test;

import com.github.kiulian.downloader.cipher.Cipher;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.DependencyManagement;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.JaveDependencyHandler;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;

public class MavenDependencyTest {

    public static void main(final String[] args) {
        Logger.setVerbose(true);
        new DependencyManagement().installAndLoad();
        new JaveDependencyHandler().installDependency();
        // example class loaded into runtime
        new Cipher() {
            @Override
            public String getSignature(final String s) {
                return null;
            }
        };
    }

}
