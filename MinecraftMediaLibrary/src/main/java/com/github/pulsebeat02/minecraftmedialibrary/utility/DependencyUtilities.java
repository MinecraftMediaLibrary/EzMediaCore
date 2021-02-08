package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.MavenDependency;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DependencyUtilities {

    public static File downloadMavenDependency(@NotNull final MavenDependency dependency, @NotNull final String parent) throws IOException {
        return downloadFile(dependency, getMavenCentralUrl(dependency), parent);
    }

    public static File downloadJitpackDependency(@NotNull final MavenDependency dependency, @NotNull final String parent) throws IOException {
        return downloadFile(dependency, getJitpackUrl(dependency), parent);
    }

    public static String getMavenCentralUrl(@NotNull final MavenDependency dependency) {
        return getDependencyUrl(dependency, "https://repo1.maven.org/maven2/");
    }

    public static String getJitpackUrl(@NotNull final MavenDependency dependency) {
        return getDependencyUrl(dependency, "https://jitpack.io/");
    }

    public static String getDependencyUrl(@NotNull final MavenDependency dependency, @NotNull final String base) {
        return base +
                dependency.getGroup().replaceAll("\\.", "/") + "/" +
                dependency.getArtifact() + "/" +
                dependency.getVersion() + "/";
    }

    public static String getDependencyUrl(@NotNull final String groupId, @NotNull final String artifactId, @NotNull final String version, @NotNull final String base) {
        return base +
                groupId.replaceAll("\\.", "/") + "/" +
                artifactId + "/" +
                version + "/";
    }

    public static File downloadFile(@NotNull final MavenDependency dependency, @NotNull final String link, @NotNull final String parent) throws IOException {
        final String file = dependency.getArtifact() + "-" + dependency.getVersion() + ".jar";
        final String url = link + file;
        return downloadFile(Paths.get(parent + "/" + file), url);
    }

    public static File downloadFile(@NotNull final String groupId, @NotNull final String artifactId, @NotNull final String version, @NotNull final String parent) throws IOException {
        final String file = artifactId + "-" + version + ".jar";
        final String url = getDependencyUrl(groupId, artifactId, version, "https://repo1.maven.org/maven2/") + file;
        return downloadFile(Paths.get(parent + "/" + file), url);
    }

    public static File downloadFile(@NotNull final Path p, @NotNull final String url) throws IOException {
        Logger.info("Downloading Dependency at " + url + " into folder " + p);
        final BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
        final FileOutputStream fileOutputStream = new FileOutputStream(String.valueOf(p));
        final byte[] dataBuffer = new byte[256000];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, bytesRead);
        }
        return new File(p.toString());
    }

    public static void loadDependency(@NotNull final File file) throws IOException {
        final String jarPath = file.getAbsolutePath();
        final JarFile jarFile = new JarFile(jarPath);
        Logger.info("Loading JAR Dependency at: " + jarPath);
        final Enumeration<JarEntry> e = jarFile.entries();
        final URL[] urls = {new URL("jar:file:" + jarPath + "!/")};
        final URLClassLoader cl = URLClassLoader.newInstance(urls);
        while (e.hasMoreElements()) {
            final JarEntry je = e.nextElement();
            if (je.isDirectory() || !je.getName().endsWith(".class")) {
                continue;
            }
            String className = je.getName().substring(0, je.getName().length() - 6);
            className = className.replace('/', '.');
            try {
                Logger.info("Loaded " + className);
                final Class<?> c = cl.loadClass(className);
            } catch (final ClassNotFoundException | NoClassDefFoundError ignored) {
                Logger.error("Could NOT Load " + className);
                Logger.info("If the class which couldn't be loaded is in in a META-INF folder or an OS" +
                        "specific class, it is completely fine to leave this error alone.");
            }
        }
        Logger.info("Finished Loading Dependency " + file.getName());
    }

}
