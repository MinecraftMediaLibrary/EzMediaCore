/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.utility.search;

/*
 * CharIntMap.java
 *
 * Created on 13.11.2003.
 *
 * StringSearch - high-performance pattern matching algorithms in Java
 * Copyright (c) 2003-2015 Johann Burkard (<http://johannburkard.de>)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

/**
 * The CharIntMap is a collection to save <code>char</code> to <code>int</code> mappings in. The
 * CharIntMap is destined to provide fast access to skip tables while being both Unicode-safe and
 * more RAM-effective than a naive
 * <code>int</code> array.
 * <p>
 * The CharIntMap is initialized by specifying the extent between the lowest and the highest
 * occuring character. Only an array of size <code>highest - lowest + 1</code> is constructed.
 * <p>
 * CharIntMap are created automatically in the pre-processing methods of each StringSearch
 * instance.
 *
 * @author <a href="http://johannburkard.de">Johann Burkard</a>
 * @version $Id: CharIntMap.java 6675 2015-01-17 21:02:35Z johann $
 * @see <a href="http://johannburkard.de/software/stringsearch/">StringSearch
 * &#8211; high-performance pattern matching algorithms in Java</a>
 */
public class CharIntMap implements Externalizable {

  static final long serialVersionUID = 1351686633123489568L;

  private int[] array;

  private char lowest;

  private int defaultValue;

  /**
   * Constructor for CharIntMap. Required for Serialization.
   */
  public CharIntMap() {
    super();
  }

  /**
   * Constructor for CharIntMap.
   *
   * @param extent       the extent of the text
   * @param lowest       the lowest occuring character
   * @param defaultValue a default value to initialize the underlying
   *                     <code>int</code> array with
   */
  public CharIntMap(final int extent, final char lowest, final int defaultValue) {
    this.array = new int[extent];
    this.lowest = lowest;
    this.defaultValue = defaultValue;
    if (defaultValue != 0) {
      Arrays.fill(this.array, defaultValue);
    }
  }

  /**
   * Returns the stored value for the given <code>char</code>.
   *
   * @param c the <code>char</code>
   * @return the stored value
   */
  public final int get(final char c) {
    final char x = (char) (c - this.lowest);
    if (x >= this.array.length) {
      return this.defaultValue;
    }
    return this.array[x];
  }

  /**
   * Sets the stored value for the given <code>char</code>.
   *
   * @param c   the <code>char</code>
   * @param val the new value
   */
  public final void set(final char c, final int val) {
    final char x = (char) (c - this.lowest);
    if (x >= this.array.length) {
      return;
    }
    this.array[x] = val;
  }

  /**
   * Returns the extent of the actual <code>char</code> array.
   *
   * @return the extent
   */
  public final int getExtent() {
    return this.array.length;
  }

  /**
   * Returns the lowest char that mappings can be saved for.
   *
   * @return a <code>char</code>
   */
  public final char getLowest() {
    return this.lowest;
  }

  /**
   * Returns the highest char that mappings can be saved for.
   *
   * @return char
   */
  public final char getHighest() {
    return (char) (this.lowest + this.array.length);
  }

  /**
   * Returns if this Object is equal to another Object.
   *
   * @param obj the other Object
   * @return if this Object is equal
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof final CharIntMap m)) {
      return false;
    }
    if (this.lowest != m.lowest) {
      return false;
    }
    if (this.defaultValue != m.defaultValue) {
      return false;
    }
    if (this.array == null && m.array == null) {
      return true;
    }
    return Arrays.equals(this.array, m.array);
  }

  /**
   * Returns the hashCode of this Object.
   *
   * @return the hashCode
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int out = this.getClass().getName().hashCode();
    out ^= this.lowest;
    out ^= this.defaultValue;
    if (this.array != null) {
      for (final int j : this.array) {
        out ^= j;
      }
    }
    return out;
  }

  /**
   * Returns a String representation of this Object.
   *
   * @return a String, never <code>null</code>
   * @see java.lang.Object#toString()
   * @see #toStringBuffer(StringBuffer)
   */
  @Override
  public @NotNull
  final String toString() {
    return this.toStringBuffer(null).toString();
  }

  /**
   * Appends a String representation of this Object to the given {@link StringBuffer} or creates a
   * new one if none is given. This method is not <code>final</code> because subclasses might want a
   * different String format.
   *
   * @param in the StringBuffer to append to, may be <code>null</code>
   * @return a StringBuffer, never <code>null</code>
   */
  public StringBuffer toStringBuffer(final StringBuffer in) {
    StringBuffer out = in;
    if (out == null) {
      out = new StringBuffer(128);
    } else {
      out.ensureCapacity(out.length() + 128);
    }
    out.append("{ CharIntMap: lowest = ");
    out.append(this.lowest);
    out.append(", defaultValue = ");
    out.append(this.defaultValue);
    if (this.array != null) {
      out.append(", array = ");
      for (int i = 0; i < this.array.length; i++) {
        if (this.array[i] != 0) {
          out.append(i);
          out.append(": ");
          out.append(this.array[i]);
          out.append(' ');
        }
      }
    }
    out.append('}');
    return out;
  }

  /**
   * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
   */
  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    if (this.array == null) {
      out.writeInt(0);
    } else {
      out.writeInt(this.array.length);
      for (final int j : this.array) {
        out.writeInt(j);
      }
    }
    out.writeChar(this.lowest);
    out.writeInt(this.defaultValue);
  }

  /**
   * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
   */
  @Override
  public void readExternal(@NotNull final ObjectInput in) throws IOException {
    final int l = in.readInt();
    if (l > 0) {
      this.array = new int[l];
      for (int i = 0; i < this.array.length; i++) {
        this.array[i] = in.readInt();
      }
    }
    this.lowest = in.readChar();
    this.defaultValue = in.readInt();
  }

}