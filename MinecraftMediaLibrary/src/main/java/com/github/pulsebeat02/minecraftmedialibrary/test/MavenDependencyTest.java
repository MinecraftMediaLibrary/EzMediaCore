package com.github.pulsebeat02.minecraftmedialibrary.test;

import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.cipher.Cipher;
import com.github.kiulian.downloader.cipher.CipherFactory;
import com.github.kiulian.downloader.cipher.CipherFunction;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.DependencyManagement;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.JaveDependencyHandler;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;

public class MavenDependencyTest {

    public static void main(final String[] args) {
        Logger.setVerbose(true);
        new DependencyManagement().installAndLoad();
        new JaveDependencyHandler().installDependency();
        // to test for whether the class loaded
        new CipherFactory() {
            @Override
            public Cipher createCipher(final String s) {
                return null;
            }

            @Override
            public void addInitialFunctionPattern(final int i, final String s) {

            }

            @Override
            public void addFunctionEquivalent(final String s, final CipherFunction cipherFunction) {

            }
        };
    }

}
