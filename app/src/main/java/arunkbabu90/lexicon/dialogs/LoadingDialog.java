package arunkbabu90.lexicon.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import arunkbabu90.lexicon.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This dialog can be used using any kind of processing or loading
 */
public class LoadingDialog extends DialogFragment
{
    @BindView(R.id.tv_loading_message) TextView mLoadingMessageTextView;

    private static String mText;

    public static LoadingDialog newInstance(String loadingMessage) {
        mText = loadingMessage;
        return new LoadingDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCancelable(false);
        return inflater.inflate(R.layout.dialog_loading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mLoadingMessageTextView.setText(mText);
    }
}
