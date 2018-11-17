package arunkbabu90.lexicon.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

import arunkbabu90.lexicon.R;
import arunkbabu90.lexicon.database.AppExecutor;
import arunkbabu90.lexicon.database.Text;
import arunkbabu90.lexicon.database.TextDatabase;

public class ListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this);
    }

    class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory
    {
        private Context mContext;
        private List<Text> mSavedTextList;

        public ListRemoteViewsFactory(Context context) {
            mContext = context;
        }

        @Override
        public void onCreate() { }

        @Override
        public void onDataSetChanged() {
            AppExecutor.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    TextDatabase db = TextDatabase.getInstance(mContext);
                    mSavedTextList = db.textDao().loadTexts();
                }
            });
        }

        @Override
        public void onDestroy() { }

        @Override
        public int getCount() {
            if (mSavedTextList == null) return 0;
            return mSavedTextList.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_saved_text);

            String text = mSavedTextList.get(position).getSavedText();
            views.setTextViewText(R.id.tv_widget_text_item, text);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
