package hard_zero1.TOAVPhotos;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class FullScreenAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int NORMAL_PHOTO = 0;
    private static final int TRANSITION_VIEW = 1;

    private final Activity activity;
    private final FullScreenPhotoActivity.FullScreenFileManager fileManager;
    private PhotoViewElement[] items;
    private final int transitionLayoutID;

    public FullScreenAdapter(Activity activity, FullScreenPhotoActivity.FullScreenFileManager fileManager, boolean verticalOrientation) throws FileTreeOrganizer.ListFilesError {
        this.activity = activity;
        this.fileManager = fileManager;
        this.items = fileManager.getShownPhotoElements();

        this.transitionLayoutID = verticalOrientation ? R.layout.full_screen_transition_element_vert : R.layout.full_screen_transition_element_horiz;
    }

    public class FullScreenPhotoHolder extends RecyclerView.ViewHolder {
        private final PhotoView photoView;

        public FullScreenPhotoHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.pvFullScreen); // PhotoView
            photoView.setBackgroundColor(0xff000000);
        }

        public void setElement(PhotoViewElement item) {
            Glide.with(activity).load(item.getFile().getUri()).into(photoView);
        }
    }

    public static class FullScreenTransitionHolder extends RecyclerView.ViewHolder {
        TextView prevDisplay;
        TextView nextDisplay;

        /**
         * @param itemView An instance of the ConstraintLayout for the transition view
         */
        public FullScreenTransitionHolder(@NonNull View itemView) {
            super(itemView);
            ConstraintLayout lay = (ConstraintLayout) itemView;
            prevDisplay = lay.findViewById(R.id.tvPrevDisplayName); // TextView
            nextDisplay = lay.findViewById(R.id.tvNextDisplayName); // TextView
        }

        public void setInfo(DirViewElement previous, DirViewElement next) {
            if(previous != null) {
                prevDisplay.setText(previous.getDisplayName());
            }else{
                prevDisplay.setText("");
            }
            if(next != null) {
                nextDisplay.setText(next.getDisplayName());
            }else{
                nextDisplay.setText("");
            }
        }
    }

    /**
     * Removes from the shown items the elements of the first directory shown before and adds those
     * of the next directory (not shown before). The forward() method of the FullScreenFileManager
     * should be called before.
     */
    public void forwardRefresh() throws FileTreeOrganizer.ListFilesError {
        int centerTransitionIndexBefore = fileManager.getCenterTransitionIndex();
        items = fileManager.getShownPhotoElements();
        notifyItemRangeRemoved(0, centerTransitionIndexBefore); // Removes the first items until the transition view, which is not removed
        int centerTransitionIndexNow = fileManager.getCenterTransitionIndex();
        notifyItemRangeInserted(centerTransitionIndexNow + 1, getItemCount() - (centerTransitionIndexNow + 1));
    }

    /**
     * Removes from the shown items the elements of the last directory shown before and adds those
     * of the last directory before the first directory shown before. The backward() method of the
     * FullScreenFileManager should be called before.
     */
    public void backwardRefresh() throws FileTreeOrganizer.ListFilesError {
        int centerTransitionIndexBefore = fileManager.getCenterTransitionIndex();
        int itemCountBefore = getItemCount();
        items = fileManager.getShownPhotoElements();
        notifyItemRangeRemoved(centerTransitionIndexBefore + 1, itemCountBefore - (centerTransitionIndexBefore + 1));
        notifyItemRangeInserted(0, fileManager.getCenterTransitionIndex());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == NORMAL_PHOTO) {
            return new FullScreenPhotoHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.full_screen_element, parent, false));
        }else{ // TRANSITION_VIEW
            return new FullScreenTransitionHolder(LayoutInflater.from(parent.getContext()).inflate(transitionLayoutID, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == NORMAL_PHOTO) {
            ((FullScreenPhotoHolder) holder).setElement(items[position]);
        }else{ // TRANSITION_VIEW
            if(position == 0) {
                ((FullScreenTransitionHolder) holder).setInfo(fileManager.getPreviousDir(), fileManager.getCurrentDir1());
            }else if(position == fileManager.getCenterTransitionIndex()) { // order important if only one directory shown/existent
                ((FullScreenTransitionHolder) holder).setInfo(fileManager.getCurrentDir1(), fileManager.getCurrentDir2());
            }else if(position == getItemCount() - 1) {
                ((FullScreenTransitionHolder) holder).setInfo(fileManager.getCurrentDir2(), fileManager.getNextDir());
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == fileManager.getCenterTransitionIndex() || position == 0 || position == getItemCount() - 1) {
            return TRANSITION_VIEW;
        }
        return NORMAL_PHOTO;
    }

    @Override
    public int getItemCount() {
        return items.length;
    }
}
