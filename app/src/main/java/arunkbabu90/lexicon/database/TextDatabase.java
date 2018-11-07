package arunkbabu90.lexicon.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Text.class}, version = 1, exportSchema = false)
public abstract class TextDatabase extends RoomDatabase
{
    private static TextDatabase sInstance;
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "SavedTextDB";

    public static TextDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        TextDatabase.class, DATABASE_NAME)
                        .build();
            }
        }
        return sInstance;
    }

    public abstract TextDao textDao();
}
