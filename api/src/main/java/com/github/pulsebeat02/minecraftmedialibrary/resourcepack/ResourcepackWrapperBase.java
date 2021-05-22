/*............................................................................................
 . Copyright © 2021 Brandon Li                                                               .
 .                                                                                           .
 . Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
 . software and associated documentation files (the “Software”), to deal in the Software     .
 . without restriction, including without limitation the rights to use, copy, modify, merge, .
 . publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
 . persons to whom the Software is furnished to do so, subject to the following conditions:  .
 .                                                                                           .
 . The above copyright notice and this permission notice shall be included in all copies     .
 . or substantial portions of the Software.                                                  .
 .                                                                                           .
 . THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
 .  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
 .   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
 .   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
 .   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
 .   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
 .   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
 .   SOFTWARE.                                                                               .
 ............................................................................................*/

package com.github.pulsebeat02.minecraftmedialibrary.resourcepack;

import com.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public interface ResourcepackWrapperBase extends PackHolder, ConfigurationSerializable {

    /**
     * Checks if the two ResourcepackWrapper objects are equal.
     *
     * @param obj the other object
     * @return whether the two objects are equal or not
     */
    @Override
    boolean equals(Object obj);

    /**
     * Returns a String version of the current instance.
     *
     * @return the stringified version of the instance
     */
    @Override
    String toString();

    /**
     * Gets sound name.
     *
     * @return the sound name
     */
    String getSoundName();

    /**
     * Gets path.
     *
     * @return the path
     */
    String getPath();

    /**
     * Gets audio.
     *
     * @return the audio
     */
    Path getAudio();

    /**
     * Gets icon.
     *
     * @return the icon
     */
    Path getIcon();

    /**
     * Gets description.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Gets pack format.
     *
     * @return the pack format
     */
    int getPackFormat();
}
