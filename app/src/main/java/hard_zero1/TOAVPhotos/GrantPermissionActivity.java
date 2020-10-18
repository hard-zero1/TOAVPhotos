package hard_zero1.TOAVPhotos;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

/**
 * Activity that is shown when the file write permission is not granted. Asks for that permission
 * and finishes with RESULT_OK if the permission is granted.
 */
public class GrantPermissionActivity extends AppCompatActivity {

    private static final int REQUEST_FILE_PERMISSION = 1;
    private static final int REQUEST_TREE_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT < 21) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_FILE_PERMISSION);
        }
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
        if (Build.VERSION.SDK_INT >= 21) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_TREE_PERMISSION);
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_FILE_PERMISSION);
        }
    }

    @RequiresApi(api = 19) // Used only with at least 21
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TREE_PERMISSION && resultCode == RESULT_OK && data != null) {
            Uri dirUri = data.getData();
            Intent resultIntent = new Intent();
            resultIntent.setData(dirUri);
            setResult(RESULT_OK, resultIntent);
            getContentResolver().takePersistableUriPermission(dirUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            finish();
        }

    }
}
