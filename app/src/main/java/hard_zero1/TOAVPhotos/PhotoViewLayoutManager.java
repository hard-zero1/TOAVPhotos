package hard_zero1.TOAVPhotos;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * A LayoutManager for the RecyclerView that shows the photos in the main activity. It allows to
 * disable and enable scrolling.
 */
public class PhotoViewLayoutManager extends LinearLayoutManager {
    private boolean scrollEnabled;

    public PhotoViewLayoutManager(Context context) {
        super(context);
    }

    public void setScrollEnabled(boolean enabled) {
        this.scrollEnabled = enabled;
    }

    @Override
    public boolean canScrollVertically() {
        return scrollEnabled && super.canScrollVertically();
    }

    @Override
    public boolean canScrollHorizontally() {
        return scrollEnabled && super.canScrollHorizontally();
    }
}
