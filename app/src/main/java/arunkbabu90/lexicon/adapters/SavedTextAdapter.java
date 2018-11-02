package arunkbabu90.lexicon.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

// TODO: Should be fully implemented
public class SavedTextAdapter extends RecyclerView.Adapter<SavedTextAdapter.SavedTextAdapterViewHolder>
{
    @NonNull
    @Override
    public SavedTextAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull SavedTextAdapterViewHolder savedTextAdapterViewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class SavedTextAdapterViewHolder extends RecyclerView.ViewHolder
    {
        public SavedTextAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
