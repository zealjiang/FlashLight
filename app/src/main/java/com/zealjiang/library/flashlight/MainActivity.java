package com.zealjiang.library.flashlight;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.ib_toggle)
    ImageButton ivToggle;

    private boolean flashOn = false;
    Camera mCamera;
    //6.0拍照权限申请码
    private final static int CAMERA_REQESTCODE = 100;
    private final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @OnClick(R.id.ib_toggle)
    void toggle() {
        if (flashOn) {
            flashOn = false;
            ivToggle.setImageResource(R.mipmap.off);

            try {
                Camera.Parameters mParameters;
                mParameters = mCamera.getParameters();
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(mParameters);
                mCamera.release();
                mCamera = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            flashOn = true;
            ivToggle.setImageResource(R.mipmap.on);
            requestPermissionCamera();

        }
    }

    /**
     * 获取相机
     *
     * @author zealjiang
     * @time 2016/9/21 15:38
     */
    private void getCamera() {
        if (mCamera == null) {
            //异步线程获取相机
            new OpenCameraTask().execute();
        } else {
            openFlash();
        }
    }

    /**
     * Asynchronous task for preparing the Camera open, since it's a long blocking
     * operation.
     */
    class OpenCameraTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // Create an instance of Camera
            mCamera = getCameraInstance();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            openFlash();
        }
    }

    private void openFlash() {
        if (null != mCamera) {
            Camera.Parameters mParameters;
            mParameters = mCamera.getParameters();
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(mParameters);
        }
    }


    //处理6.0动态权限问题
    private void requestPermissionCamera() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {//表明用户已经彻底禁止弹出权限请求
                    showMessageOKCancel("You need to allow access to Camera,请手动从设置里打开",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQESTCODE);
                                }
                            });
                    return;
                }
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQESTCODE);
                return;
            } else {
                getCamera();
            }
        } else {
            getCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQESTCODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCamera();
            } else {
                Toast.makeText(this, "需要允许拍照权限来打开闪光灯", Toast.LENGTH_LONG).show();
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void openPermission() {

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}
