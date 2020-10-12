package hard_zero1.TOAVPhotos;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.experimental.UseExperimental;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalUseCaseGroup;
import androidx.camera.core.ExposureState;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.util.Range;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;

import static android.view.Surface.ROTATION_0;

@RequiresApi(api = 21 /*android.os.Build.VERSION_CODES.LOLLIPOP*/)
@UseExperimental(markerClass = androidx.camera.core.ExperimentalExposureCompensation.class)
public class TakePhotoActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private Resources res;
    private FileTreeOrganizer fileOrga;

    private ImageCapture imageCapture;
    private Preview preview;
    private CameraControl camControl;
    private CameraInfo camInfo;

    private SeekBar sbZoom;

    private DisplayOrientationListener orientListener;
    private ScaleGestureDetector scaleDetect;
    private boolean scalingEnd = true;
    private boolean cameraRunning = false;
    private boolean created = false;

    /**
     * Sets fullscreen mode, checks camera permission. Calls onCreateWithPermission() if
     * granted, requests the permission otherwise.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        res = getResources();
        try {
            fileOrga = FileTreeOrganizer.getSingletonInstance(this);
        } catch (FileTreeOrganizer.DirectoryNotCreatedException e) {
            Toast.makeText(this, getResources().getString(R.string.error_initial_dir_not_created), Toast.LENGTH_SHORT).show();
            finish();
            return;
        } catch (FileTreeOrganizer.ListFilesError listFilesError) {
            Toast.makeText(this, getResources().getString(R.string.error_list_dirs), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }else{
            onCreateWithPermission();
        }
    }

    /**
     * Calls onCreateWithPermission() and onResumeWithPermission() if camera permission is granted,
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            onCreateWithPermission();
        }else {
            finish();
        }
    }

    private void onCreateWithPermission() {
        setContentView(R.layout.activity_take_photo);
        startCamera();
        orientListener = new DisplayOrientationListener();
        created = true;
    }

    /**
     * Can be called without camera permission, when nothing is initialized and will be called
     * again after onRequestPermissionResult() when permission is granted.
     * If activity is set up: updates the views as the orientation could have changed and registers a listener for orientation changes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(created) {
            ((DisplayManager) getSystemService(DISPLAY_SERVICE)).registerDisplayListener(orientListener, null);
            updateViews();
        }
    }

    /**
     * Unregisters the DisplayListener as orientation changes are not important when the activity is stopped.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if(created) {
            ((DisplayManager) getSystemService(DISPLAY_SERVICE)).unregisterDisplayListener(orientListener); // Looks like otherwise the listener is called even if the app is not visible
        }
    }

    /**
     * Starts the initialization of the camera and the activity's views.
     */
    private void startCamera() { // Based on the code from https://codelabs.developers.google.com/codelabs/camerax-getting-started/
        PreviewView viewFinder = findViewById(R.id.pvViewFinder);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            // Used to bind the lifecycle of cameras to the lifecycle owner
            ProcessCameraProvider cameraProvider;
            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (Exception e) {
                Toast.makeText(this, res.getString(R.string.exception_getting_cam_provider), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Preview
            preview = (new Preview.Builder()).setTargetAspectRatio(AspectRatio.RATIO_16_9).build();
            preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

            imageCapture = (new ImageCapture.Builder()).setTargetAspectRatio(AspectRatio.RATIO_16_9).build();

            // Select back camera as a default
            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind use cases to camera
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                camControl = camera.getCameraControl();
                camInfo = camera.getCameraInfo();

            } catch(Exception ex) {
                Toast.makeText(this, res.getString(R.string.error_using_camera), Toast.LENGTH_SHORT).show();
            }


            // Register the listeners not before the camera is running, because the listeners assume a running camera (onCreate() finishes before this is executed)
            cameraRunning = true;
            setupCameraControls();

        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Takes a photo and saves it to a file prepared by FileTreeOrganizer.prepareSaveFile().
     * Called on click on the corresponding button.
     * @param v Ignored
     */
    public void onTakePhoto(View v) {  // Based on the code from https://codelabs.developers.google.com/codelabs/camerax-getting-started/
        if( !cameraRunning) { return; }

        v.setBackgroundColor(res.getColor(R.color.colorShutterButtonActive));

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

        // Create output options object which contains file + metadata
        ImageCapture.OutputFileOptions outputOptions = (new ImageCapture.OutputFileOptions.Builder(saveFileResult.getSaveFile())).build();

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture( outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onError(@NonNull ImageCaptureException exc) {
                String errMessage = res.getString(R.string.error_taking_photo);
                try {
                    fileOrga.cancelSaveFile();
                } catch (FileTreeOrganizer.PhotoFileNotDeletedException e) {
                    errMessage = res.getString(R.string.error_taking_photo_and_deleting);
                }
                Toast.makeText(TakePhotoActivity.this, errMessage, Toast.LENGTH_SHORT).show();
                v.setBackgroundColor(res.getColor(R.color.colorPrimary));
            }
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                try {
                    fileOrga.confirmSaveFile();
                } catch (FileTreeOrganizer.ListFilesError listFilesError) {
                    Toast.makeText(TakePhotoActivity.this, res.getString(R.string.error_list_dirs), Toast.LENGTH_SHORT).show();
                }
                v.setBackgroundColor(res.getColor(R.color.colorPrimary));
            }
        });
    }

    /**
     * Sets up the views for camera control. Should not be called when the camera is not running
     * (cameraRunning should be true) because the listeners of the views assume the camera
     * is running.
     */
    private void setupCameraControls() {
        scaleDetect = new ScaleGestureDetector(this, new ScaleGestureListener());
        /*PreviewView*/findViewById(R.id.pvViewFinder).setOnTouchListener(this);

        SeekBar sbBrightness = findViewById(R.id.sbBrightness);
        ExposureState es = camInfo.getExposureState();
        sbBrightness.setMax(es.getExposureCompensationRange().getUpper() - es.getExposureCompensationRange().getLower());
        sbBrightness.setProgress(es.getExposureCompensationIndex() - es.getExposureCompensationRange().getLower());
        sbBrightness.setOnSeekBarChangeListener(new OnBrightnessSlideListener());

        sbZoom = findViewById(R.id.sbZoom); // SeekBar
        sbZoom.setProgress((int)(camInfo.getZoomState().getValue().getLinearZoom() * 100));
        sbZoom.setOnSeekBarChangeListener(new OnZoomSlideListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // only viewfinder is registered
        scaleDetect.onTouchEvent(event);
        if(event.getAction() == MotionEvent.ACTION_UP) {
            if(scalingEnd) {
                scalingEnd = false;
                return true;
            }
            MeteringPointFactory factory = ((PreviewView) v).getMeteringPointFactory();
            MeteringPoint p = factory.createPoint(event.getX(), event.getY());
            FocusMeteringAction metering = (new FocusMeteringAction.Builder(p, FocusMeteringAction.FLAG_AF | FocusMeteringAction.FLAG_AE | FocusMeteringAction.FLAG_AWB)).build(); // All Flags (AF, AE, AWB) are default
            camControl.startFocusAndMetering(metering);
        }
        return true;
    }

    @UseExperimental(markerClass = ExperimentalUseCaseGroup.class)
    private void updateViews() {
        Display display;
        if(Build.VERSION.SDK_INT >= 30) {
            display = getDisplay();
        }else {
            display = getWindowManager().getDefaultDisplay();
        }

        int layoutID = -1;
        switch (display.getRotation()) {
            case ROTATION_0:
                layoutID = R.layout.take_photo_controls_0;
                break;
            case Surface.ROTATION_180:
                layoutID = R.layout.take_photo_controls_180;
                break;
            case Surface.ROTATION_270:
                layoutID = R.layout.take_photo_controls_270;
                break;
            case Surface.ROTATION_90:
                layoutID = R.layout.take_photo_controls_90;
                break;
        }

        TransitionManager.go(Scene.getSceneForLayout(findViewById(R.id.flRootTakePhotoControls), layoutID, this), new Fade().removeTarget(R.id.pvViewFinder));

        if (cameraRunning) {
            setupCameraControls();
            preview.setTargetRotation(display.getRotation());
            imageCapture.setTargetRotation(display.getRotation());
        }
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            ZoomState zs = camInfo.getZoomState().getValue();
            float newRatio = zs.getZoomRatio() * detector.getScaleFactor();
            camControl.setZoomRatio(Math.max(Math.min(newRatio, zs.getMaxZoomRatio()), zs.getMinZoomRatio()));
            sbZoom.setProgress((int)(camInfo.getZoomState().getValue().getLinearZoom() * 100));
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) { scalingEnd = true; }
    }

    private class OnBrightnessSlideListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(!fromUser) { return; }
            Range<Integer> range = camInfo.getExposureState().getExposureCompensationRange();
            int newIndex = progress + range.getLower();
            if (newIndex <= range.getUpper()) {
                camControl.setExposureCompensationIndex(newIndex);
            }
            seekBar.setMax(range.getUpper() - range.getLower());
            seekBar.setProgress(camInfo.getExposureState().getExposureCompensationIndex() - range.getLower());
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }
    private class OnZoomSlideListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(!fromUser) { return; }
            camControl.setLinearZoom(((float)progress) / 100);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

    private class DisplayOrientationListener implements DisplayManager.DisplayListener {
        @Override
        public void onDisplayAdded(int displayId) { }
        @Override
        public void onDisplayRemoved(int displayId) { }
        @Override
        public void onDisplayChanged(int displayId) {
            updateViews();
        }
    }
}