package hard_zero1.TOAVPhotos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

import static java.lang.Math.max;

/**
 * A singleton class to handle the file system actions and provide information about the images and
 * folders in the current directory.
 */
public class FileTreeOrganizer {
    private static FileTreeOrganizer instance;
    private final Resources res;

    private final SimpleDateFormat timeFormat;

    private DocumentFile currentDir;
    private DirViewElement[] dirViewElements;
    private PhotoViewElement[] photoViewElements;
    private DocumentFile currentSaveFile;
    private int currentPhotoCounter;
    private int currentDirCounter;

    /**
     * Thrown when listing files of a parent directory (the current Directory) fails.
     */
    public static class ListFilesError extends Exception {}

    /**
     * Thrown when creating the initial directory fails.
     */
    public static class DirectoryNotCreatedException extends Exception {}

    /**
     * Thrown when creating the file prepared for saving a photo failed.
     */
    public static class FileNotCreatedException extends Exception {}

    /**
     * Thrown when deleting the file prepared for saving a photo failed.
     */
    public static class PhotoFileNotDeletedException extends Exception {}

    /**
     * Thrown when renaming a file failed.
     */
    public static class RenameFailedException extends Exception {}

    @SuppressLint("SimpleDateFormat")
    private FileTreeOrganizer(Context appContext, DocumentFile parent) throws DirectoryNotCreatedException, ListFilesError {
        this.res = appContext.getResources();
        timeFormat = new SimpleDateFormat(res.getString(R.string.date_format_in_filename));
        currentDir = parent.findFile(appContext.getResources().getString(R.string.initial_subdirectory));
        if (currentDir == null) {
            currentDir = parent.createDirectory(appContext.getResources().getString(R.string.initial_subdirectory));
            if(currentDir == null) {
                throw new DirectoryNotCreatedException();
            }
        }
        refresh();
    }

    /**
     * For use with api level below 21
     */
    public static FileTreeOrganizer getSingletonInstance(Context appContext) throws DirectoryNotCreatedException, ListFilesError {
        if(instance == null) {
            File extRoot = Environment.getExternalStorageDirectory();
            instance = new FileTreeOrganizer(appContext, DocumentFile.fromFile(extRoot));
        }
        return instance;
    }
    /**
     * For use with api level 21 or higher
     */
    @RequiresApi(api = 21)
    public static FileTreeOrganizer getSingletonInstance(Context appContext, Uri initialParent) throws DirectoryNotCreatedException, ListFilesError {
        if(instance == null) {
            instance = new FileTreeOrganizer(appContext, DocumentFile.fromTreeUri(appContext, initialParent));
        }
        return instance;
    }

    /**
     * Enter the directory at index pos as new current "working" directory.
     * @param pos The index of the directory
     */
    public void cdEnter(int pos) throws ListFilesError {
        currentDir = dirViewElements[pos].getDirFile();
        refresh();
    }

    /**
     * Move to the parent directory as new current "working" directory.
     */
    public void cdUP() throws ListFilesError {
        DocumentFile parent = currentDir.getParentFile();
        if (parent != null && parent.canRead()) {
            currentDir = parent;
        }
        refresh();
    }

    /**
     * Reloads the files of the current directory and updates the counters. Called at the end of
     * many other methods of FileTreeOrganizer.
     * @throws ListFilesError Will be thrown if the current directory's (File.)listFiles() method returns null
     */
    public void refresh() throws ListFilesError {
        dirViewElements = generateDirElements(currentDir);
        photoViewElements = generatePhotoViewElements(currentDir);
        refreshCurrentFileCounter();
        refreshCurrentDirCounter();
    }

    /**
     * Generates an array of DirViewElements for the subdirectories of the given parent directory,
     * sorted by position number.
     * @param parent The parent directory
     * @return The generated array of DirViewElements
     * @throws ListFilesError Will be thrown if the parent.listFiles() method returns null
     */
    public DirViewElement[] generateDirElements(DocumentFile parent) throws ListFilesError {
        ArrayList<DirViewElement> dirElements = new ArrayList<>();
        DocumentFile[] dirList = parent.listFiles();
        if (dirList == null) { throw new ListFilesError(); }
        for (DocumentFile file : dirList) {
            if(file.isDirectory()) {
                String filename = file.getName();
                dirElements.add(new DirViewElement(getDirCounter(filename), getDirDisplayName(filename), file));
            }
        }
        DirViewElement[] dirElementsArray = dirElements.toArray(new DirViewElement[0]);
        Arrays.sort(dirElementsArray);
        return dirElementsArray;
    }

    /**
     * Generates an array of PhotoViewElements for the images in the given directory,
     * sorted by position number.
     * @param parent The directory of the images
     * @return The generated array of PhotoViewElements
     * @throws ListFilesError Will be thrown if the parent.listFiles() method returns null
     */
    public PhotoViewElement[] generatePhotoViewElements(DocumentFile parent) throws ListFilesError {
        ArrayList<PhotoViewElement> photoViewElements = new ArrayList<>();
        DocumentFile[] photoList = parent.listFiles();
        if (photoList == null) { throw new ListFilesError(); }
        for (DocumentFile file : photoList) {
            String filename = file.getName();
            if(file.isFile() && filename != null && (filename.endsWith(res.getString(R.string.photo_file_extension_accept)) || filename.endsWith(res.getString(R.string.photo_file_extension_accept2)))) {
                photoViewElements.add(new PhotoViewElement(res, getFileCounter(filename), getFileTime(filename), file));
            }
        }
        PhotoViewElement[] photoViewElementsArray = photoViewElements.toArray(new PhotoViewElement[0]);
        Arrays.sort(photoViewElementsArray);
        return photoViewElementsArray;
    }

    /**
     * Returns a DirViewElement for the current "working" directory.
     * @return A DirViewElement for the current "working" directory.
     */
    public DirViewElement getCurrentDirElement() {
        String name = currentDir.getName();
        return new DirViewElement(getDirCounter(name), getDirDisplayName(name), currentDir);
    }

    /**
     * Returns the current array of DirViewElements.
     * @return The current array of DirViewElements
     */
    public DirViewElement[] getShownDirElements() {
        return dirViewElements;
    }
    /**
     * Returns the current array of PhotoViewElements.
     * @return The current array of PhotoViewElements
     */
    public PhotoViewElement[] getShownPhotoElements() {
        return photoViewElements;
    }

    /**
     * Creates (if not already there) a new file with the next higher position number and the current date and time.
     * @return The Uri of the created file
     */
    public Uri prepareSaveFile() throws FileNotCreatedException{
        String dateStr = timeFormat.format(new Date());
        @SuppressLint("DefaultLocale")
        String counterStr = String.format(res.getString(R.string.photo_file_number_prefix_format), currentPhotoCounter + 1);
        currentSaveFile = currentDir.createFile("image/jpeg", counterStr + res.getString(R.string.photo_file_number_prefix_separator) + dateStr + res.getString(R.string.photo_file_extension_toavp));
        if(currentSaveFile == null) {
            throw new FileNotCreatedException();
        }
        return currentSaveFile.getUri();
    }

    /**
     * Calls refresh() so the file prepared to save a photo will be recognized and shown.
     * Called from the TakePhotoActivity when a photo was successfully taken and saved.
     */
    public void confirmSaveFile() throws ListFilesError {
        refresh();
    }

    /**
     * Deletes the file last prepared to save a photo. Called from the TakePhotoActivity before
     * the file is registered in the photoViewElements array when failed to take or save a photo.
     */
    public void cancelSaveFile() throws PhotoFileNotDeletedException {
        if (!currentSaveFile.delete()) {
            throw new PhotoFileNotDeletedException();
        }
    }

    /**
     * Creates a new directory at the position with the given number. If it is not the highest
     * number, the numbers of all other directories with equal or higher number will increase by one.
     * @param displayName The name to be displayed for the new folder
     * @param number The number of the new folder
     * @return If the directory was successfully created
     */
    public boolean mkDir(String displayName, int number) throws RenameFailedException, ListFilesError {
        int index = 0;
        for(; index < dirViewElements.length; index++) {
            if(dirViewElements[index].getNumber() >= number) {
                break;
            }
        }
        shiftDirFileNumbers(index, true);
        @SuppressLint("DefaultLocale")
        boolean created = null != currentDir.createDirectory(displayName + String.format(res.getString(R.string.directory_number_suffix_format), number));
        refresh();
        return created;
    }

    /**
     * Deletes the directory at index position. The numbers of directories with numbers higher than
     * the number of the deleted directory will decrease by one.
     * @param position The index of the folder to be deleted
     * @return If the directory was successfully deleted
     */
    public boolean deleteDir(int position) throws RenameFailedException, ListFilesError{
        boolean deleted = dirViewElements[position].getDirFile().delete();
        if(deleted) {
            shiftDirFileNumbers(position, false);
        }
        refresh();
        return deleted;
    }

    /**
     * Deletes the photo at index position. The numbers of photos with numbers higher than
     * the number of the deleted photo will decrease by one.
     * @param position The index of the photo to be deleted
     * @return If the photo was successfully deleted
     */
    public boolean deletePhoto(int position) throws RenameFailedException, ListFilesError {
        boolean deleted = photoViewElements[position].getFile().delete();
        if(deleted) {
            shiftPhotoFileNumbers(position, false);
        }
        refresh();
        return deleted;
    }

    /**
     * Gives the directory at index oldPos the display name newDisplayName and the number newNumber.
     * The numbers of directories with numbers between the old number and newNumber will increase
     * or decrease by one accordingly, except if the directory has an invalid filename (number 0).
     * Then, the numbers of all other directories with equal or higher number than newNumber will increase by one.
     * @param oldPos The index of the directory
     * @param newNumber The new number for the directory
     * @param newDisplayName The new name to be displayed for the directory
     */
    public void moveDir(int oldPos, int newNumber, String newDisplayName) throws RenameFailedException, ListFilesError {
        int newPos = 0;
        for(; newPos < dirViewElements.length; newPos++) {
            if(dirViewElements[newPos].getNumber() >= newNumber) {
                break;
            }
        }
        moveSingleDir(oldPos, dirViewElements[oldPos].getNumber(), res.getString(R.string.temp_dirname_being_moved_prefix) + newDisplayName);
        boolean shiftAtNewPos = shiftDirFileNumbers(oldPos, false) &&  (newPos > oldPos);
        shiftDirFileNumbers((shiftAtNewPos ? newPos + 1 : newPos), true);
        moveSingleDir(oldPos, newNumber, newDisplayName);
        refresh();
    }

    /**
     * Gives the photo at index oldPos the number newNumber. The numbers of photos with numbers
     * between the old number and newNumber will increase or decrease by one accordingly.
     * @param oldPos The index of the directory
     * @param newNumber The new number for the directory
     */
    public void movePhoto(int oldPos, int newNumber) throws RenameFailedException, ListFilesError {
        int newPos = 0;
        for(; newPos < photoViewElements.length; newPos++) {
            if(photoViewElements[newPos].getNumber() >= newNumber) {
                break;
            }
        }
        moveSinglePhoto(oldPos, photoViewElements[oldPos].getNumber());
        boolean shiftAtNewPos = shiftPhotoFileNumbers(oldPos, false) && (newPos > oldPos);
        shiftPhotoFileNumbers((shiftAtNewPos ? newPos + 1 : newPos), true);
        moveSinglePhoto(oldPos, newNumber);
        refresh();
    }

    /**
     * Returns the position number of the photo with the highest position number.
     * @return The position number of the photo with the highest position number.
     */
    public int getCurrentPhotoCounter() {
        return currentPhotoCounter;
    }

    /**
     * Returns the position number of the folder with the highest position number.
     * @return The position number of the folder with the highest position number.
     */
    public int getCurrentDirCounter() {
        return currentDirCounter;
    }

    /**
     * Returns the Date of the time that is encoded in the given filename of a photo.
     * @param filename The filename to extract the time from
     * @return The Date for the extracted time
     */
    private Date getFileTime(String filename) {
        try {
            return timeFormat.parse(filename, new ParsePosition(4));
        }catch(Exception e){
            return null;
        }
    }

    /**
     * Returns the number of a photo with the given filename.
     * @param filename The filename to extract the number from
     * @return The extracted number or 0 if the filename is not valid
     */
    private int getFileCounter(String filename) {
        try {
            return Integer.parseInt(filename.substring(0, 3));
        }catch(Exception exp){
            return 0;
        }
    }

    /**
     * Returns the number of a directory with the given name.
     * @param dirName The name to extract the number from
     * @return The extracted number or 0 if the filename is not valid
     */
    private int getDirCounter(String dirName) {
        if(Pattern.compile(res.getString(R.string.directory_number_suffix_regex)).matcher(dirName).matches()) {
            try {
                return Integer.parseInt(dirName.substring(dirName.length() - 3));
            }catch(Exception exp){
                return 0;
            }
        }
        return 0;
    }

    /**
     * Returns the name to be displayed for a directory with the given filename.
     * @param dirName The name to extract the display name from
     * @return The extracted display name
     */
    private String getDirDisplayName(String dirName) {
        if(Pattern.compile(res.getString(R.string.directory_number_suffix_regex)).matcher(dirName).matches()) {
            return dirName.substring(0, dirName.length() - 9);
        }
        return dirName;
    }

    private void refreshCurrentFileCounter() {
        int number = 0;
        for(PhotoViewElement element : photoViewElements) {
            number = max(number, element.getNumber());
        }
        currentPhotoCounter = number;
    }

    private void refreshCurrentDirCounter() {
        int number = 0;
        for(DirViewElement element : dirViewElements) {
            number = max(number, element.getNumber());
        }
        currentDirCounter = number;
    }

    /**
     * Renames the directory at index oldIndex such that is has the position number newNumber and
     * the display name newDisplayName. refresh() is not called, so the directory keeps its place
     * (at oldIndex) in the dirViewElements array.
     * @param oldIndex The index of the directory to rename
     * @param newNumber The new position number for the directory
     * @param newDisplayName The new display name for the directory
     */
    private void moveSingleDir(int oldIndex, int newNumber, String newDisplayName) throws RenameFailedException{
        @SuppressLint("DefaultLocale")
        String filename = newDisplayName + String.format(res.getString(R.string.directory_number_suffix_format), newNumber);
        DocumentFile file = dirViewElements[oldIndex].getDirFile();
        if(!file.renameTo(filename)) {
            throw new RenameFailedException();
        }
        dirViewElements[oldIndex] = new DirViewElement(newNumber, getDirDisplayName(filename), file);
    }

    /**
     * If forward is true: Renames the directories with index lowestIndex or higher such that
     * their position number increases by one.
     * If forward is false: Renames the directories with index higher than lowestIndex such that
     * their position number decreases by one.
     * If the directory at lowestIndex has number 0 or an invalid filename, nothing happens and
     * false is returned to avoid renaming files/directories that were not created by this app or
     * moving directories to number 0. Otherwise true is returned.
     * refresh() is not called, so all directories keep their place in the dirViewElements array.
     * @param lowestIndex The index of the directory with the lowest index to be shifted
     * @param forward If the numbers of the shifted directories should increase (or decrease) by one
     * @return False, if the number of the element at lowestIndex is 0 (nothing happened), true otherwise
     */
    private boolean shiftDirFileNumbers(int lowestIndex, boolean forward) throws RenameFailedException{
        if(lowestIndex < dirViewElements.length && dirViewElements[lowestIndex].getNumber() == 0) {
            return false;
        }
        if(forward) {
            for (int i = dirViewElements.length - 1; i >= lowestIndex; i--) {
                moveSingleDir(i, dirViewElements[i].getNumber() + 1, dirViewElements[i].getDisplayName());
            }
        }else {
            for (int i = lowestIndex + 1; i < dirViewElements.length; i++) {
                moveSingleDir(i, dirViewElements[i].getNumber() - 1, dirViewElements[i].getDisplayName());
            }
        }
        return true;
    }

    /**
     * Renames the images at index oldIndex such that is has the position number newNumber.
     * refresh() is not called, so the photo keeps its place (at oldIndex) in the photoViewElements array.
     * @param oldIndex The index of the photo to rename
     * @param newNumber The new position number for the photo
     */
    private void moveSinglePhoto(int oldIndex, int newNumber) throws RenameFailedException{
        Date time = photoViewElements[oldIndex].getTime();
        String timeString = res.getString(R.string.photo_file_no_time);
        if(time != null) {
            timeString = timeFormat.format(time);
        }
        @SuppressLint("DefaultLocale")
        String filename = String.format(res.getString(R.string.photo_file_number_prefix_format), newNumber) + res.getString(R.string.photo_file_number_prefix_separator) + timeString + res.getString(R.string.photo_file_extension_toavp);
        DocumentFile file = photoViewElements[oldIndex].getFile();
        if(!file.renameTo(filename)) {
            throw new RenameFailedException();
        }
        photoViewElements[oldIndex] = new PhotoViewElement(res, newNumber, time, file);
    }

    /**
     * If forward is true: Renames the photos with index lowestIndex or higher such that
     * their position number increases by one.
     * If forward is false: Renames the photos with index higher than lowestIndex such that
     * their position number decreases by one.
     * If the photo at lowestIndex has number 0 or an invalid filename, nothing happens and
     * false is returned to avoid renaming files that were not created by this app or moving
     * photos to number 0. Otherwise true is returned.
     * refresh() is not called, so all photos keep their place in the photoViewElements array.
     * @param lowestIndex The index of the photo with the lowest index to be shifted
     * @param forward If the numbers of the shifted photos should increase (or decrease) by one
     * @return False, if the number of the element at lowestIndex is 0 (nothing happened), true otherwise
     */
    private boolean shiftPhotoFileNumbers(int lowestIndex, boolean forward) throws RenameFailedException{
        if(lowestIndex < photoViewElements.length && photoViewElements[lowestIndex].getNumber() == 0) {
            return false;
        }
        if(forward) {
            for (int i = photoViewElements.length - 1; i >= lowestIndex; i--) {
                moveSinglePhoto(i, photoViewElements[i].getNumber() + 1);
            }
        }else {
            for (int i = lowestIndex + 1; i < photoViewElements.length; i++) {
                moveSinglePhoto(i, photoViewElements[i].getNumber() - 1);
            }
        }
        return true;
    }
}
