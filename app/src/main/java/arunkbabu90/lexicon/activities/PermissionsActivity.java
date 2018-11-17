package arunkbabu90.lexicon.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import arunkbabu90.lexicon.Constants;
import arunkbabu90.lexicon.R;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;

@RequiresApi(api = Build.VERSION_CODES.M)
public class PermissionsActivity extends AppCompatActivity implements View.OnClickListener
{
    @BindView(R.id.btn_skip) Button mSkipButton;
    @BindView(R.id.btn_overlay_permission) Button mOverlayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        ButterKnife.bind(this);

        mSkipButton.setOnClickListener(this);
        mOverlayButton.setOnClickListener(this);

        // Set the navigation bar color
        // Set the status bar color [API 21+ Lollipop]
        getWindow().addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // Checking whether the Permissions are already available or granted
        if (Settings.canDrawOverlays(this)) {
            applyPermissionState();
        }
    }

    /**
     * Change the view states to indicate that the permission is granted
     */
    private void applyPermissionState() {
        mOverlayButton.setClickable(false);
        mOverlayButton.setEnabled(false);
        mOverlayButton.setTextColor(getColor(R.color.colorGreyedText));
        mSkipButton.setText(getString(R.string.close));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_skip:
                finish();
                break;
            case R.id.btn_overlay_permission:
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(permissionIntent, Constants.REQUEST_CODE_DRAWOVER_PERMISSION);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Constants.REQUEST_CODE_DRAWOVER_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                applyPermissionState();
            } else {
                Toast.makeText(this, getString(R.string.overlay_permission_required), Toast.LENGTH_LONG).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
