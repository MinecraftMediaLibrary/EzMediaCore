#include <iostream>

static jint *color_map;

JNIEXPORT void JNICALL setup(JNIEnv *env, jobject current, jintArray arr) {
    color_map = (*env)->GetIntArrayElements(env, arr, NULL);
}

JNIEXPORT void JNICALL dither_native(JNIEnv *env, jobject current, jobject buffer, jintArray arr, jint width) {
    jint *array = (*env)->GetIntArrayElements(env, arr, NULL);
    jsize height = (*env)->GetArrayLength(env, jArr);
    const int widthMinus = width - 1;
    const int heightMinus = height - 1;
    const int dither_buffer[2][(width + width) << 2];
    jbyte *data = (*env)->GetDirectBufferAddress(env, buffer);
    for (int y = 0; y < height; ++y) {
        const bool hasNextY = y < heightMinus;
        const int yIndex = y * width;
        if (!(y & 0x1)) {
            int bufferIndex = 0;
            int buf1[] = dither_buffer[0];
            int buf2[] = dither_buffer[1];
            for (int x = 0; x < width; ++x) {
                const int index = yIndex + x;
                const int rgb = buffer[index];
                int red = rgb >> 16 & 0xFF;
                int green = rgb >> 8 & 0xFF;
                int blue = rgb & 0xFF;
                red = (red += buf1[bufferIndex++]) > 255 ? 255 : red < 0 ? 0 : red;
                green = (green += buf1[bufferIndex++]) > 255 ? 255 : green < 0 ? 0 : green;
                blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : blue < 0 ? 0 : blue;
                const int closest = getBestFullColor(red, green, blue);
                const int delta_r = red - (closest >> 16 & 0xFF);
                const int delta_g = green - (closest >> 8 & 0xFF);
                const int delta_b = blue - (closest & 0xFF);
                if (x < widthMinus) {
                    buf1[bufferIndex] = delta_r >> 1;
                    buf1[bufferIndex + 1] = delta_g >> 1;
                    buf1[bufferIndex + 2] = delta_b >> 1;
                }
                if (hasNextY) {
                    if (x > 0) {
                        buf2[bufferIndex - 6] = delta_r >> 2;
                        buf2[bufferIndex - 5] = delta_g >> 2;
                        buf2[bufferIndex - 4] = delta_b >> 2;
                    }
                    buf2[bufferIndex - 3] = delta_r >> 2;
                    buf2[bufferIndex - 2] = delta_g >> 2;
                    buf2[bufferIndex - 1] = delta_b >> 2;
                }
                data.put(index, getBestColor(closest));
            }
        } else {
            int bufferIndex = width + (width << 1) - 1;
            int buf1[] = dither_buffer[1];
            int buf2[] = dither_buffer[0];
            for (int x = width - 1; x >= 0; --x) {
                const int index = yIndex + x;
                const int rgb = buffer[index];
                int red = rgb >> 16 & 0xFF;
                int green = rgb >> 8 & 0xFF;
                int blue = rgb & 0xFF;
                blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : blue < 0 ? 0 : blue;
                green = (green += buf1[bufferIndex--]) > 255 ? 255 : green < 0 ? 0 : green;
                red = (red += buf1[bufferIndex--]) > 255 ? 255 : red < 0 ? 0 : red;
                const int closest = getBestFullColor(red, green, blue);
                const int delta_r = red - (closest >> 16 & 0xFF);
                const int delta_g = green - (closest >> 8 & 0xFF);
                const int delta_b = blue - (closest & 0xFF);
                if (x > 0) {
                    buf1[bufferIndex] = delta_b >> 1;
                    buf1[bufferIndex - 1] = delta_g >> 1;
                    buf1[bufferIndex - 2] = delta_r >> 1;
                }
                if (hasNextY) {
                    if (x < widthMinus) {
                        buf2[bufferIndex + 6] = delta_b >> 2;
                        buf2[bufferIndex + 5] = delta_g >> 2;
                        buf2[bufferIndex + 4] = delta_r >> 2;
                    }
                    buf2[bufferIndex + 3] = delta_b >> 2;
                    buf2[bufferIndex + 2] = delta_g >> 2;
                    buf2[bufferIndex + 1] = delta_r >> 2;
                }
                data.put(index, getBestColor(closest));
            }
        }
    }
}

char getBestColor(const int rgb) {
    return color_map[
            (rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
}
