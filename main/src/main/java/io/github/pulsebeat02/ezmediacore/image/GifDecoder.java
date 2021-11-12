/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.image;

import static java.lang.System.arraycopy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Stolen directly from https://github.com/DhyanB/Open-Imaging
 */
public final class GifDecoder {

  public static GifImage read(final byte[] in) throws IOException {
    final GifDecoder decoder = new GifDecoder();
    final GifImage img = new GifImage();
    GifFrame frame = null; // Currently, open frame
    int pos = readHeader(in, img); // Read header, get next byte position
    pos = readLogicalScreenDescriptor(img, in, pos);
    if (img.hasGlobColTbl) {
      img.globalColTbl = new int[img.sizeOfGlobColTbl];
      pos = readColTbl(in, img.globalColTbl, pos);
    }
    while (pos < in.length) {
      final int block = in[pos] & 0xFF;
      switch (block) {
        case 0x21: // Extension introducer
          if (pos + 1 >= in.length) {
            throw new IOException("Unexpected end of file.");
          }
          switch (in[pos + 1] & 0xFF) {
            case 0xFE -> // Comment extension
                pos = readTextExtension(in, pos);
            case 0xFF -> // Application extension
                pos = readAppExt(img, in, pos);
            case 0x01 -> { // Plain text extension
              frame = null; // End of current frame
              pos = readTextExtension(in, pos);
            }
            case 0xF9 -> { // Graphic control extension
              if (frame == null) {
                frame = new GifFrame();
                img.frames.add(frame);
              }
              pos = readGraphicControlExt(frame, in, pos);
            }
            default -> throw new IOException("Unknown extension at " + pos);
          }
          break;
        case 0x2C: // Image descriptor
          if (frame == null) {
            frame = new GifFrame();
            img.frames.add(frame);
          }
          pos = readImgDescr(frame, in, pos);
          if (frame.hasLocColTbl) {
            frame.localColTbl = new int[frame.sizeOfLocColTbl];
            pos = readColTbl(in, frame.localColTbl, pos);
          }
          pos = readImgData(frame, in, pos);
          frame = null; // End of current frame
          break;
        case 0x3B: // GIF Trailer
          return img; // Found trailer, finished reading.
        default:
          // Unknown block. The image is corrupted. Strategies: a) Skip
          // and wait for a valid block. Experience: It'll get worse. b)
          // Throw exception. c) Return gracefully if we are almost done
          // processing. The frames we have so far should be error-free.
          final double progress = 1.0 * pos / in.length;
          if (progress < 0.9) {
            throw new IOException("Unknown block at: " + pos);
          }
          pos = in.length; // Exit loop
      }
    }
    return img;
  }

  public static GifImage read(@NotNull final InputStream is) throws IOException {
    final byte[] data = new byte[is.available()];
    if (is.read(data, 0, data.length) == -1) {
      throw new IOException("Invalid data GIF input stream!");
    }
    return read(data);
  }

  static int readAppExt(@NotNull final GifImage img, final byte[] in, int i) {
    img.appId = new String(in, i + 3, 8); // should be "NETSCAPE"
    img.appAuthCode = new String(in, i + 11, 3); // should be "2.0"
    i += 14; // Go to sub-block size, it's value should be 3
    final int subBlockSize = in[i] & 0xFF;
    // The only app extension widely used is NETSCAPE, it's got 3 data bytes
    if (subBlockSize == 3) {
      // in[i+1] should have value 01, in[i+5] should be block terminator
      img.repetitions = in[i + 2] & 0xFF | in[i + 3] & 0xFF << 8; // Short
      return i + 5;
    } // Skip unknown application extensions
    while ((in[i] & 0xFF) != 0) { // While sub-block size != 0
      i += (in[i] & 0xFF) + 1; // Skip to next sub-block
    }
    return i + 1;
  }

  static int readColTbl(final byte[] in, final int @NotNull [] colors, int i) {
    final int numColors = colors.length;
    for (int c = 0; c < numColors; c++) {
      final int a = 0xFF; // Alpha 255 (opaque)
      final int r = in[i++] & 0xFF; // 1st byte is red
      final int g = in[i++] & 0xFF; // 2nd byte is green
      final int b = in[i++] & 0xFF; // 3rd byte is blue
      colors[c] = ((a << 8 | r) << 8 | g) << 8 | b;
    }
    return i;
  }

  static int readGraphicControlExt(@NotNull final GifFrame fr, final byte @NotNull [] in,
      final int i) {
    fr.disposalMethod = (in[i + 3] & 0b00011100) >>> 2; // Bits 4-2
    fr.transpColFlag = (in[i + 3] & 1) == 1; // Bit 0
    fr.delay = in[i + 4] & 0xFF | (in[i + 5] & 0xFF) << 8; // 16 bit LSB
    fr.transpColIndex = in[i + 6] & 0xFF; // Byte 6
    return i + 8; // Skipped byte 7 (blockTerminator), as it's always 0x00
  }

  static int readHeader(final byte @NotNull [] in, final GifImage img) throws IOException {
    if (in.length < 6) { // Check first 6 bytes
      throw new IOException("Image is truncated.");
    }
    img.header = new String(in, 0, 6);
    if (!img.header.equals("GIF87a") && !img.header.equals("GIF89a")) {
      throw new IOException("Invalid GIF header.");
    }
    return 6;
  }

  static int readImgData(@NotNull final GifFrame fr, final byte @NotNull [] in, int i) {
    final int fileSize = in.length;
    final int minCodeSize = in[i++] & 0xFF; // Read code size, go to block
    final int clearCode = 1 << minCodeSize; // CLEAR = 2^minCodeSize
    fr.firstCodeSize = minCodeSize + 1; // Add 1 bit for CLEAR and EOI
    fr.clearCode = clearCode;
    fr.endOfInfoCode = clearCode + 1;
    final int imgDataSize = readImgDataSize(in, i);
    final byte[] imgData = new byte[imgDataSize + 2];
    int imgDataPos = 0;
    int subBlockSize = in[i] & 0xFF;
    while (subBlockSize > 0) { // While block has data
      try { // Next line may throw exception if sub-block size is fake
        final int nextSubBlockSizePos = i + subBlockSize + 1;
        final int nextSubBlockSize = in[nextSubBlockSizePos] & 0xFF;
        arraycopy(in, i + 1, imgData, imgDataPos, subBlockSize);
        imgDataPos += subBlockSize; // Move output data position
        i = nextSubBlockSizePos; // Move to next sub-block size
        subBlockSize = nextSubBlockSize;
      } catch (final Exception e) {
        // Sub-block exceeds file end, only use remaining bytes
        subBlockSize = fileSize - i - 1; // Remaining bytes
        arraycopy(in, i + 1, imgData, imgDataPos, subBlockSize);
        i += subBlockSize + 1; // Move to next sub-block size
        break;
      }
    }
    fr.data = imgData; // Holds LZW encoded data
    i++; // Skip last sub-block size, should be 0
    return i;
  }

  @Contract(pure = true)
  static int readImgDataSize(final byte @NotNull [] in, int i) {
    final int fileSize = in.length;
    int imgDataPos = 0;
    int subBlockSize = in[i] & 0xFF;
    while (subBlockSize > 0) { // While block has data
      try { // Next line may throw exception if sub-block size is fake
        final int nextSubBlockSizePos = i + subBlockSize + 1;
        final int nextSubBlockSize = in[nextSubBlockSizePos] & 0xFF;
        imgDataPos += subBlockSize; // Move output data position
        i = nextSubBlockSizePos; // Move to next sub-block size
        subBlockSize = nextSubBlockSize;
      } catch (final Exception e) {
        // Sub-block exceeds file end, only use remaining bytes
        subBlockSize = fileSize - i - 1; // Remaining bytes
        imgDataPos += subBlockSize; // Move output data position
        break;
      }
    }
    return imgDataPos;
  }

  static int readImgDescr(@NotNull final GifFrame fr, final byte @NotNull [] in, int i) {
    fr.x = in[++i] & 0xFF | (in[++i] & 0xFF) << 8; // Byte 1-2: left
    fr.y = in[++i] & 0xFF | (in[++i] & 0xFF) << 8; // Byte 3-4: top
    fr.w = in[++i] & 0xFF | (in[++i] & 0xFF) << 8; // Byte 5-6: width
    fr.h = in[++i] & 0xFF | (in[++i] & 0xFF) << 8; // Byte 7-8: height
    fr.wh = fr.w * fr.h;
    final byte b = in[++i]; // Byte 9 is a packed byte
    fr.hasLocColTbl = (b & 0b10000000) >>> 7 == 1; // Bit 7
    fr.interlaceFlag = (b & 0b01000000) >>> 6 == 1; // Bit 6
    fr.sortFlag = (b & 0b00100000) >>> 5 == 1; // Bit 5
    final int colTblSizePower = (b & 7) + 1; // Bits 2-0
    fr.sizeOfLocColTbl = 1 << colTblSizePower; // 2^(N+1), As per the spec
    return ++i;
  }

  static int readLogicalScreenDescriptor(@NotNull final GifImage img, final byte @NotNull [] in,
      final int i) {
    img.w = in[i] & 0xFF | (in[i + 1] & 0xFF) << 8; // 16 bit, LSB 1st
    img.h = in[i + 2] & 0xFF | (in[i + 3] & 0xFF) << 8; // 16 bit
    img.wh = img.w * img.h;
    final byte b = in[i + 4]; // Byte 4 is a packed byte
    img.hasGlobColTbl = (b & 0b10000000) >>> 7 == 1; // Bit 7
    final int colResPower = ((b & 0b01110000) >>> 4) + 1; // Bits 6-4
    img.colorResolution = 1 << colResPower; // 2^(N+1), As per the spec
    img.sortFlag = (b & 0b00001000) >>> 3 == 1; // Bit 3
    final int globColTblSizePower = (b & 7) + 1; // Bits 0-2
    img.sizeOfGlobColTbl = 1 << globColTblSizePower; // 2^(N+1), see spec
    img.bgColIndex = in[i + 5] & 0xFF; // 1 Byte
    img.pxAspectRatio = in[i + 6] & 0xFF; // 1 Byte
    return i + 7;
  }

  @Contract(pure = true)
  static int readTextExtension(final byte @NotNull [] in, final int pos) {
    int i = pos + 2; // Skip extension introducer and label
    int subBlockSize = in[i++] & 0xFF;
    while (subBlockSize != 0 && i < in.length) {
      i += subBlockSize;
      subBlockSize = in[i++] & 0xFF;
    }
    return i;
  }

  static final class BitReader {

    private int bitPos; // Next bit to read
    private int numBits; // Number of bits to read
    private int bitMask; // Used to kill unwanted higher bits
    private byte[] in; // Data array

    // To avoid costly bounds checks, 'in' needs 2 more 0-bytes at the end
    private void init(final byte[] in) {
      this.in = in;
      this.bitPos = 0;
    }

    private int read() {
      // Byte indices: (bitPos / 8), (bitPos / 8) + 1, (bitPos / 8) + 2
      int i = this.bitPos >>> 3; // Byte = bit / 8
      // Bits we'll shift to the right, AND 7 is the same as MODULO 8
      final int rBits = this.bitPos & 7;
      // Byte 0 to 2, AND to get their unsigned values
      final int b0 = this.in[i++] & 0xFF, b1 = this.in[i++] & 0xFF, b2 = this.in[i] & 0xFF;
      // Glue the bytes together, don't do more shifting than necessary
      final int buf = ((b2 << 8 | b1) << 8 | b0) >>> rBits;
      this.bitPos += this.numBits;
      return buf & this.bitMask; // Kill the unwanted higher bits
    }

    private void setNumBits(final int numBits) {
      this.numBits = numBits;
      this.bitMask = (1 << numBits) - 1;
    }
  }

  static final class CodeTable {

    private final int[][] tbl; // Map codes to list of colors
    private int initTableSize; // Number of colors +2 for CLEAR + EOI
    private int initCodeSize; // Initial code size
    private int initCodeLimit; // First code limit
    private int codeSize; // Current code size, maximum is 12 bits
    private int nextCode; // Next available code for a new entry
    private int nextCodeLimit; // Increase codeSize when nextCode == limit
    private BitReader br; // Notify when code sizes increases

    public CodeTable() {
      this.tbl = new int[4096][1];
    }

    private int add(final int[] indices) {
      if (this.nextCode < 4096) {
        if (this.nextCode == this.nextCodeLimit && this.codeSize < 12) {
          this.codeSize++; // Max code size is 12
          this.br.setNumBits(this.codeSize);
          this.nextCodeLimit = (1 << this.codeSize) - 1; // 2^codeSize - 1
        }
        this.tbl[this.nextCode++] = indices;
      }
      return this.codeSize;
    }

    private int clear() {
      this.codeSize = this.initCodeSize;
      this.br.setNumBits(this.codeSize);
      this.nextCodeLimit = this.initCodeLimit;
      this.nextCode = this.initTableSize; // Don't recreate table, reset pointer
      return this.codeSize;
    }

    private void init(@NotNull final GifFrame fr, final int @NotNull [] activeColTbl,
        final BitReader br) {
      this.br = br;
      final int numColors = activeColTbl.length;
      this.initCodeSize = fr.firstCodeSize;
      this.initCodeLimit = (1 << this.initCodeSize) - 1; // 2^initCodeSize - 1
      this.initTableSize = fr.endOfInfoCode + 1;
      this.nextCode = this.initTableSize;
      for (int c = numColors - 1; c >= 0; c--) {
        this.tbl[c][0] = activeColTbl[c]; // Translated color
      } // A gap may follow with no colors assigned if numCols < CLEAR
      this.tbl[fr.clearCode] = new int[]{fr.clearCode}; // CLEAR
      this.tbl[fr.endOfInfoCode] = new int[]{fr.endOfInfoCode}; // EOI
      // Locate transparent color in code table and set to 0
      if (fr.transpColFlag && fr.transpColIndex < numColors) {
        this.tbl[fr.transpColIndex][0] = 0;
      }
    }
  }

  static final class GifFrame {

    // Graphic control extension (optional)
    // Disposal: 0=NO_ACTION, 1=NO_DISPOSAL, 2=RESTORE_BG, 3=RESTORE_PREV
    private int disposalMethod; // 0-3 as above, 4-7 undefined
    private boolean transpColFlag; // 1 Bit
    private int delay; // Unsigned, LSByte first, n * 1/100 * s
    private int transpColIndex; // 1 Byte
    // Image descriptor
    private int x; // Position on the canvas from the left
    private int y; // Position on the canvas from the top
    private int w; // May be smaller than the base image
    private int h; // May be smaller than the base image
    private int wh; // width * height
    private boolean hasLocColTbl; // Has local color table? 1 Bit
    private boolean interlaceFlag; // Is an interlace image? 1 Bit

    private boolean sortFlag; // True if local colors are sorted, 1 Bit

    private int sizeOfLocColTbl; // Size of the local color table, 3 Bits
    private int[] localColTbl; // Local color table (optional)
    // Image data
    private int firstCodeSize; // LZW minimum code size + 1 for CLEAR & EOI
    private int clearCode;
    private int endOfInfoCode;
    private byte[] data; // Holds LZW encoded data
    private BufferedImage img; // Full drawn image, not just the frame area
  }

  public static final class GifImage {

    private final List<GifFrame> frames = new ArrayList<>(64);
    private final BitReader bits = new BitReader();
    private final CodeTable codes = new CodeTable();
    public String header; // Bytes 0-5, GIF87a or GIF89a
    public boolean hasGlobColTbl; // 1 Bit
    public int colorResolution; // 3 Bits
    public boolean sortFlag; // True if global colors are sorted, 1 Bit
    public int sizeOfGlobColTbl; // 2^(val(3 Bits) + 1), see spec
    public int bgColIndex; // Background color index, 1 Byte
    public int pxAspectRatio; // Pixel aspect ratio, 1 Byte
    public int[] globalColTbl; // Global color table
    public String appId = ""; // 8 Bytes at in[i+3], usually "NETSCAPE"
    public String appAuthCode = ""; // 3 Bytes at in[i+11], usually "2.0"
    public int repetitions = 0; // 0: infinite loop, N: number of loops
    private int w; // Unsigned 16 Bit, least significant byte first
    private int h; // Unsigned 16 Bit, least significant byte first
    private int wh; // Image width * image height
    private BufferedImage img = null; // Currently drawn frame
    private Graphics2D g;

    private int @NotNull [] decode(final GifFrame fr, final int[] activeColTbl) {
      this.codes.init(fr, activeColTbl, this.bits);
      this.bits.init(fr.data); // Incoming codes
      final int clearCode = fr.clearCode, endCode = fr.endOfInfoCode;
      final int[] out = new int[this.wh]; // Target image pixel array
      final int[][] tbl = this.codes.tbl; // Code table
      int outPos = 0; // Next pixel position in the output image array
      this.codes.clear(); // Init code table
      this.bits.read(); // Skip leading clear code
      int code = this.bits.read(); // Read first code
      int[] pixels = tbl[code]; // Output pixel for first code
      arraycopy(pixels, 0, out, outPos, pixels.length);
      outPos += pixels.length;
      try {
        while (true) {
          final int prevCode = code;
          code = this.bits.read(); // Get next code in stream
          if (code == clearCode) { // After a CLEAR table, there is
            this.codes.clear(); // no previous code, we need to read
            code = this.bits.read(); // a new one
            pixels = tbl[code]; // Output pixels
            arraycopy(pixels, 0, out, outPos, pixels.length);
            outPos += pixels.length;
            continue; // Back to the loop with a valid previous code
          } else if (code == endCode) {
            break;
          }
          final int[] prevVals = tbl[prevCode];
          final int[] prevValsAndK = new int[prevVals.length + 1];
          arraycopy(prevVals, 0, prevValsAndK, 0, prevVals.length);
          if (code < this.codes.nextCode) { // Code table contains code
            pixels = tbl[code]; // Output pixels
            arraycopy(pixels, 0, out, outPos, pixels.length);
            outPos += pixels.length;
            prevValsAndK[prevVals.length] = tbl[code][0]; // K
          } else {
            prevValsAndK[prevVals.length] = prevVals[0]; // K
            arraycopy(prevValsAndK, 0, out, outPos, prevValsAndK.length);
            outPos += prevValsAndK.length;
          }
          this.codes.add(prevValsAndK); // Previous indices + K
        }
      } catch (final ArrayIndexOutOfBoundsException ignored) {
      }
      return out;
    }

    private int @NotNull [] deinterlace(final int @NotNull [] src, @NotNull final GifFrame fr) {
      final int w = fr.w, h = fr.h, wh = fr.wh;
      final int[] dest = new int[src.length];
      // Interlaced images are organized in 4 sets of pixel lines
      final int set2Y = (h + 7) >>> 3; // Line no. = ceil(h/8.0)
      final int set3Y = set2Y + ((h + 3) >>> 3); // ceil(h-4/8.0)
      final int set4Y = set3Y + ((h + 1) >>> 2); // ceil(h-2/4.0)
      // Sets' start indices in source array
      final int set2 = w * set2Y, set3 = w * set3Y, set4 = w * set4Y;
      // Line skips in destination array
      final int w2 = w << 1, w4 = w2 << 1, w8 = w4 << 1;
      // Group 1 contains every 8th line starting from 0
      int from = 0, to = 0;
      for (; from < set2; from += w, to += w8) {
        arraycopy(src, from, dest, to, w);
      } // Group 2 contains every 8th line starting from 4
      for (to = w4; from < set3; from += w, to += w8) {
        arraycopy(src, from, dest, to, w);
      } // Group 3 contains every 4th line starting from 2
      for (to = w2; from < set4; from += w, to += w4) {
        arraycopy(src, from, dest, to, w);
      } // Group 4 contains every 2nd line starting from 1 (biggest group)
      for (to = w; from < wh; from += w, to += w2) {
        arraycopy(src, from, dest, to, w);
      }
      return dest; // All pixel lines have now been rearranged
    }

    private void drawFrame(@NotNull final GifFrame fr) {
      // Determine the color table that will be active for this frame
      final int[] activeColTbl = fr.hasLocColTbl ? fr.localColTbl : this.globalColTbl;
      // Get pixels from data stream
      int[] pixels = this.decode(fr, activeColTbl);
      if (fr.interlaceFlag) {
        pixels = this.deinterlace(pixels, fr); // Rearrange pixel lines
      }
      // Create image of type 2=ARGB for frame area
      final BufferedImage frame = new BufferedImage(fr.w, fr.h, 2);
      arraycopy(pixels, 0, ((DataBufferInt) frame.getRaster().getDataBuffer()).getData(), 0, fr.wh);
      // Draw frame area on top of working image
      this.g.drawImage(frame, fr.x, fr.y, null);

      // Visualize frame boundaries during testing
      // if (DEBUG_MODE) {
      // if (prev != null) {
      // g.setColor(Color.RED); // Previous frame color
      // g.drawRect(prev.x, prev.y, prev.w - 1, prev.h - 1);
      // }
      // g.setColor(Color.GREEN); // New frame color
      // g.drawRect(fr.x, fr.y, fr.w - 1, fr.h - 1);
      // }

      // Keep one copy as "previous frame" in case we need to restore it
      // Previous frame's pixels
      final int[] prevPx = new int[this.wh];
      arraycopy(
          ((DataBufferInt) this.img.getRaster().getDataBuffer()).getData(), 0, prevPx, 0, this.wh);

      // Create another copy for the end user to not expose internal state
      fr.img = new BufferedImage(this.w, this.h, 2); // 2 = ARGB
      arraycopy(
          prevPx, 0, ((DataBufferInt) fr.img.getRaster().getDataBuffer()).getData(), 0, this.wh);

      // Handle disposal of current frame
      if (fr.disposalMethod == 2) {
        // Restore to background color (clear frame area only)
        this.g.clearRect(fr.x, fr.y, fr.w, fr.h);
      } else if (fr.disposalMethod == 3) {
        // Restore previous frame
        arraycopy(
            prevPx,
            0,
            ((DataBufferInt) this.img.getRaster().getDataBuffer()).getData(),
            0,
            this.wh);
      }
    }

    public int getBackgroundColor() {
      final GifFrame frame = this.frames.get(0);
      if (frame.hasLocColTbl) {
        return frame.localColTbl[this.bgColIndex];
      } else if (this.hasGlobColTbl) {
        return this.globalColTbl[this.bgColIndex];
      }
      return 0;
    }

    public int getDelay(final int index) {
      return this.frames.get(index).delay;
    }

    public BufferedImage getFrame(final int index) {
      if (this.img == null) { // Init
        this.img = new BufferedImage(this.w, this.h, 2); // 2 = ARGB
        this.g = this.img.createGraphics();
        this.g.setBackground(new Color(0, true)); // Transparent color
      }
      GifFrame fr = this.frames.get(index);
      if (fr.img == null) {
        // Draw all frames until and including the requested frame
        for (int i = 0; i <= index; i++) {
          fr = this.frames.get(i);
          if (fr.img == null) {
            this.drawFrame(fr);
          }
        }
      }
      return fr.img;
    }

    public int getFrameCount() {
      return this.frames.size();
    }

    public int getHeight() {
      return this.h;
    }

    public int getWidth() {
      return this.w;
    }
  }
}
