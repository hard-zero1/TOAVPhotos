package hard_zero1.TOAVPhotos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Activity to show the photos in ihe current directory in fullscreen mode. You can slide through
 * the photos in horizontal or vertical direction. Photos are ordered by position number as in the
 * main activity. When sliding past the last or first photo of the current directory you can reach
 * the photos of the next/previous sibling directory. Between the directories a divider is shown
 * with the display names of the directories.
 */
public class FullScreenPhotoActivity extends AppCompatActivity {

    public static final String POSITION_TO_SHOW = "hard-zero1.FullScreenPhotoActivity.positionToShow";
    public static final String SCROLL_VERTICAL = "hard-zero1.FullScreenPhotoActivity.verticalScrollOrientation";

    private int lastIndex;

    /**
     * Initializes the activity, sets the scroll direction and shown element as given by the Intent.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_full_screen_photo);

        ViewPager2 vpGallery = findViewById(R.id.vpFullScreenGallery);
        FullScreenFileManager fileManager;
        boolean verticalScrollOrientation;
        FullScreenAdapter adapter;
        try {
            fileManager = new FullScreenFileManager();
            verticalScrollOrientation = getIntent().getBooleanExtra(SCROLL_VERTICAL, false);
            adapter = new FullScreenAdapter(this, fileManager, verticalScrollOrientation);
        } catch (FileTreeOrganizer.DirectoryNotCreatedException e) {
            Toast.makeText(this, getResources().getString(R.string.error_initial_dir_not_created), Toast.LENGTH_SHORT).show();
            finish();
            return;
        } catch (NoParentFileException e) {
            Toast.makeText(this, getResources().getString(R.string.error_no_parent), Toast.LENGTH_SHORT).show();
            finish();
            return;
        } catch (FileTreeOrganizer.ListFilesError listFilesError) {
            Toast.makeText(this, getResources().getString(R.string.error_list_dirs), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        vpGallery.setAdapter(adapter);
        vpGallery.setOrientation(verticalScrollOrientation ? ViewPager2.ORIENTATION_VERTICAL : ViewPager2.ORIENTATION_HORIZONTAL);
        vpGallery.setCurrentItem(getIntent().getIntExtra(POSITION_TO_SHOW, 0) + 1, false);
        vpGallery.registerOnPageChangeCallback(new FullScreenPageChangeListener(vpGallery, adapter, fileManager));
    }

    /**
     * Thrown if failed to get the current "working" directory.
     */
    public static class NoParentFileException extends Exception {}

    public class FullScreenPageChangeListener extends ViewPager2.OnPageChangeCallback {
        ViewPager2 viewPager;
        FullScreenAdapter adapter;
        FullScreenFileManager fileManager;

        /**
         * @param viewPager The ViewPager to which this listener is registered
         * @param adapter The ViewPager's adapter
         * @param fileManager The FullScreenFileManager to handle the directory transitions
         */
        public FullScreenPageChangeListener(ViewPager2 viewPager, FullScreenAdapter adapter, FullScreenFileManager fileManager) {
            this.viewPager = viewPager;
            this.adapter = adapter;
            this.fileManager = fileManager;
        }

        /*@Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            ...*/
        // Using this instead of onPageScrolled() (doing the same) is more buggy: when scrolling over directories multiple times in one direction it sometimes stops. Scroll back a little to go further.

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            try {
                if (position == 0) {
                    if (fileManager.backward()) {
                        adapter.backwardRefresh();
                    }
                } else if (position == lastIndex) {
                    if (fileManager.forward()) {
                        adapter.forwardRefresh();
                    }
                }
            }catch (NoParentFileException e) {
                Toast.makeText(FullScreenPhotoActivity.this, getResources().getString(R.string.error_no_parent), Toast.LENGTH_SHORT).show();
            } catch (FileTreeOrganizer.ListFilesError listDirectoriesError) {
                Toast.makeText(FullScreenPhotoActivity.this, getResources().getString(R.string.error_list_dirs), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Keeps track of the current and surrounding sibling directories and provides the array of
     * PhotoViewElements to show. Since either backward() or forward() is called and if there are
     * sibling directories, the photos of two directories are in the array of elements to show. So
     * when scrolling back and forth only between two neighboring directories with photos, nothing
     * has to be reloaded.
     */
    public class FullScreenFileManager {
        private final FileTreeOrganizer fileOrga;
        // See getters for documentation
        private int centerTransitionIndex;
        private DirViewElement previousDir;
        private DirViewElement currentDir1;
        private DirViewElement currentDir2;
        private DirViewElement nextDir;
        /**
         * If only the photos of currentDir1 are within the shown photos. True if there are no sibling directories and before either backward() or forward() is called.
         */
        private boolean singleDir;

        public FullScreenFileManager() throws FileTreeOrganizer.DirectoryNotCreatedException, FileTreeOrganizer.ListFilesError, NoParentFileException {
            fileOrga = FileTreeOrganizer.getSingletonInstance(getApplicationContext());
            singleDir = true;
            currentDir1 = fileOrga.getCurrentDirElement();
            updateDirElements();
        }

        /**
         * Change to the previous sibling directory if there is one. The array of shown elements
         * will contain the the elements of the directories one before the directories of the photos
         * shown before. Changes only this object's (FullScreenFileManager) state but not any views.
         * @return If there was a previous directory to move to. If false, nothing happened
         */
        public boolean backward() throws NoParentFileException, FileTreeOrganizer.ListFilesError {
            if(previousDir == null) { return false; }
            singleDir = false;
            //if singleDir is true: centerTransitionIndex is still lastIndex as set in getShownPhotoElements() (so that in the call of notifyItemRangeRemoved() in FullScreenAdapter nothing will be removed)
            currentDir1 = previousDir;
            updateDirElements();
            return true;
        }

        /**
         * Change to the next sibling directory if there is one. The array of shown elements
         * will contain the the elements of the directories one after the directories of the photos
         * shown before. Changes only this object's (FullScreenFileManager) state but not any views.
         * @return If there was a next directory to move to. If false, nothing happened
         */
        public boolean forward() throws NoParentFileException, FileTreeOrganizer.ListFilesError {
            if(singleDir) {
                if(currentDir2 == null) { return false; }
                singleDir = false;
                centerTransitionIndex = 0; // so that in the call of notifyItemRangeRemoved() in FullScreenAdapter nothing will be removed
                return true;
            }
            if (nextDir == null) { return false; }
            currentDir1 = currentDir2;
            updateDirElements();
            return true;
        }

        /**
         * Updates the other variables after currentDir1 was set by the calling method.
         */
        private void updateDirElements() throws NoParentFileException, FileTreeOrganizer.ListFilesError {
            DirViewElement[] elements;
            DocumentFile parent = currentDir1.getDirFile().getParentFile();
            if (parent == null) { throw new NoParentFileException(); }
            elements = fileOrga.generateDirElements(parent);
            for(int i = 0; i < elements.length; i++) {
                if(elements[i].dirEquals(currentDir1)){
                    if(i > 0) {
                        previousDir = elements[i-1];
                    }else{
                        previousDir = null;
                    }
                    if(i+1 < elements.length) {
                        currentDir2 = elements[i+1];
                        if(i+2 < elements.length) {
                            nextDir = elements[i+2];
                        }else{
                            nextDir = null;
                        }
                    }else{
                        currentDir2 = null;
                        nextDir = null;
                    }
                    break;
                }
            }
        }

        /**
         * Generates the array of PhotoViewElements to be shown. Between the elements of different
         * directories a null element is inserted and centerTransitionIndex is set to its index in
         * the array (to the last index if singleDir is true). The first and last elements are null, too.
         * @return The generated array
         */
        public PhotoViewElement[] getShownPhotoElements() throws FileTreeOrganizer.ListFilesError {
            if(singleDir || currentDir2 == null) {
                PhotoViewElement[] elements = fileOrga.generatePhotoViewElements(currentDir1.getDirFile());
                PhotoViewElement[] result = new PhotoViewElement[elements.length + 2];
                lastIndex = result.length - 1;
                centerTransitionIndex = lastIndex; // for FullScreenAdapter.onBindViewHolder() to set the correct directories
                result[0] = null;
                for (int i = 0; i < elements.length; i++) {
                    result[i+1] = elements[i];
                }
                result[lastIndex] = null;
                return result;
            }else{
                    return concat(fileOrga.generatePhotoViewElements(currentDir1.getDirFile()), fileOrga.generatePhotoViewElements(currentDir2.getDirFile()));
            }
        }

        /**
         * Returns a new array with null as first element, then the elements of a1, then a null
         * element in between, then the elements of a2 and finally a null element again.
         * @param a1 The array of PhotoViewElements with the elements to put at the beginning of the new array
         * @param a2 The array of PhotoViewElements with the elements to put at the end of the new array
         * @return The resulting new array
         */
        private PhotoViewElement[] concat(PhotoViewElement[] a1, PhotoViewElement[] a2) {
            lastIndex = a1.length + a2.length + 2;
            centerTransitionIndex = a1.length + 1;
            PhotoViewElement[] result = new PhotoViewElement[lastIndex + 1];
            result[0] = null;
            for(int i = 0; i < a1.length; i++) {
                result[i+1] = a1[i];
            }
            result[centerTransitionIndex] = null;
            for(int i = 0; i < a2.length; i++) {
                result[i+centerTransitionIndex+1] = a2[i];
            }
            result[lastIndex] = null;
            return result;
        }

        /**
         * @return The index of the null element of the array returned by getShownPhotoElements()
         * or the length of the array if singleDir is true.
         */
        public int getCenterTransitionIndex() {
            return centerTransitionIndex;
        }

        /**
         * @return The directory before currentDir1. Becomes currentDir1 when moving backward, if existent (not null).
         */
        public DirViewElement getPreviousDir() {
            return previousDir;
        }
        /**
         * @return The first directory whose photos are in the array of shown elements.
         */
        public DirViewElement getCurrentDir1() {
            return currentDir1;
        }
        /**
         * @return Next directory after currentDir1 (if existent, else null). Photos in there are within
         * the shown elements, if singleDir is false. Becomes currentDir1 when moving forward in this case.
         */
        public DirViewElement getCurrentDir2() {
            return currentDir2;
        }
        /**
         * @return Directory after currentDir2. Becomes currentDir2 when moving forward, if existent (not null) and singleDir is false.
         */
        public DirViewElement getNextDir() {
            return nextDir;
        }

    }
}
