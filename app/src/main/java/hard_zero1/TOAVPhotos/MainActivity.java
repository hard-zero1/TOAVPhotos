package hard_zero1.TOAVPhotos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.Toast;

import java.io.IOException;

import static androidx.recyclerview.widget.RecyclerView.HORIZONTAL;
import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final int REQUEST_PERMISSION_ACTIVITY = 1;
    private static final int REQUEST_TAKE_PHOTO = 0;

    private boolean created = false;

    private boolean verticalScrollOrientation;

    private FileTreeOrganizer fileOrga;
    private Resources res;

    private DirViewAdapter dirViewAdapter;

    private RecyclerView rvPhotos;
    private PhotoViewAdapter photoViewAdapter;

    /**
     * Deactivates the titlebar, checks for filesystem write permission. If permission is granted, calls onCreateWithPermission(), otherwise starts GrantPermissionActivity for result.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); // This call would crash if placed at the beginning of onCreateWithPermission() if coming from GrantPermissionActivity (via onActivityResult())
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(new Intent(this, GrantPermissionActivity.class), REQUEST_PERMISSION_ACTIVITY);
        }else{
            onCreateWithPermission();
        }
    }

    /**
     * Initializes instance variables, sets up the views of the activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void onCreateWithPermission() {
        setContentView(R.layout.activity_main);
        res = getResources();

        RecyclerView rvDirs = findViewById(R.id.rvDirs);
        rvDirs.setHasFixedSize(true);
        LinearLayoutManager DirViewLayManager = new LinearLayoutManager(this);
        rvDirs.setLayoutManager(DirViewLayManager);
        rvDirs.addItemDecoration(new DividerItemDecoration(this, DirViewLayManager.getOrientation()));

        rvPhotos = findViewById(R.id.rvPhotos); // RecyclerView
        rvPhotos.setHasFixedSize(true);
        PhotoViewLayoutManager photoViewLayManager = new PhotoViewLayoutManager(this);
        rvPhotos.setLayoutManager(photoViewLayManager);
        rvPhotos.addItemDecoration(new DividerItemDecoration(this, photoViewLayManager.getOrientation()));

        try {
            fileOrga = FileTreeOrganizer.getSingletonInstance(getApplicationContext());
            dirViewAdapter = new DirViewAdapter(this);
            photoViewAdapter = new PhotoViewAdapter(this, photoViewLayManager);
        } catch (FileTreeOrganizer.DirectoryNotCreatedException e) {
            Toast.makeText(this, getResources().getString(R.string.error_initial_dir_not_created), Toast.LENGTH_SHORT).show();
            finish();
            return;
        } catch (FileTreeOrganizer.ListFilesError listFilesError) {
            Toast.makeText(this, getResources().getString(R.string.error_list_dirs), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvDirs.setAdapter(dirViewAdapter);
        rvPhotos.setAdapter(photoViewAdapter);
        rvPhotos.setOnTouchListener(photoViewAdapter);

        SwitchCompat swScrollOrientation = findViewById(R.id.swScrollOrientation);
        swScrollOrientation.setOnCheckedChangeListener(this); // should be after the LayoutManager of rvPhotos is set
        onCheckedChanged(swScrollOrientation, swScrollOrientation.isChecked());

        created = true;
    }

    /**
     * Refreshes everything when the
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (created) {
            refreshAll();
        }
    }

    /**
     * Starts a FullScreenPhotoActivity to initially show the photo that was clicked on.
     * Called by the PhotoViewAdapter.PhotoViewHolder that holds the corresponding photoView when it is clicked.
     * @param pos The position/index of the photoView(Holder) within the recyclerview.
     */
    public void onPhotoClick(int pos) {
        Intent intent = new Intent(this, FullScreenPhotoActivity.class);
        intent.putExtra(FullScreenPhotoActivity.POSITION_TO_SHOW, pos);
        intent.putExtra(FullScreenPhotoActivity.SCROLL_VERTICAL, verticalScrollOrientation);
        startActivity(intent);
    }

    /**
     * Tells the FileTreeOrganizer to enter the directory that was clicked on and refreshes the activities views.
     * Called by the DirViewAdapter.DirViewViewHolder that holds the corresponding element when it is clicked.
     * @param pos The position/index within the recyclerview of the directory that was clicked on.
     */
    public void onDirClick(int pos) { // click on RecyclerView item (directory)
        try {
            fileOrga.cdEnter(pos);
        } catch (FileTreeOrganizer.ListFilesError listFilesError) {
            Toast.makeText(this, getResources().getString(R.string.error_list_dirs), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        dirViewAdapter.refreshDirView();
        photoViewAdapter.refreshPhotoView();
    }

    /**
     * Opens a dialog with options for the directory that was long clicked on.
     * Called by the DirViewAdapter.DirViewViewHolder that holds the corresponding element when it is clicked.
     * @param pos The position/index within the recyclerview of the directory that was clicked on.
     */
    public void onDirLongClick(int pos) {
        DirViewElement elem = fileOrga.getShownDirElements()[pos];
        int number = elem.getNumber();
        String displayName = elem.getDisplayName();
        int newDirCount = fileOrga.getCurrentDirCounter() + (number == 0 ? 1 : 0); // If the element had number 0 before, it will be a new sorted element. Otherwise the number of sorted elements stays the same.
        DirOptionsDialogFragment frag = new DirOptionsDialogFragment(DirOptionsDialogFragment.Action.MOVE_OR_DELETE, 1, newDirCount, number, pos, displayName);
        frag.show(getSupportFragmentManager(), "dirOptionsDialog");
    }

    /**
     * Opens a dialog with options for the photo that was long clicked on.
     * Called by the PhotoViewAdapter.PhotoViewHolder that holds the corresponding photoView when it is clicked.
     * @param pos The position/index within the recyclerview of the directory that was clicked on.
     */
    public void onPhotoLongClick(int pos) {
        PhotoViewElement elem = fileOrga.getShownPhotoElements()[pos];
        int number = elem.getNumber();
        int newPhotoCount = fileOrga.getCurrentPhotoCounter() + (number == 0 ? 1 : 0);
        PhotoOptionsDialogFragment frag = new PhotoOptionsDialogFragment(1, newPhotoCount, number, pos, elem);
        frag.show(getSupportFragmentManager(), "photoOptionsDialog");
    }

    /**
     * Tells the FileTreeOrganizer to move to the parent directory and refreshes the activities views.
     * @param v Ignored
     */
    public void onBtnUpClick(View v) {
        try {
            fileOrga.cdUP();
        } catch (FileTreeOrganizer.ListFilesError listFilesError) {
            Toast.makeText(this, getResources().getString(R.string.error_list_dirs), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        dirViewAdapter.refreshDirView();
        photoViewAdapter.refreshPhotoView();
    }

    /**
     * Opens a dialog to create a new directory.
     * @param v Ignored
     */
    public void onBtnNewDirClick(View v) {
        int newDirCount = fileOrga.getCurrentDirCounter() + 1;
        DirOptionsDialogFragment frag = new DirOptionsDialogFragment(DirOptionsDialogFragment.Action.MAKE_NEW, 1, newDirCount, newDirCount, 0, "");
        frag.show(getSupportFragmentManager(), "numberSelectDialog");
    }

    /**
     * Called when the dialog with directory options is finished by clicking ok. Calls the right method to perform the task specified by the user in the dialog.
     * @param action Must be MOVE, DELETE or MAKE_NEW
     * @param displayName The displayed name of the directory
     * @param oldPosition The index of the directory before the action (ignored if action is MAKE_NEW)
     * @param newPosition The number the directory should have after the action (ignored if action is DELETE)
     */
    public void onDirDialogFinished(DirOptionsDialogFragment.Action action, String displayName, int oldPosition, int newPosition) {
        switch (action){
            case MAKE_NEW:
                onMakeDirectory(newPosition, displayName);
                break;
            case MOVE:
                onDirMoveSpecified(oldPosition, newPosition, displayName);
                break;
            case DELETE:
                onDeleteDirectoryRequest(oldPosition, displayName);
        }
    }

    /**
     * Creates a new directory and updates the directory list.
     * @param position The number the new directory should have (other directories are shifted if not the highest number)
     * @param displayName The name that should be displayed for the directory
     */
    public void onMakeDirectory(int position, String displayName) {
        try {
            if(!fileOrga.mkDir(displayName, position)) {
                Toast.makeText(this, res.getString(R.string.error_dir_not_created), Toast.LENGTH_SHORT).show();
            }
        } catch (FileTreeOrganizer.RenameFailedException e) {
            Toast.makeText(this, res.getString(R.string.error_shift_rename_to_create_dir_failed), Toast.LENGTH_SHORT).show();
        } catch (FileTreeOrganizer.ListFilesError listFilesError) {
            Toast.makeText(this, getResources().getString(R.string.error_list_dirs), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        dirViewAdapter.refreshDirView();
    }

    /**
     * Moves the directory at oldPos to the position with newNumber
     * @param oldPos The index of the directory before
     * @param newNumber The new number of the directory
     * @param newDisplayName The new name to be displayed for the directory
     */
    public void onDirMoveSpecified(int oldPos, int newNumber, String newDisplayName) {
        try {
            fileOrga.moveDir(oldPos, newNumber, newDisplayName);
        }catch (FileTreeOrganizer.RenameFailedException ex) {
            Toast.makeText(this, res.getString(R.string.error_dir_rename_to_move_dir_failed), Toast.LENGTH_SHORT).show();
        } catch (FileTreeOrganizer.ListFilesError listFilesError) {
            Toast.makeText(this, getResources().getString(R.string.error_list_dirs), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        dirViewAdapter.refreshDirView();
    }

    /**
     * Opens a dialog to confirm deletion of the directory at index position.
     * @param position The index of the directory to be deleted
     * @param displayName The displayed name of the directory to be deleted
     */
    public void onDeleteDirectoryRequest(int position, String displayName) {
        new ConfirmDeleteDirDialogFragment(position, displayName).show(getSupportFragmentManager(), "confirmDeleteDirDialog");
    }
    /**
     * Deletes the directory at index position. Called on confirmation of the deletion in the dialog.
     * @param position The index of the directory to delete
     */
    public void onDeleteDirectoryConfirmed(int position) {
        try {
            if(!fileOrga.deleteDir(position)) {
                Toast.makeText(this, res.getString(R.string.error_dir_not_deleted), Toast.LENGTH_SHORT).show();
            }
        } catch (FileTreeOrganizer.RenameFailedException e) {
            Toast.makeText(this, res.getString(R.string.error_shift_rename_after_dir_deletion_failed), Toast.LENGTH_SHORT).show();
        } catch (FileTreeOrganizer.ListFilesError listFilesError) {
            Toast.makeText(this, getResources().getString(R.string.error_list_dirs), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        dirViewAdapter.refreshDirView();
    }

    /**
     * Moves or deletes a photo according to the result of the photo options dialog.
     * @param delete If the photo should be deleted
     * @param pos The index of the photo
     * @param newPos The new new number of the photo (ignored if delete is true)
     */
    public void onPhotoDialogFinished(boolean delete, int pos, int newPos) {
        if (delete) {
            new ConfirmDeletePhotoDialogFragment(pos, fileOrga.getShownPhotoElements()[pos]).show(getSupportFragmentManager(), "confirmDeletePhotoDialog");
        }else{
            try {
                fileOrga.movePhoto(pos, newPos);
            }catch (FileTreeOrganizer.RenameFailedException ex) {
                Toast.makeText(this, res.getString(R.string.error_photo_rename_to_move_photo_failed), Toast.LENGTH_SHORT).show();
            } catch (FileTreeOrganizer.ListFilesError listFilesError) {
                Toast.makeText(this, getResources().getString(R.string.error_list_dirs), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            photoViewAdapter.refreshPhotoView();
        }
    }

    /**
     * Deletes the photo at index position. Called on confirmation of the deletion in the dialog.
     * @param position The index of the photo to delete
     */
    public void onDeletePhotoConfirmed(int position) {
        try {
            if(!fileOrga.deletePhoto(position)) {
                Toast.makeText(this, res.getString(R.string.error_photo_not_deleted), Toast.LENGTH_SHORT).show();
            }
        } catch (FileTreeOrganizer.RenameFailedException ex) {
            Toast.makeText(this, res.getString(R.string.error_shift_rename_after_photo_deletion_failed), Toast.LENGTH_SHORT).show();
        } catch (FileTreeOrganizer.ListFilesError listFilesError) {
            Toast.makeText(this, getResources().getString(R.string.error_list_dirs), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        photoViewAdapter.refreshPhotoView();
    }

    /**
     * Refreshes everything
     * @param v Ignored
     */
    public void onBtnRefreshClick(View v) {
        refreshAll();
    }

    /**
     * Tells the FileTreeOrganizer to refresh its data and refreshes the activities views.
     */
    private void refreshAll(){
        try {
            fileOrga.refresh();
        } catch (FileTreeOrganizer.ListFilesError listFilesError) {
            Toast.makeText(this, getResources().getString(R.string.error_list_dirs), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        dirViewAdapter.refreshDirView();
        photoViewAdapter.refreshPhotoView();
    }

    /**
     * Changes the scroll directions according to the the switches state.
     * Called on change of the switches state.
     * @param buttonView Ignored
     * @param isChecked If the switch is checked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { // Callback of the scroll direction switch
        verticalScrollOrientation = !isChecked;
        ((PhotoViewLayoutManager) rvPhotos.getLayoutManager()).setOrientation(isChecked ? HORIZONTAL : VERTICAL);
        ((DividerItemDecoration) rvPhotos.getItemDecorationAt(0)).setOrientation(isChecked ? HORIZONTAL : VERTICAL);
    }

    /**
     * Starts the TakePhotoActivity. Called on click on the corresponding button.
     * If the API level is below 21, an activity with an ACTION_IMAGE_CAPTURE Intent is called if
     * possible. In this case, onActivityResult() will be called after that activity finished.
     * @param v Ignored
     */
    public void onBtnCameraClick(View v) {
        if(Build.VERSION.SDK_INT >= 21) {
            startActivity(new Intent(this, TakePhotoActivity.class));
        }else{
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
                FileTreeOrganizer.PreparedSaveFile saveFileResult;
                try {
                    saveFileResult = fileOrga.prepareSaveFile();
                }catch (IOException ex){
                    Toast.makeText(this, res.getString(R.string.error_IO_createPhotoFile), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(saveFileResult.alreadyExisted()) {
                    Toast.makeText(this, res.getString(R.string.warning_file_already_existed), Toast.LENGTH_SHORT).show();
                }
                Uri saveFileUri = FileProvider.getUriForFile(this,"hard_zero1.TOAVPhotos.fileprovider", saveFileResult.getSaveFile());
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, saveFileUri);
                takePhotoIntent.setClipData(ClipData.newRawUri("", saveFileUri));
                takePhotoIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
            }else{
                Toast.makeText(MainActivity.this, res.getString(R.string.error_no_camera_app), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Called if GrantPermissionActivity or the external activity to take a photo finish.
     * Calls onCreateWithPermission() if the GrantPermissionActivity finishes with the file
     * permission granted and finishes if not granted.
     * If the activity to take the photo finishes, the file prepared for the photo is either
     * cancelled or confirmed, depending on the success of the activity.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_TAKE_PHOTO){
            if(resultCode != RESULT_CANCELED){
                try {
                    fileOrga.confirmSaveFile();
                } catch (FileTreeOrganizer.ListFilesError listFilesError) {
                    Toast.makeText(this, res.getString(R.string.error_list_dirs), Toast.LENGTH_SHORT).show();
                }
                photoViewAdapter.refreshPhotoView();
            }else{
                Toast.makeText(MainActivity.this, res.getString(R.string.error_photo_activity), Toast.LENGTH_SHORT).show();
                try {
                    fileOrga.cancelSaveFile();
                } catch (FileTreeOrganizer.PhotoFileNotDeletedException e) {
                    Toast.makeText(this, res.getString(R.string.error_taking_photo_and_deleting), Toast.LENGTH_SHORT).show();
                }
            }
        }else if(requestCode == REQUEST_PERMISSION_ACTIVITY && resultCode == RESULT_OK){
            onCreateWithPermission();
        }else{
            finish();
        }
    }
}
