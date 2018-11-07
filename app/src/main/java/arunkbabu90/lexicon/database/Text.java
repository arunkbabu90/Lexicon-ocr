package arunkbabu90.lexicon.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "saved_texts")
public class Text {
    @PrimaryKey
    private int id;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "saved_text")
    private String savedText;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSavedText() {
        return savedText;
    }

    public void setSavedText(String savedText) {
        this.savedText = savedText;
    }
}
