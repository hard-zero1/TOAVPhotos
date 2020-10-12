package hard_zero1.TOAVPhotos;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * A DialogFragment to confirm the deletion of a directory.
 */
public class ConfirmDeleteDirDialogFragment extends DialogFragment {
    private int pos;
    private String displayName;

    /**
     * Constructor for a DialogFragment to confirm the deletion of a directory.
     * @param pos The directory index
     * @param displayName The directory display name
     */
    public ConfirmDeleteDirDialogFragment(int pos, String displayName) {
        super();
        this.pos = pos;
        this.displayName = displayName;
    }

    /**
     * Do not use this constructor. It exists for recreation after being destroyed.
     */
    public ConfirmDeleteDirDialogFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            pos = savedInstanceState.getInt("pos");
            displayName = savedInstanceState.getString("displayName");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Resources res = getResources();
        return new AlertDialog.Builder(requireActivity())
                .setTitle(res.getString(R.string.confirm_delete_dialog_title))
                .setMessage(res.getString(R.string.confirm_delete_dialog_message_before_dirName) + displayName + res.getString(R.string.confirm_delete_dialog_message_after_dirName))
                .setPositiveButton(res.getString(R.string.confirm_delete_dialog_button_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity) requireActivity()).onDeleteDirectoryConfirmed(pos);
                    }
                }).setNegativeButton(res.getString(R.string.confirm_delete_dialog_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                }).create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Resources res = getResources();
        AlertDialog dialog = (AlertDialog) requireDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(res.getColor(R.color.colorDialogButtonText));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(res.getColor(R.color.colorDialogButtonText));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pos", pos);
        outState.putString("displayName", displayName);
    }
}
