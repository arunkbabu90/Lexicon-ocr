package arunkbabu90.lexicon.tutorials;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import arunkbabu90.lexicon.Constants;
import arunkbabu90.lexicon.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TutorialActivity extends AppCompatActivity implements View.OnClickListener
{
    @BindView(R.id.tutorial_view_pager) ViewPager mViewPager;
    @BindView(R.id.btn_tutorial_next) Button mNextButton;
    @BindView(R.id.btn_tutorial_skip) Button mSkipButton;
    @BindView(R.id.dotsLayout) LinearLayout mDotsLayout;
    @BindView(R.id.tutorial_layout) RelativeLayout mTutorialLayout;

    private Resources r;
    private TextView[] mDots;
    private TutorialPagerAdapter mPagerAdapter;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mPrefEditor;
    private int mCurrentPage = 0;
    private boolean mIsSkipPressed;
    private boolean mIsFinishPressed;
    private boolean mIsLastPage;
    private int color;
    private int mShortAnimationDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        ButterKnife.bind(this);

        r = getResources();
        mSharedPreferences = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mPagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(pageChangeListener);
        addDotsIndicator(0);


        getWindow().setStatusBarColor(r.getColor(R.color.colorDeepGrey));
        getWindow().setNavigationBarColor(r.getColor(R.color.colorDeepGrey));
        mTutorialLayout.setBackgroundColor(r.getColor(R.color.colorDeepGrey));

        mSkipButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
    }

    /**
     * Adds dots to indicate the page position of the tutorial
     * @param position The current position of the page
     */
    private void addDotsIndicator(int position) {
        mDots = new TextView[mPagerAdapter.getCount()];
        mDotsLayout.removeAllViews();

        // Add the dots to the view
        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226"));  // ASCII Code for Bullet in Html
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(r.getColor(R.color.colorGreyedText));
            mDotsLayout.addView(mDots[i]);
        }
        // Change the color of the current page's dot to white to highlight the
        //  current page position
        if (mDots.length > 0) {
            mDots[position].setTextColor(r.getColor(android.R.color.white));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_tutorial_next:
                if (mIsLastPage) {
                    mIsFinishPressed = true;
                    finish();
                } else {
                    mViewPager.setCurrentItem(mCurrentPage + 1);
                }
                break;
            case R.id.btn_tutorial_skip:
                mIsSkipPressed = true;
                finish();
                break;
        }

    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) { }

        @Override
        public void onPageSelected(int i) {
            changeBgColor(i);
            addDotsIndicator(i);
            mCurrentPage = i;
            // Change the Next Button text to Finish when the Tutorial reaches the end of page
            if (i == mDots.length - 1) {
                mSkipButton.setVisibility(View.INVISIBLE);
                mNextButton.setText(R.string.finish);
                mIsLastPage = true;
            } else {
                mSkipButton.setVisibility(View.VISIBLE);
                mNextButton.setText(R.string.next);
                mIsLastPage = false;
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) { }
    };

    /**
     * Changes the color of status & navigation bar with respect to the background
     * @param i The current position of the page
     */
    private void changeBgColor(int i) {
        color = android.R.color.black;
        switch (i)
        {
            case 0:
                color = R.color.colorDeepGrey;
                break;
            case 1:
                color = R.color.vibrantBlue;
                break;
            case 2:
                color = R.color.vibrantGreen;
                break;
            case 3:
                color = R.color.vibrantAmber;
                break;
        }

        getWindow().setStatusBarColor(r.getColor(color));
        getWindow().setNavigationBarColor(r.getColor(color));

        mTutorialLayout.animate().alpha(0.9f).setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mTutorialLayout.setBackgroundColor(r.getColor(color));
                        mTutorialLayout.animate().alpha(1f).setDuration(mShortAnimationDuration);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsSkipPressed || mIsFinishPressed) {
            mPrefEditor = mSharedPreferences.edit();
            mPrefEditor.putBoolean(Constants.PREF_TUTORIAL_COMPLETED, true);
            mPrefEditor.apply();
        }
    }
}
