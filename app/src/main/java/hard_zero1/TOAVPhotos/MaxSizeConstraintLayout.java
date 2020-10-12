package hard_zero1.TOAVPhotos;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;

public class MaxSizeConstraintLayout extends ConstraintLayout {
    private boolean constrainWidth = false;
    private boolean constrainHeight = false;

    public MaxSizeConstraintLayout(Context context) {
        super(context);
    }

    public MaxSizeConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaxSizeConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if(params.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            constrainWidth = true;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        if(params.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            constrainHeight = true;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        super.setLayoutParams(params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { // Assuming given sizes with EXACTLY-Mode, as LayoutParams should be set to MATCH_PARENT
        if(constrainWidth) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST);
        }
        if(constrainHeight) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
