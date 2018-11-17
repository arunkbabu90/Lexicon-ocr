package arunkbabu90.lexicon;

import java.io.Closeable;

public class LexUtils
{
    /**
     * Closes the OutputStream without throwing any Exception
     * @param closeable OutputStream to be closed
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Throwable ignored) {
        }
    }
}
