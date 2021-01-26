package com.github.pulsebeat02.test;

import com.github.pulsebeat02.logger.Logger;
import com.github.pulsebeat02.dependency.DependencyManagement;

public class MavenDependencyTest {

    public static void main(String[] args) {
        Logger.setVerbose(true);
        new DependencyManagement().installAndLoad();
    }

}
