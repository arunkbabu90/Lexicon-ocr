package arunkbabu90.lexicon.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import arunkbabu90.lexicon.R;

public class ExtractActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extract);

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorDeepGrey));
    }
}
