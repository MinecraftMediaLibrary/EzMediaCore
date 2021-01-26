package com.github.pulsebeat02.dependency;

import com.github.pulsebeat02.Logger;
import com.github.pulsebeat02.utility.DependencyUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DependencyManagement {

    private final String path;

    public DependencyManagement() {
        this.path = System.getProperty("user.dir");
    }

    public Set<File> install() {
        Set<File> files = new HashSet<>();
        File dir = new File(path + "/mml_libs");
        if (!dir.exists()) {
            if (dir.mkdir()) {
                Logger.info("Dependency Directory (" + dir.getAbsolutePath() + ") does not exist... Creating a folder");
            } else {
                Logger.info("Dependency Directory (" + dir.getAbsolutePath() + ") exists!");
            }
        }
        for (MavenDependency dependency : MavenDependency.values()) {
            File file = null;
            String artifact = dependency.getArtifact();
            try {
                Logger.info("Checking Maven Central Repository for " + artifact);
                file = DependencyUtilities.downloadMavenDependency(dependency, path + "/mml_libs");
            } catch (IOException e) {
                try {
                    Logger.info("Could not find in the Maven Central Repository... Checking Jitpack Central Repository for " + artifact);
                    file = DependencyUtilities.downloadJitpackDependency(dependency, path + "/mml_libs");
                } catch (IOException exception) {
                    Logger.error("Could not find " + artifact + " in the Maven Central Repository or Jitpack");
                    exception.printStackTrace();
                }
            }
            files.add(file);
        }
        return files;
    }

    public void installAndLoad() {
        install();
        for (File f : new File(path + "/mml_libs").listFiles()) {
            try {
                DependencyUtilities.loadDependency(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
