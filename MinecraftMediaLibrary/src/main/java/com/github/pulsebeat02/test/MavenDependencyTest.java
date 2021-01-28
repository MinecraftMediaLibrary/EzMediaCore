package com.github.pulsebeat02.test;

import com.github.kiulian.downloader.cipher.Cipher;
import com.github.pulsebeat02.dependency.DependencyManagement;
import com.github.pulsebeat02.logger.Logger;
import uk.co.caprica.vlcj.factory.MediaApi;

public class MavenDependencyTest {

    public static void main(String[] args) {
        Logger.setVerbose(true);
        new DependencyManagement().installAndLoad();
        // example class loaded into runtime
        new Cipher() {
            @Override
            public String getSignature(String s) {
                return null;
            }
        };
    }

}
