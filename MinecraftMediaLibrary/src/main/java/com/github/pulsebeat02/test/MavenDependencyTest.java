package com.github.pulsebeat02.test;

import com.github.pulsebeat02.dependency.DependencyManagement;
import com.github.pulsebeat02.logger.Logger;

public class MavenDependencyTest {

    public static void main(String[] args) {
        Logger.setVerbose(true);
        new DependencyManagement().installAndLoad();
    }

}
