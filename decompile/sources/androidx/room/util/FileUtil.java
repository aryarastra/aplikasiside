package androidx.room.util;

import android.os.Build;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import kotlin.jvm.internal.LongCompanionObject;

/* loaded from: classes.dex */
public class FileUtil {
    public static void copy(ReadableByteChannel input, FileChannel output) throws IOException {
        try {
            if (Build.VERSION.SDK_INT > 23) {
                output.transferFrom(input, 0L, LongCompanionObject.MAX_VALUE);
            } else {
                InputStream inputStream = Channels.newInputStream(input);
                OutputStream outputStream = Channels.newOutputStream(output);
                byte[] buffer = new byte[4096];
                while (true) {
                    int length = inputStream.read(buffer);
                    if (length <= 0) {
                        break;
                    }
                    outputStream.write(buffer, 0, length);
                }
            }
            output.force(false);
        } finally {
            input.close();
            output.close();
        }
    }

    private FileUtil() {
    }
}
