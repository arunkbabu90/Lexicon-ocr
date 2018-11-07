package arunkbabu90.lexicon.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class TextViewModel extends AndroidViewModel
{
    private final LiveData<List<Text>> savedTexts;

    public TextViewModel(@NonNull Application application) {
        super(application);
        savedTexts = TextDatabase.getInstance(this.getApplication()).textDao().loadAllTexts();
    }

    public LiveData<List<Text>> getSavedTexts() {
        return savedTexts;
    }
}
