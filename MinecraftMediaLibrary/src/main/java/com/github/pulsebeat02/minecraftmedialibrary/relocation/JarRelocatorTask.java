/*
 * Copyright Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.github.pulsebeat02.minecraftmedialibrary.relocation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * A task that copies {@link JarEntry jar entries} from a {@link JarFile jar input} to a
 * {@link JarOutputStream jar output}, applying the relocations defined by a
 * {@link RelocatingRemapper}.
 */
final class JarRelocatorTask {
    private final RelocatingRemapper remapper;
    private final JarOutputStream jarOut;
    private final JarFile jarIn;

    private final Set<String> resources = new HashSet<>();

    JarRelocatorTask(RelocatingRemapper remapper, JarOutputStream jarOut, JarFile jarIn) {
        this.remapper = remapper;
        this.jarOut = jarOut;
        this.jarIn = jarIn;
    }

    void processEntries() throws IOException {
        for (Enumeration<JarEntry> entries = this.jarIn.entries(); entries.hasMoreElements(); ) {
            JarEntry entry = entries.nextElement();

            // The 'INDEX.LIST' file is an optional file, containing information about the packages
            // defined in a jar. Instead of relocating the entries in it, we delete it, since it is
            // optional anyway.
            //
            // We don't process directory entries, and instead opt to recreate them when adding
            // classes/resources.
            if (entry.getName().equals("META-INF/INDEX.LIST") || entry.isDirectory()) {
                continue;
            }

            try (InputStream entryIn = this.jarIn.getInputStream(entry)) {
                processEntry(entry, entryIn);
            }
        }
    }

    private void processEntry(JarEntry entry, InputStream entryIn) throws IOException {
        String name = entry.getName();
        String mappedName = this.remapper.map(name);

        // ensure the parent directory structure exists for the entry.
        processDirectory(mappedName, true);

        if (name.endsWith(".class")) {
            processClass(name, entryIn);
        } else if (!this.resources.contains(mappedName)) {
            processResource(mappedName, entryIn, entry.getTime());
        }
    }

    private void processDirectory(String name, boolean parentsOnly) throws IOException {
        int index = name.lastIndexOf('/');
        if (index != -1) {
            String parentDirectory = name.substring(0, index);
            if (!this.resources.contains(parentDirectory)) {
                processDirectory(parentDirectory, false);
            }
        }

        if (parentsOnly) {
            return;
        }

        // directory entries must end in "/"
        JarEntry entry = new JarEntry(name + "/");
        this.jarOut.putNextEntry(entry);
        this.resources.add(name);
    }

    private void processResource(String name, InputStream entryIn, long lastModified) throws IOException {
        JarEntry jarEntry = new JarEntry(name);
        jarEntry.setTime(lastModified);

        this.jarOut.putNextEntry(jarEntry);
        copy(entryIn, this.jarOut);

        this.resources.add(name);
    }

    private void processClass(String name, InputStream entryIn) throws IOException {
        ClassReader classReader = new ClassReader(entryIn);
        ClassWriter classWriter = new ClassWriter(0);
        RelocatingClassVisitor classVisitor = new RelocatingClassVisitor(classWriter, this.remapper, name);

        try {
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        } catch (Throwable e) {
            throw new RuntimeException("Error processing class " + name, e);
        }

        byte[] renamedClass = classWriter.toByteArray();

        // Need to take the .class off for remapping evaluation
        String mappedName = this.remapper.map(name.substring(0, name.indexOf('.')));

        // Now we put it back on so the class file is written out with the right extension.
        this.jarOut.putNextEntry(new JarEntry(mappedName + ".class"));
        this.jarOut.write(renamedClass);
    }

    private static void copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[8192];
        while (true) {
            int n = from.read(buf);
            if (n == -1) {
                break;
            }
            to.write(buf, 0, n);
        }
    }
}