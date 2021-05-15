#include <iostream>
#include <jni.h>

static jint *color_map;
static jint *full_color_map;

JNIEXPORT void JNICALL Java_com_github_pulsebeat02_minecraftmedialibrary_natives_NativeFilterLiteDither_setup(JNIEnv *env, jobject current, jintArray color, jintArray full) {
    color_map = env -> GetIntArrayElements(color, (jboolean*)true);
    full_color_map = env -> GetIntArrayElements(full, (jboolean*)true);
}

char get_best_color(const int rgb) {
    return color_map[
            (rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
}

int getBestFullColor(const int red, const int green, const int blue) {
    return full_color_map[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
}

JNIEXPORT void JNICALL Java_com_github_pulsebeat02_minecraftmedialibrary_natives_NativeFilterLiteDither_dither_native(JNIEnv *env, jobject current, jobject buffer, jintArray arr, const jint width) {
    jint *array = env -> GetIntArrayElements(arr, (jboolean*)true);
    jsize *height = (jsize*)(size_t)env -> GetArrayLength(arr);
    jbyte *data = (jbyte*)(env -> GetDirectBufferAddress(buffer));
    const int widthMinus = width - 1;
    const int heightMinus = *height - 1;

    /*

    const int span = width << 2;

    Width is never going to be greater than 1024.
    This means that 1024 << 2 is equivalent to
    1024 * 2 * 2 or 4096. We can use this as
    the max size we can use.

    */

    int** dither_buffer = new int*[2];
    for (int i = 0; i < 2; ++i) {
        dither_buffer[i] = new int[4096];
    }

    for (int y = 0; y < *height; ++y) {
        const bool hasNextY = y < heightMinus;
        const int yIndex = y * width;
        if (!(y & 0x1)) {
            int bufferIndex = 0;
            int* buf1 = dither_buffer[0];
            int* buf2 = dither_buffer[1];
            for (int x = 0; x < width; ++x) {
                const int index = yIndex + x;
                const int rgb = array[index];
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
                data[index] = get_best_color(closest);
            }
        } else {
            int bufferIndex = width + (width << 1) - 1;
            int* buf1 = dither_buffer[1];
            int* buf2 = dither_buffer[0];
            for (int x = width - 1; x >= 0; --x) {
                const int index = yIndex + x;
                const int rgb = array[index];
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
                data[index] = get_best_color(closest);
            }
        }
    }
    for (int i = 0; i < 2; ++i) {
        delete[] dither_buffer[i];
    }
    delete[] dither_buffer;
}
