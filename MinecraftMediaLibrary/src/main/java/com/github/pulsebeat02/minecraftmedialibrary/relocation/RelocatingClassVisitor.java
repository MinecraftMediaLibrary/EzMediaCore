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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

/**
 * A {@link ClassVisitor} that relocates types and names with a {@link RelocatingRemapper}.
 */
final class RelocatingClassVisitor extends ClassRemapper {
    private final String packageName;

    RelocatingClassVisitor(ClassWriter writer, RelocatingRemapper remapper, String name) {
        super(writer, remapper);
        this.packageName = name.substring(0, name.lastIndexOf('/') + 1);
    }

    @Override
    public void visitSource(String source, String debug) {
        if (source == null) {
            super.visitSource(null, debug);
            return;
        }

        // visit source file name
        String name = this.packageName + source;
        String mappedName = super.remapper.map(name);
        String mappedFileName = mappedName.substring(mappedName.lastIndexOf('/') + 1);
        super.visitSource(mappedFileName, debug);
    }
}