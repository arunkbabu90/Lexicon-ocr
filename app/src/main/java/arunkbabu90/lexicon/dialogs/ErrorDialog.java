package arunkbabu90.lexicon.dialogs;

import android.os.Bundle;
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

import arunkbabu90.lexicon.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This dialog can used for showing errors
 */
public class ErrorDialog extends DialogFragment
{
    @BindView(R.id.tv_error) TextView mErrorTextView;
    @BindView(R.id.btn_close_error) Button mCloseButton;

    private static int mDialogWidth;
    private static String mErrorText;

    public static ErrorDialog newInstance(String errorText, int displayWidth){
        mErrorText = errorText;
        mDialogWidth = displayWidth - 30;

        return new ErrorDialog();
    }

    @Override
    public void onStart() {
        super.onStart();

        getDialog().getWindow().setLayout(mDialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawableResource(R.color.colorDeepGrey);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(R.layout.dialog_error, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mErrorTextView.setText(mErrorText);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
