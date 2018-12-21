package arunkbabu90.lexicon.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import arunkbabu90.lexicon.R;
import arunkbabu90.lexicon.database.Text;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SavedTextAdapter extends RecyclerView.Adapter<SavedTextAdapter.SavedTextAdapterViewHolder>
{
    private ArrayList<Text> mSavedTextList;
    private ItemClickListener mItemClickListener;

    public void setSavedTexts(ArrayList<Text> savedTextList) {
        mSavedTextList = savedTextList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SavedTextAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_saved_text, viewGroup, false);
        return new SavedTextAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedTextAdapterViewHolder savedTextAdapterViewHolder, int i) {
        savedTextAdapterViewHolder.mSavedTextView.setText(mSavedTextList.get(i).getSavedText());
    }

    @Override
    public int getItemCount() {
        return mSavedTextList == null ? 0 : mSavedTextList.size();
    }

    /**
     * Returns the saved text list from the adapter
     * @return The List of saved texts
     */
    public ArrayList<Text> getSavedTextList() {
        return mSavedTextList;
    }

    public class SavedTextAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        @BindView(R.id.tv_saved_text) TextView mSavedTextView;
        @BindView(R.id.btn_saved_text_share) ImageButton mImageButton;
        @BindView(R.id.saved_text_card) public CardView viewForeground;

        public SavedTextAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mImageButton.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                int position = getAdapterPosition();
                String text = mSavedTextList.get(position).getSavedText();
                mItemClickListener.onItemClick(v, text);
            }
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View v, String savedText);
    }
}