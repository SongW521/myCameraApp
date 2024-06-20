package com.example.cameraxjavaactivity.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Rational;
import android.view.OrientationEventListener;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.cameraxjavaactivity.MainActivity;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// CameraController.java
public class CameraServer {
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private Recording recording;
    private ImageAnalysis imageAnalysis;
    private final ExecutorService cameraExecutor;
    private final PreviewView previewView;
    private final Context context;
    private final LifecycleOwner lifecycleOwner;


    ProcessCameraProvider processCameraProvider;

    ListenableFuture<ProcessCameraProvider> processCameraProviderListenableFuture;

    public CameraServer(Context context, PreviewView previewView, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.previewView = previewView;
        this.lifecycleOwner = lifecycleOwner;
        cameraExecutor = Executors.newSingleThreadExecutor();
        initCamera(this.lifecycleOwner);
    }


    /**
     * 初始化相机参数
     * */
    private void initCamera(LifecycleOwner lifecycleOwner) {
        // Camera initialization logic...
        //实例化（可以设置许多属性)
        imageCapture = new ImageCapture.Builder()
                //控制闪光灯模式
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .build();
        Recorder recorder = new Recorder.Builder().build();
        videoCapture = VideoCapture.withOutput(recorder);
        processCameraProviderListenableFuture = ProcessCameraProvider.getInstance(this.context);
        processCameraProviderListenableFuture.addListener(() -> {
            try {
                //配置预览(https://developer.android.google.cn/training/camerax/preview?hl=zh-cn)
                previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                //绑定到生命周期
                processCameraProvider = processCameraProviderListenableFuture.get();
                //图片分析
                initImageAnalysis();
                //设置旋转
                setOrientationEventListener();
                //剪裁矩形(拍摄之后，对图片进行裁剪）
                ViewPort viewPort = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    viewPort = new ViewPort.Builder(
                            new Rational(100, 100), previewView.getDisplay().getRotation()
                    ).build();
                } else {
                    viewPort = new ViewPort.Builder(
                            new Rational(100, 100), Surface.ROTATION_0
                    ).build();
                }
                UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                        .addUseCase(preview)
                        .addUseCase(imageAnalysis)
                        .addUseCase(imageCapture)
                        .addUseCase(videoCapture)
                        .setViewPort(viewPort)
                        .build();
                processCameraProvider.unbindAll();
                Camera camera = processCameraProvider.bindToLifecycle(this.lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, useCaseGroup);
//                //Camera
                //               setShotCut(1);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this.context));
    }

    /**
     * 图片分析
     * */
    @SuppressLint("UnsafeOptInUsageError")
    private void initImageAnalysis() {
        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
            imageProxy.close();
        });
    }

    /**
     * 设置旋转
     * */
    private void setOrientationEventListener() {
        OrientationEventListener orientationEventListener = new OrientationEventListener(this.context) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation;
                if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    rotation = Surface.ROTATION_90;
                } else {
                    rotation = Surface.ROTATION_0;
                }
//                Log.i("CameraXTest", String.valueOf(rotation));
//                Log.i("CameraXTest", String.valueOf(orientation));
                imageCapture.setTargetRotation(rotation);
            }
        };
        orientationEventListener.enable();
    }

    /**
     * 设置闪光灯模式
     * @param mode   0 -- auto  1 -- open    2 -- close
     * */
    public void setFlashMode(int mode) {
        imageCapture.setFlashMode(mode);
    }

    /**
     * 拍照功能
     * */
    public void takePhoto() {
        // Take photo logic...
        if (imageCapture != null) {
            //ContentValues
            String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.SIMPLIFIED_CHINESE).format(System.currentTimeMillis());
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CameraXImage");
            }
            //图片输出
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions
                    .Builder(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    .build();
            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this.context), new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Log.i("CameraXTest", Objects.requireNonNull(outputFileResults.getSavedUri()).toString());

                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.e("CameraXTest", exception.toString());
                }
            });
        }
    }

    /**
     * 录像功能
     * * @param mode   0 -- auto  1 -- open    2 -- close
     * */
    private void startRecording() {
        if (videoCapture != null) {
            String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.SIMPLIFIED_CHINESE).format(System.currentTimeMillis()) + ".mp4";
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/CameraVideo");
            }
            MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions
                    .Builder(context.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    .setContentValues(contentValues)
                    .build();
        }
    }

    private void stopRecording() {
        if (recording != null) {
            recording.stop();
            recording = null;
        }
    }
    /**
     * 切换镜头
     * */
    public void setShotCut(int mode){

        // 重新绑定用例
        if (processCameraProvider != null) {
            processCameraProvider.unbindAll();
            Camera camera = processCameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, imageCapture);
            CameraControl cameraControl = camera.getCameraControl();
            CameraInfo cameraInfo = camera.getCameraInfo();
        }

    }
    public void releaseCamera() {
        // Release camera resources...
        cameraExecutor.shutdown(); // 关闭线程池
    }

    // Other camera-related methods...
}

