package com.example.cameraxjavaactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;

import com.example.cameraxjavaactivity.camera.CameraServer;
import com.example.cameraxjavaactivity.utils.AnimationUtil;

public class MainActivity extends AppCompatActivity {
    //按钮
    ImageButton takePhotoButton;
    ImageButton switchCameraButton;
    ImageButton viewPhotoButton;
    //预览
    PreviewView previewView;
    View flashView;
    //权限
    public static final String[] REQUIRE_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    public static final int REQUEST_CODE_PERMISSIONS = 10;
    private CameraServer cameraServer;
    private AnimationUtil controllerAnimation;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        controllerAnimation = new AnimationUtil();
        //绑定控件
        takePhotoButton = findViewById(R.id.takePhotoBtn);
        switchCameraButton = findViewById(R.id.switchCameraBtn);
        viewPhotoButton = findViewById(R.id.viewPhotoBtn);
        previewView = findViewById(R.id.preview_view);
        flashView = findViewById(R.id.flashView);
        takePhotoButton.setOnClickListener(v -> {
            controllerAnimation.setFlashViewAnimation(flashView);
            controllerAnimation.setButtonClickAnimation(v);
            cameraServer.takePhoto(viewPhotoButton);
        });
        switchCameraButton.setOnClickListener(v -> {
            controllerAnimation.setButtonRscTurnAnimation(switchCameraButton);
            cameraServer.switchCamera();
        });
        viewPhotoButton.setOnClickListener(v -> {
            controllerAnimation.setButtonClickAnimation(v);
            cameraServer.viewPhoto();
        });

        //获取权限
        if (havePermissions()) {
            cameraServer = new CameraServer(this, previewView, this);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRE_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraServer.releaseCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            cameraServer = new CameraServer(this, previewView, this);
        } else {
            finish();
        }
    }

    //判断权限是否获取
    private boolean havePermissions() {
        for (String permission : REQUIRE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}