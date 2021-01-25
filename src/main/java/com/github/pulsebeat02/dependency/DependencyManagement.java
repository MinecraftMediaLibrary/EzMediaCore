package com.github.pulsebeat02.dependency;

import com.github.pulsebeat02.utility.DependencyUtilities;

import java.io.File;
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
            if (!dir.mkdir()) {
                System.out.println("Making Directory");
            }
        }
        for (MavenDependency dependency : MavenDependency.values()) {
            File file = DependencyUtilities.downloadMavenDependency(dependency, path + "/mml_libs");
            if (file == null) {
                file = DependencyUtilities.downloadJitpackDependency(dependency, path + "/mml_libs");
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
