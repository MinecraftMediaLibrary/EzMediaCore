package com.github.pulsebeat02.test;

import com.github.pulsebeat02.dependency.MavenInstallationDaemon;

public class MavenDependencyTest {

    public static void main(String[] args) {
        new MavenInstallationDaemon().install();
    }

}
