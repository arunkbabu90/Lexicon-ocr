package arunkbabu90.lexicon.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import arunkbabu90.lexicon.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PermissionsActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.btn_close) Button mCloseButton;
    @BindView(R.id.btn_skip) Button mSkipButton;
    @BindView(R.id.btn_overlay_permission) Button mOverlayPermissionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        ButterKnife.bind(this);

        mCloseButton.setOnClickListener(this);
        mSkipButton.setOnClickListener(this);
        mOverlayPermissionButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.btn_close:
                break;
            case R.id.btn_skip:
                break;
            case R.id.btn_overlay_permission:
                break;
        }
    }
}
