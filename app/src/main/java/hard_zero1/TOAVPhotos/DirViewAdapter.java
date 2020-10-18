package hard_zero1.TOAVPhotos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

/**
 * The Adapter for the RecyclerView that shows the directories in the main activity.
 */
public class DirViewAdapter extends RecyclerView.Adapter<DirViewAdapter.DirViewViewHolder> {

    private final MainActivity activity;
    private final FileTreeOrganizer fileOrga;
    private DirViewElement[] elements;

    /**
     * Constructor for DirViewAdapter. Gets the elements to show from the FileTreeOrganizer,
     * @param activity The MainActivity instance to use for callbacks on user action
     */
    public DirViewAdapter(MainActivity activity) throws FileTreeOrganizer.ListFilesError, FileTreeOrganizer.DirectoryNotCreatedException {
        this.activity = activity;
        fileOrga = FileTreeOrganizer.getSingletonInstance(activity.getApplicationContext());
        elements = fileOrga.getShownDirElements();
    }

    public void refreshDirView() {
        elements = fileOrga.getShownDirElements();
        notifyDataSetChanged();
    }

    public class DirViewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final TextView tvFileName;
        private final TextView tvDirNumber;

        public DirViewViewHolder(ConstraintLayout lay) {
            super(lay);
            tvFileName = lay.findViewById(R.id.tvFileName); // TextView
            tvDirNumber = lay.findViewById(R.id.tvDirNumber); // TextView
            lay.setOnClickListener(this);
            lay.setOnLongClickListener(this);
        }

        public void setElement(DirViewElement element) {
            tvFileName.setText(element.getDisplayName());
            tvDirNumber.setText(String.valueOf(element.getNumber()));
        }

        @Override
        public void onClick(View v) {
            activity.onDirClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            activity.onDirLongClick(getAdapterPosition());
            return true;
        }
    }

    @NonNull
    @Override
    public DirViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout lay = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.dir_view_element, parent, false);
        return new DirViewViewHolder(lay);
    }

    @Override
    public void onBindViewHolder(@NonNull DirViewViewHolder holder, int position) {
        holder.setElement(elements[position]);
    }

    @Override
    public int getItemCount() {
        return elements.length;
    }
}
