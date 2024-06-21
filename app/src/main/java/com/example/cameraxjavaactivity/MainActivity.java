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
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //绑定控件
        takePhotoButton = findViewById(R.id.takePhotoBtn);
        switchCameraButton = findViewById(R.id.switchCameraBtn);
        viewPhotoButton = findViewById(R.id.viewPhotoBtn);
        previewView = findViewById(R.id.preview_view);
        flashView = findViewById(R.id.flashView);
        takePhotoButton.setOnClickListener(v -> {
            setFlashViewAnimation();
            setButtonClickAnimation(v);
            cameraServer.takePhoto(viewPhotoButton);
        });
        switchCameraButton.setOnClickListener(v -> {
            setButtonRscTurnAnimation(switchCameraButton);
            cameraServer.switchCamera();
        });
        viewPhotoButton.setOnClickListener(v -> {
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

    public void setButtonRscTurnAnimation(ImageButton button) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(button, "rotation", 0f, 180f);
        rotateAnimator.setDuration(300); // 设置动画持续时间
        rotateAnimator.start(); // 开始动画
    }

    private void setButtonClickAnimation(View view) {

        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0.7f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.7f);
        scaleDownX.setDuration(200);
        scaleDownY.setDuration(200);

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f);
        scaleUpX.setDuration(200);
        scaleUpY.setDuration(200);

        scaleDownX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleDownY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleUpX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleUpY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleDownX.start();
        scaleDownY.start();
        scaleDownX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scaleUpX.start();
                scaleUpY.start();
            }
        });
    }

    private void setFlashViewAnimation() {
        //设置拍照完成动画
        flashView.setVisibility(View.VISIBLE);
        AlphaAnimation fade = new AlphaAnimation(1, 0);
        fade.setDuration(300);
        fade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                flashView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        flashView.startAnimation(fade);
    }
}