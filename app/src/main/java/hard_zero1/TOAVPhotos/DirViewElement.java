package hard_zero1.TOAVPhotos;

import java.io.File;

/**
 * Holds the data necessary to show a directory with its display name at the right position and to
 * handle it on the file system.
 * Note: this class has a natural ordering (compareTo()) that is inconsistent with equals().
 */
public class DirViewElement implements Comparable<DirViewElement> {
    private String displayName;
    private int number;
    private File dirFile;

    /**
     * The constructor for DirViewElement.
     * @param number The position number of the directory to hold
     * @param displayName The name to display for the directory
     * @param dirFile A File instance for the directory.
     */
    public DirViewElement(int number, String displayName, File dirFile) {
        this.displayName = displayName;
        this.number = number;
        this.dirFile = dirFile;
    }

    public String getDisplayName() {
        return displayName;
    }

    public File getDirFile() {
        return dirFile;
    }

    public int getNumber() {
        return number;
    }

    /**
     * Implemented for sorting the elements by position number (done in FileTreeOrganizer).
     * Note: this class has a natural ordering that is inconsistent with equals.
     * @param o The DirViewElement object to compare with this instance.
     * @return The position number of this instance minus the position number of the given object o.
     */
    @Override
    public int compareTo(DirViewElement o) {
        return this.number - o.number;
    }
}
