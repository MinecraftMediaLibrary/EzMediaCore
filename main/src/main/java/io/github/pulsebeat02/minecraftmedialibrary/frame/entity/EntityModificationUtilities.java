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

package io.github.pulsebeat02.minecraftmedialibrary.frame.entity;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * An class containing utilities for the default built-in entities which can be used to play videos
 * in Minecraft.
 */
public class EntityModificationUtilities {

  protected static Entity getModifiedAreaEffectCloud(
      @NotNull final AreaEffectCloud cloud, @NotNull final String name, final int height) {
    cloud.setInvulnerable(true);
    cloud.setDuration(999999);
    cloud.setDurationOnUse(0);
    cloud.setRadiusOnUse(0);
    cloud.setRadius(0);
    cloud.setRadiusPerTick(0);
    cloud.setReapplicationDelay(0);
    cloud.setCustomNameVisible(true);
    cloud.setCustomName(StringUtils.repeat(name, height));
    cloud.setGravity(false);
    return cloud;
  }

  protected static Entity getModifiedArmorStand(
      @NotNull final ArmorStand armorstand, @NotNull final String name, final int height) {
    armorstand.setInvulnerable(true);
    armorstand.setVisible(false);
    armorstand.setCustomNameVisible(true);
    armorstand.setGravity(false);
    armorstand.setCustomName(StringUtils.repeat(name, height));
    return armorstand;
  }
}
