package hard_zero1.TOAVPhotos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

/**
 * Activity that is shown when the file write permission is not granted. Asks for that permission
 * and finishes with RESULT_OK if the permission is granted.
 */
public class GrantPermissionActivity extends AppCompatActivity {

    private static final int REQUEST_FILE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_FILE_PERMISSION);
        setContentView(R.layout.activity_grant_permission);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_FILE_PERMISSION && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    public void onBtnRequestPermissionClick(View v) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_FILE_PERMISSION);
    }
}
