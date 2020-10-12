package hard_zero1.TOAVPhotos;

import android.annotation.SuppressLint;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.OnViewDragListener;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * The Adapter for the RecyclerView that shows the photos in the main activity.
 */
public class PhotoViewAdapter extends RecyclerView.Adapter<PhotoViewAdapter.PhotoViewHolder> implements View.OnTouchListener {
    private MainActivity activity;
    private PhotoViewLayoutManager layManager;
    private FileTreeOrganizer fileOrga;
    private PhotoViewElement[] items;

    /**
     * Constructor for PhotoViewAdapter. Gets the elements to show from the FileTreeOrganizer,
     * @param activity The MainActivity instance to use for callbacks on user action
     * @param layManager The PhotoViewLayoutManager used as LayoutManager for the RecyclerView to use to enable and disable scrolling
     */
    public PhotoViewAdapter(MainActivity activity, PhotoViewLayoutManager layManager) throws FileTreeOrganizer.ListFilesError, FileTreeOrganizer.DirectoryNotCreatedException {
        this.activity = activity;
        this.layManager = layManager;
        this.fileOrga = FileTreeOrganizer.getSingletonInstance(activity.getApplicationContext());
        this.items = fileOrga.getShownPhotoElements();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) { // Receives events from the whole RecyclerView
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            layManager.setScrollEnabled(true);
        }
        return false;
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, OnViewDragListener, OnScaleChangedListener, View.OnClickListener, View.OnLongClickListener {
        TextView tvPhotoInfo;
        PhotoView pvPhotoView;
        GestureDetector gestureDetector;

        @SuppressLint("ClickableViewAccessibility")
        public PhotoViewHolder(ConstraintLayout lay) {
            super(lay);
            lay.findViewById(R.id.leftScrollBridge).setOnTouchListener(this);
            lay.findViewById(R.id.rightScrollBridge).setOnTouchListener(this);
            tvPhotoInfo = lay.findViewById(R.id.tvPhotoInfo); // TextView
            tvPhotoInfo.setOnTouchListener(this);
            pvPhotoView = lay.findViewById(R.id.pvPhotoView); // PhotoView
            pvPhotoView.setOnViewDragListener(this);
            pvPhotoView.setOnScaleChangeListener(this);
            pvPhotoView.setOnClickListener(this);
            pvPhotoView.setOnLongClickListener(this);

            gestureDetector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    onClick(null);
                    return true;
                }
                @Override
                public void onLongPress(MotionEvent e) {
                    onLongClick(null);
                }
            });
        }

        public void setPhoto(PhotoViewElement item) {
            tvPhotoInfo.setText(item.getInfoText());
            Glide.with(activity).load(item.getFile()).into(pvPhotoView);
        }

        /**
         * Disables RecyclerView scroll when scaling the photo.
         */
        @Override
        public void onScaleChange(float scaleFactor, float focusX, float focusY) {
            layManager.setScrollEnabled(false);
        }
        /**
         * Disables RecyclerView scroll when dragging the photo.
         */
        @Override
        public void onDrag(float dx, float dy) {
            layManager.setScrollEnabled(false);
        }

        /**
         * Enables scroll when touching (ACTION_DOWN event) the left or right bridge or the
         * info-TextView. Returns true then, so the gesture is not passed to the PhotoView, what
         * would disable scrolling again. But it also means that click events on the other views are
         * not detected, so the every event is passed to a gesture detector to detect them anyways.
         */
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                layManager.setScrollEnabled(true);
                return true;
            }
            return false;
        }
        @Override
        public void onClick(View v) {
            activity.onPhotoClick(getAdapterPosition());
        }
        @Override
        public boolean onLongClick(View v) {
            activity.onPhotoLongClick(getAdapterPosition());
            return false;
        }
    }

    public void refreshPhotoView() {
        this.items = fileOrga.getShownPhotoElements();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout lay = (ConstraintLayout) LayoutInflater.from(activity).inflate(R.layout.photo_view_element, parent, false);
        return new PhotoViewHolder(lay);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.setPhoto(items[position]);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }
}
