package arunkbabu90.lexicon.dialogs;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import arunkbabu90.lexicon.Constants;
import arunkbabu90.lexicon.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ExtractDialog extends DialogFragment implements View.OnClickListener
{
    @BindView(R.id.tv_extracted_text) TextView mExtractedTextView;
    @BindView(R.id.btn_exit) Button mExitButton;
    @BindView(R.id.btn_share) Button mShareButton;
    @BindView(R.id.btn_copy) Button mCopyButton;
    @BindView(R.id.btn_save) Button mSaveButton;

    private static String mText;
    private static int mDisplayWidth;
    private Context mContext;

    public static ExtractDialog newInstance(String bodyText, int displayWidth) {
        mText = bodyText;
        mDisplayWidth = displayWidth;

        return new ExtractDialog();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Scale Up Animation
        ObjectAnimator scaleUpAnimator = ObjectAnimator.ofPropertyValuesHolder(getDialog().getWindow().getDecorView(),
                PropertyValuesHolder.ofFloat("scaleX", 0.0f, 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 0.0f, 1.0f),
                PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f));
        scaleUpAnimator.setDuration(200);
        scaleUpAnimator.start();

        mContext = getActivity();

        getDialog().getWindow().setLayout(mDisplayWidth, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_extract, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (mText != null && mText.equals("")) {
            mSaveButton.setVisibility(View.INVISIBLE);
            mCopyButton.setVisibility(View.INVISIBLE);
            mShareButton.setVisibility(View.INVISIBLE);
            mExtractedTextView.setText(getString(R.string.err_no_text_detected));
        } else {
            mSaveButton.setVisibility(View.VISIBLE);
            mCopyButton.setVisibility(View.VISIBLE);
            mShareButton.setVisibility(View.VISIBLE);
            mExtractedTextView.setText(mText);
        }

        mSaveButton.setOnClickListener(this);
        mCopyButton.setOnClickListener(this);
        mShareButton.setOnClickListener(this);
        mExitButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_save:
                saveText();
                break;
            case R.id.btn_copy:
                copyToClipboard();
                break;
            case R.id.btn_share:
                shareText();
                break;
            case R.id.btn_exit:
                exitDialog();
                break;
        }
    }


    /**
     * Saves the text to a database
     */
    private void saveText() {
        Toast.makeText(mContext, "This feature is not yet implemented", Toast.LENGTH_SHORT).show();
    }

    /**
     * Copies the text to clipboard
     */
    private void copyToClipboard() {
        ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData plainText = ClipData.newPlainText(Constants.CLIPBOARD_TEXT_LABEL, mText);
        clipboardManager.setPrimaryClip(plainText);
        Toast.makeText(mContext, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    /**
     * Shares the text to external applications
     */
    private void shareText() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, mText);
        shareIntent.setType("text/plain");

        if (shareIntent.resolveActivity(mContext.getPackageManager()) != null) {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)));
        } else {
            Toast.makeText(mContext, R.string.err_no_apps_found, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Closes the dialog with a scale down animation
     */
    private void exitDialog() {

        // Scale Down Animation
        ObjectAnimator scaleDownAnimator = ObjectAnimator.ofPropertyValuesHolder(getDialog().getWindow().getDecorView(),
                PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.7f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.7f),
                PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f));
        scaleDownAnimator.setDuration(120);
        scaleDownAnimator.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        },120);
    }
}