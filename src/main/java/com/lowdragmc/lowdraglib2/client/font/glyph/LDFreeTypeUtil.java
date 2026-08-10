package com.lowdragmc.lowdraglib2.client.font.glyph;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;
import org.slf4j.Logger;

/**
 * 1.20.1 backport of vanilla 1.21's {@code net.minecraft.client.gui.font.providers.FreeTypeUtil}.
 * <p>
 * Minecraft only started shipping LWJGL FreeType in 1.21; on 1.20.1 the fork vendors
 * {@code org.lwjgl:lwjgl-freetype} itself (see {@code build.gradle}), so this utility has to live here.
 * Behaviour mirrors vanilla exactly: the library handle is initialized lazily under
 * {@link #LIBRARY_LOCK}, hard failures throw, soft failures are logged, and 26.6 fixed point
 * vectors convert at {@code / 64}.
 */
@OnlyIn(Dist.CLIENT)
public final class LDFreeTypeUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Object LIBRARY_LOCK = new Object();
    private static long library = 0L;

    private LDFreeTypeUtil() {
    }

    public static long getLibrary() {
        synchronized (LIBRARY_LOCK) {
            if (library == 0L) {
                try (var stack = MemoryStack.stackPush()) {
                    var pointer = stack.mallocPointer(1);
                    assertError(FreeType.FT_Init_FreeType(pointer), "Initializing FreeType library");
                    library = pointer.get();
                }
            }
            return library;
        }
    }

    public static void assertError(int error, String description) {
        if (error != 0) {
            throw new IllegalStateException("FreeType error: " + describeError(error) + " (" + description + ")");
        }
    }

    /**
     * @return true when the call failed (and the failure was logged)
     */
    public static boolean checkError(int error, String description) {
        if (error != 0) {
            LOGGER.error("FreeType error: {} ({})", describeError(error), description);
            return true;
        }
        return false;
    }

    private static String describeError(int error) {
        var message = FreeType.FT_Error_String(error);
        return message != null ? message : "Unrecognized error: 0x" + Integer.toHexString(error);
    }

    public static FT_Vector setVector(FT_Vector vector, float x, float y) {
        return vector.set(Math.round(x * 64.0F), Math.round(y * 64.0F));
    }

    public static float x(FT_Vector vector) {
        return (float) vector.x() / 64.0F;
    }

    public static void destroy() {
        synchronized (LIBRARY_LOCK) {
            if (library != 0L) {
                FreeType.FT_Done_Library(library);
                library = 0L;
            }
        }
    }
}
