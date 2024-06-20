package com.example.cameraxjavaactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;

import com.example.cameraxjavaactivity.camera.CameraServer;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    //按钮
    ImageButton takePhotoButton;
    ImageButton switchCameraButton;
    ImageButton newPhotoButton;
    //预览
    PreviewView previewView;
    View flashView;
    //权限
    private static final String[] REQUIRE_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    public static final int REQUEST_CODE_PERMISSIONS = 10;
    //录像
    VideoCapture videoCapture;
    Recording recording;
    //executor & imageAnalysis
    private CameraServer cameraServer;
    private ImageAnalysis imageAnalysis;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //绑定控件
        takePhotoButton = findViewById(R.id.takePhotoBtn);
        switchCameraButton = findViewById(R.id.switchCameraBtn);
        newPhotoButton = findViewById(R.id.newPhotoBtn);
        previewView = findViewById(R.id.preview_view);
        flashView = findViewById(R.id.flashView) ;
        takePhotoButton.setOnClickListener(v -> {
            setFlashViewAnimation();
            setButtonClickAnimation(v);
            cameraServer.takePhoto();
        });
        switchCameraButton.setOnClickListener(v -> {
            setButtonRscTurnAnimation(switchCameraButton);
        });
        //获取权限
        if (havePermissions()) {
            cameraServer = new CameraServer(this,previewView,this);
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
            cameraServer = new CameraServer(this,previewView,this);
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
    public  void setButtonRscTurnAnimation(ImageButton button){
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat( button, "rotation", 0f, 180f);
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
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                flashView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        flashView.startAnimation(fade);
    }
    //录像
    private void takeVideo() {
        if (videoCapture != null) {
            if (recording != null) {
                recording.stop();
                recording = null;
                return;
            }
            String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.SIMPLIFIED_CHINESE).format(System.currentTimeMillis()) + ".mp4";
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/CameraVideo");
            }
            MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions
                    .Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    .setContentValues(contentValues)
                    .build();
            Recorder recorder = (Recorder) videoCapture.getOutput();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, REQUIRE_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                return;
            }
            recording = recorder.prepareRecording(this, mediaStoreOutputOptions)
                    .withAudioEnabled()
                    .start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
                        if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                            switchCameraButton.setEnabled(true);
                        } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                            if (((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                                if (recording != null) {
                                    recording.close();
                                    recording = null;
                                }
                            } else {
                                String msg = "视频为" + ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                                Log.i("CameraXTest", msg);
                            }
                            switchCameraButton.setEnabled(true);
                            //takeVideoButton.setText(getString(R.string.startRecord));
                        }
                    });
        }
    }
}