package com.github.pulsebeat02.dependency;

import com.github.pulsebeat02.MinecraftMediaLibrary;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class DependencyClassLoader {

    private final MinecraftMediaLibrary library;

    public DependencyClassLoader(@NotNull final MinecraftMediaLibrary library) {
        this.library = library;
    }

    public void loadDependencyAtRuntime(@NotNull final File[] files) {
        URL[] urls = new URL[files.length];
        for (int i = 0; i < urls.length; i++) {
            try {
                urls[i] = files[i].toURI().toURL();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            URLClassLoader childClassLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
            Thread.currentThread().setContextClassLoader(childClassLoader);
            Class.forName("org.kostenko.examples.core.classloader.ClassLoaderTest", true , childClassLoader);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
