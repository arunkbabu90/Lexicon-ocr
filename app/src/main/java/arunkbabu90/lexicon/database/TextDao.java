package arunkbabu90.lexicon.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface TextDao {
    @Query("SELECT * FROM saved_texts ORDER BY timestamp DESC")
    LiveData<List<Text>> loadAllTexts();

    @Query("SELECT * FROM saved_texts ORDER BY timestamp DESC")
    List<Text> loadTexts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertText(Text text);

    @Delete
    void deleteText(Text text);
}
