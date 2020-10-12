package hard_zero1.TOAVPhotos;

import android.content.res.Resources;

import java.io.File;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Holds the data necessary to show a photo with its information at the right position and to
 * handle its file.
 * Note: this class has a natural ordering (compareTo()) that is inconsistent with equals().
 */
public class PhotoViewElement implements Serializable, Comparable<PhotoViewElement> {
    private String infoText;

    private Date time;
    private File file;
    private int number;

    /**
     * The constructor for PhotoViewElement.
     * @param res The Resources to get the format strings from
     * @param number The position number of the photo to hold
     * @param time The time of the photo to hold (when it was taken)
     * @param file A File instance for the photo's file.
     */
    public PhotoViewElement(Resources res, int number, Date time, File file) {
        this.number = number;
        String timeText;
        if(time != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(time);
            timeText = String.format(res.getString(R.string.photo_view_timeText_format), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
            infoText = String.format(res.getString(R.string.photoView_infoText_format_valid), number, timeText);
        }else{
            infoText = String.format(res.getString(R.string.photoView_infoText_format_invalid), number, file.getName());
        }

        this.time = time;
        this.file = file;
    }

    public Date getTime() {
        return time;
    }

    public File getFile() {
        return file;
    }

    public String getInfoText() {
        return infoText;
    }

    public int getNumber() {
        return number;
    }

    /**
     * Implemented for sorting the elements by position number (done in FileTreeOrganizer).
     * Note: this class has a natural ordering that is inconsistent with equals.
     * @param o The PhotoViewElement object to compare with this instance.
     * @return The position number of this instance minus the position number of the given object o.
     */
    @Override
    public int compareTo(PhotoViewElement o) {
        return this.number - o.number;
    }
}
