package hard_zero1.TOAVPhotos;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

/**
 * A DialogFragment to delete a given photo or move it to another position.
 */
public class PhotoOptionsDialogFragment extends DialogFragment implements CompoundButton.OnCheckedChangeListener {
    private ViewGroup dialogView;
    private int min;
    private int max;
    private int initial;
    private int position;
    private PhotoViewElement element;

    /**
     * Constructor for a DialogFragment to delete a given photo or move it to another position.
     * @param min The minimum number that can be selected for the new photo position
     * @param max The maximum number that can be selected for the new photo position
     * @param initial The number preselected for the new photo position
     * @param position The current photo index
     * @param element The PhotoViewElement of the photo
     */
    public PhotoOptionsDialogFragment(int min, int max, int initial, int position, PhotoViewElement element) {
        super();
        this.min = min;
        this.max = max;
        this.initial = initial;
        this.position = position;
        this.element = element;
    }

    /**
     * Do not use this constructor. It exists for recreation after being destroyed.
     */
    public PhotoOptionsDialogFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean deleteChecked = false;
        if (savedInstanceState != null) { // recreated from previous state
            min = savedInstanceState.getInt("min");
            max = savedInstanceState.getInt("max");
            initial = savedInstanceState.getInt("initial");
            position = savedInstanceState.getInt("position");
            element = (PhotoViewElement) savedInstanceState.getSerializable("element");
            deleteChecked = savedInstanceState.getBoolean("deleteChecked");
        }
        dialogView = (ViewGroup) requireActivity().getLayoutInflater().inflate(R.layout.photo_options_dialog, null);
        CheckBox cbDelete = dialogView.findViewById((R.id.cbDeletePhoto));
        cbDelete.setOnCheckedChangeListener(this);
        cbDelete.setChecked(deleteChecked);

        ((TextView) dialogView.findViewById(R.id.tvPhotoIdentText)).setText(element.getInfoText());
        Glide.with(requireActivity()).load(element.getFile().getUri()).into((ImageView) dialogView.findViewById(R.id.ivPreview));
        NumberPicker picker = dialogView.findViewById(R.id.photoNumberPicker);
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
                .setTitle(res.getString(R.string.photo_options_dialog_title))
                .setMessage(res.getString(R.string.photo_options_dialog_message))
                .setPositiveButton(res.getString(R.string.photo_options_dialog_button_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog diag = requireDialog();
                        boolean delete = ((CheckBox) diag.findViewById(R.id.cbDeletePhoto)).isChecked();
                        int newPosition = ((NumberPicker) diag.findViewById(R.id.photoNumberPicker)).getValue();
                        ((MainActivity) requireActivity()).onPhotoDialogFinished(delete, position, newPosition);
                    }
                }).setNegativeButton(res.getString(R.string.photo_options_dialog_button_cancel), new DialogInterface.OnClickListener() {
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
            dialogView.findViewById(R.id.photoNumberPicker).setEnabled(false);
        }else{
            dialogView.findViewById(R.id.photoNumberPicker).setEnabled(true);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("min", min);
        outState.putInt("max", max);
        outState.putInt("initial", dialogView == null ? initial : ((NumberPicker) dialogView.findViewById(R.id.photoNumberPicker)).getValue());
        outState.putInt("position", position);
        outState.putSerializable("element", element);
        outState.putBoolean("deleteChecked", dialogView == null ? false : ((CheckBox) dialogView.findViewById(R.id.cbDeletePhoto)).isChecked());
    }
}
