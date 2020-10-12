package hard_zero1.TOAVPhotos;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * A DialogFragment to delete a given directory or rename it and/or move it to another position.
 */
public class DirOptionsDialogFragment extends DialogFragment implements CompoundButton.OnCheckedChangeListener {

    public enum Action {
        MOVE_OR_DELETE,
        MOVE,
        DELETE,
        MAKE_NEW
    }

    private ViewGroup dialogView;
    private Action action;
    private int min;
    private int max;
    private int initial;
    private int position;
    private String displayName;

    /**
     * Constructor for a DialogFragment to delete a given directory or rename it and/or move it to
     * another position.
     * @param action Must be MOVE_OR_DELETE (to delete or rename and/or move the folder to
     *               another position) or MAKE_NEW (to create a new folder)
     * @param min The minimum number that can be selected for the (new) folder position
     * @param max The maximum number that can be selected for the (new) folder position
     * @param initial The number preselected for the (new) folder position
     * @param position The current folder index
     * @param displayName The directory display name to show up in the EditText initially
     */
    public DirOptionsDialogFragment(Action action, int min, int max, int initial, int position, String displayName) {
        super();
        this.action = action;
        this.min = min;
        this.max = max;
        this.initial = initial;
        this.position = position;
        this.displayName = displayName;
    }

    /**
     * Do not use this constructor. It exists for recreation after being destroyed.
     */
    public DirOptionsDialogFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean deleteChecked = false;
        if (savedInstanceState != null) { // recreated from previous state
            action = (Action) savedInstanceState.getSerializable("action");
            min = savedInstanceState.getInt("min");
            max = savedInstanceState.getInt("max");
            initial = savedInstanceState.getInt("initial");
            position = savedInstanceState.getInt("position");
            displayName = savedInstanceState.getString("displayName");
            deleteChecked = savedInstanceState.getBoolean("deleteChecked");
        }

        dialogView = (ViewGroup) requireActivity().getLayoutInflater().inflate(R.layout.dir_options_dialog, null);
        if(action == Action.MAKE_NEW) {
            dialogView.removeView(dialogView.findViewById(R.id.cbDeleteDir));
        }else{
            CheckBox cbDelete = (CheckBox) dialogView.findViewById((R.id.cbDeleteDir));
            cbDelete.setOnCheckedChangeListener(this);
            cbDelete.setChecked(deleteChecked);
        }
        ((EditText) dialogView.findViewById(R.id.etNewDirName)).setText(displayName);
        NumberPicker picker = dialogView.findViewById(R.id.numberPicker);
        picker.setMinValue(min);
        picker.setMaxValue(max);
        picker.setValue(initial);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Resources res = getResources();
        return new AlertDialog.Builder(requireActivity())
                .setView(dialogView)
                .setTitle(action == Action.MAKE_NEW ? res.getString(R.string.change_dir_make_new_dialog_title) : res.getString(R.string.change_dir_dialog_title))
                .setMessage(action == Action.MAKE_NEW ? res.getString(R.string.change_dir_make_new_dialog_message) : res.getString(R.string.change_dir_dialog_message))
                .setPositiveButton(res.getString(R.string.change_dir_dialog_button_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog diag = requireDialog();
                        if(action == Action.MOVE_OR_DELETE) {
                            action = ((CheckBox) diag.findViewById(R.id.cbDeleteDir)).isChecked() ? Action.DELETE : Action.MOVE;
                        }
                        int newPosition = ((NumberPicker) diag.findViewById(R.id.numberPicker)).getValue();
                        if(action != Action.DELETE) {
                            displayName = ((EditText) diag.findViewById(R.id.etNewDirName)).getText().toString();
                        }
                        ((MainActivity) requireActivity()).onDirDialogFinished(action, displayName, position, newPosition);
                    }
                }).setNegativeButton(res.getString(R.string.change_dir_dialog_button_cancel), new DialogInterface.OnClickListener() {
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
            dialogView.findViewById(R.id.etNewDirName).setEnabled(false);
            dialogView.findViewById(R.id.numberPicker).setEnabled(false);
        }else{
            dialogView.findViewById(R.id.etNewDirName).setEnabled(true);
            dialogView.findViewById(R.id.numberPicker).setEnabled(true);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("action", action);
        outState.putInt("min", min);
        outState.putInt("max", max);
        outState.putInt("initial", dialogView == null ? initial : ((NumberPicker) dialogView.findViewById(R.id.numberPicker)).getValue());
        outState.putInt("position", position);
        outState.putString("displayName", dialogView == null ? displayName : ((EditText) dialogView.findViewById(R.id.etNewDirName)).getText().toString());
        outState.putBoolean("deleteChecked", (dialogView == null || action != Action.MOVE_OR_DELETE) ? false : ((CheckBox) dialogView.findViewById(R.id.cbDeleteDir)).isChecked());
    }
}
