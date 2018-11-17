package arunkbabu90.lexicon.tutorials;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TutorialPagerAdapter extends FragmentPagerAdapter
{
    public TutorialPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i)
        {
            case 0:
                return new Page1Fragment();
            case 1:
                return new Page2Fragment();
            case 2:
                return new Page3Fragment();
            case 3:
                return new Page4Fragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}
