package com.fg.dji_media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.log.DJILog;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.media.DownloadListener;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKManager;

public class MainMediaActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static BaseProduct mProduct;
    private Handler mHandler;
    private List<MediaFile> mediaFileList = new ArrayList<MediaFile>();
    private MediaManager mMediaManager;
    private Camera camera;
    private Aircraft aircraft;
    private Button mediaFilesButton, downloadButton, startButton;
    private TextView mediafilesText;
    private int currentProgress;
    private File destDir;
    private ProgressDialog mDownloadDialog;
    private MediaFile mediaFile;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;



    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the compile and target version is higher than 22, please request the following permission at runtime to ensure the SDK works well.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////            checkAndRequestPermissions();
////        }

        setContentView(R.layout.activity_main_media);
        mPrefs = getSharedPreferences("label", 0);
        mEditor = mPrefs.edit();

        destDir = new File(Environment.getExternalStorageDirectory().getPath()+"/Media Files DJI/");

        mDownloadDialog = new ProgressDialog(this);
        mDownloadDialog.setTitle("Downloading files");
        mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadDialog.setCanceledOnTouchOutside(false);
        mDownloadDialog.setCancelable(true);
        mDownloadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mMediaManager != null) {
                    mMediaManager.exitMediaDownloading();
                }
            }
        });
        mediaFilesButton = findViewById(R.id.mediafilesBtn);
        downloadButton = findViewById(R.id.downloadBtn);
        startButton = findViewById(R.id.startBtn);
        mHandler = new Handler(Looper.getMainLooper());
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadButton.setClickable(false);
                mediaFilesButton.setClickable(false);

                while (isConnectionAvalable()){
                    try{
                        aircraft = getAircraftInstance();
                        if (aircraft != null) camera = aircraft.getCamera();
                        if (camera.isMediaDownloadModeSupported()) {
                            camera.setMode(SettingsDefinitions.CameraMode.MEDIA_DOWNLOAD, new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError error) {
                                    if (error == null) {
                                        DJILog.e(TAG, "Set cameraMode success");
                                        mMediaManager = camera.getMediaManager();
                                    } else {
                                        showToast("Set cameraMode failed");
                                    }
                                }
                            });
                        } else {
                            showToast("Media Manager is not supported");
                        }
                        mediaFileList = mMediaManager.getSDCardFileListSnapshot();
                        showToast(String.valueOf(mediaFileList.size()));
                        mediafilesText.setText(mediaFileList.size());
                        checkForNewestMedia();
                        downloadMedia();
                        Thread.sleep(2000);
                    }catch(Exception e){
                        showToast(e.toString());

                    }
                }
                downloadButton.setClickable(true);
                mediaFilesButton.setClickable(true);
            }
        });

//
//        try{
//            aircraft = getAircraftInstance();
//            if (aircraft != null) camera = aircraft.getCamera();
//            if (camera.isMediaDownloadModeSupported()) {
//                mMediaManager = camera.getMediaManager();
//            } else {
//                showToast("Media Manager is not supported");
//            }
//            mediaFileList = mMediaManager.getInternalStorageFileListSnapshot();
//            showToast(String.valueOf(mediaFileList.size()));
//            mediafilesText.setText(mediaFileList.size());
//        }catch(Exception e){
//            showToast(e.toString());
//
//        }
//        checkForNewestMedia();
//        downloadMedia();
        //Initialize DJI SDK Manager

        mediaFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    aircraft = getAircraftInstance();
                    if (aircraft != null) camera = aircraft.getCamera();
                    if (camera.isMediaDownloadModeSupported()) {
                        mMediaManager = camera.getMediaManager();
                    } else {
                        showToast("Media Manager is not supported");
                    }
                    mediaFileList = mMediaManager.getInternalStorageFileListSnapshot();
                    showToast(String.valueOf(mediaFileList.size()));
                    mediafilesText.setText(mediaFileList.size());
                }catch(Exception e){
                    showToast(e.toString());

                }

            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkForNewestMedia();
                downloadMedia();
            }
        });


    }
    private boolean isConnectionAvalable(){
        boolean a = false;
        try {
            a = aircraft.isConnected();
        }catch (Exception e){
            showToast(e.toString());
        }
        return a;

    }
    private void downloadMedia(){
        for (int i = 0; i< mediaFileList.size(); i++){

            mediaFile = mediaFileList.get(i);
            if (mediaFile.getTimeCreated() > mPrefs.getLong("Last", -1)) {
                mediaFile.fetchFileData(destDir, null, new DownloadListener<String>() {
                    @Override
                    public void onStart() {
                        currentProgress = -1;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                mDownloadDialog.incrementProgressBy(-mDownloadDialog.getProgress());
                                mDownloadDialog.setTitle(mediaFile.getFileName());
                                mDownloadDialog.show();
                            }
                        });
                    }

                    @Override
                    public void onRateUpdate(long total, long current, long l2) {
                        int tmpProgress = (int) (1.0 * current / total * 100);
                        if (tmpProgress != currentProgress) {
                            mDownloadDialog.setProgress(tmpProgress);
                            currentProgress = tmpProgress;
                        }
                    }

                    @Override
                    public void onProgress(long l, long l1) {

                    }

                    @Override
                    public void onSuccess(String s) {
                        if (null != mDownloadDialog && mDownloadDialog.isShowing()) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    mDownloadDialog.dismiss();
                                }
                            });
                        }
                        showToast("Files saved to: " + destDir);
                        currentProgress = -1;
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        if (null != mDownloadDialog && mDownloadDialog.isShowing()) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    mDownloadDialog.dismiss();
                                }
                            });
                        }
                        showToast("Download fail: " + djiError.getDescription());
                    }
                });
            }
        }

    }
    private void sortMedia(){
        Collections.sort(mediaFileList, Comparators.TIME);
        Collections.sort(mediaFileList, Comparators.NAME);
    }

    private void checkForNewestMedia(){
        sortMedia();
        for (int i = 0; i < mediaFileList.size(); i++){
            if (mPrefs.getLong("Last", -1) < mediaFileList.get(i).getTimeCreated()){
                mEditor.putLong("Last", mediaFileList.get(i).getTimeCreated());
            }
        }


    }
    private static class Comparators {
        private static final Comparator<MediaFile> NAME = (MediaFile o1, MediaFile o2) -> o1.getFileName().compareTo(o2.getFileName());
        private static final Comparator<MediaFile> TIME = (MediaFile o1, MediaFile o2) -> Long.compare(o1.getTimeCreated(), o2.getTimeCreated());
//        public static final Comparator<MediaFile> NAMEANDAGE = (MediaFile o1, MediaFile o2) -> NAME.thenComparing(TIME).compare(o1, o2);
    }



    public static synchronized BaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getProduct();
        }
        return mProduct;
    }
    public static synchronized Aircraft getAircraftInstance() {
        if (!(getProductInstance() != null && getProductInstance() instanceof Aircraft)) return null;
        return (Aircraft) getProductInstance();
    }

    public static synchronized Camera getCameraInstance() {

        if (getProductInstance() == null) return null;

        Camera camera = null;

        if (getProductInstance() instanceof Aircraft){
            camera = ((Aircraft) getProductInstance()).getCamera();

        } else if (getProductInstance() instanceof HandHeld) {
            camera = ((HandHeld) getProductInstance()).getCamera();
        }

        return camera;
    }

    private void showToast(final String toastMsg) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });

    }

}
