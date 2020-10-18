package hard_zero1.TOAVPhotos;

import androidx.documentfile.provider.DocumentFile;

/**
 * Holds the data necessary to show a directory with its display name at the right position and to
 * handle it on the file system.
 * Note: this class has a natural ordering (compareTo()) that is inconsistent with equals().
 */
public class DirViewElement implements Comparable<DirViewElement> {
    private final String displayName;
    private final int number;
    private final DocumentFile dirFile;

    /**
     * The constructor for DirViewElement.
     * @param number The position number of the directory to hold
     * @param displayName The name to display for the directory
     * @param dirFile A File instance for the directory.
     */
    public DirViewElement(int number, String displayName, DocumentFile dirFile) {
        this.displayName = displayName;
        this.number = number;
        this.dirFile = dirFile;
    }

    public String getDisplayName() {
        return displayName;
    }

    public DocumentFile getDirFile() {
        return dirFile;
    }

    public int getNumber() {
        return number;
    }

    public boolean dirEquals(DirViewElement o) {
        return dirFile.getUri().equals(o.dirFile.getUri());
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
