package hard_zero1.TOAVPhotos;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

/**
 * A DialogFragment to confirm the deletion of a photo.
 */
public class ConfirmDeletePhotoDialogFragment extends DialogFragment {
    ViewGroup dialogView;
    private int pos;
    private PhotoViewElement element;

    /**
     * Constructor for a DialogFragment to confirm the deletion of a photo.
     * @param pos The photo index
     * @param element The PhotoViewElement of the photo
     */
    public ConfirmDeletePhotoDialogFragment(int pos, PhotoViewElement element) {
        super();
        this.pos = pos;
        this.element = element;
    }

    /**
     * Do not use this constructor. It exists for recreation after being destroyed.
     */
    public ConfirmDeletePhotoDialogFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            pos = savedInstanceState.getInt("pos");
            element = (PhotoViewElement) savedInstanceState.getSerializable("element");
        }
        dialogView = (ViewGroup) requireActivity().getLayoutInflater().inflate(R.layout.confirm_delete_picture_dialog, null);
        Glide.with(requireActivity()).load(element.getFile().getUri()).into((ImageView) dialogView.findViewById(R.id.ivDeletePreview));
        ((TextView) dialogView.findViewById(R.id.tvDeletePhotoIdent)).setText(element.getInfoText());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Resources res = getResources();
        return new AlertDialog.Builder(requireActivity())
                .setView(dialogView)
                .setTitle(res.getString(R.string.confirm_delete_photo_title))
                .setMessage(res.getString(R.string.confirm_delete_photo_message))
                .setPositiveButton(res.getString(R.string.confirm_delete_photo_button_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity) requireActivity()).onDeletePhotoConfirmed(pos);
                    }
                }).setNegativeButton(res.getString(R.string.confirm_delete_photo_button_cancel), new DialogInterface.OnClickListener() {
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
        outState.putSerializable("element", element);
    }
}
