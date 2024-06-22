package com.example.cameraxjavaactivity.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.example.cameraxjavaactivity.R;
import com.example.cameraxjavaactivity.camera.CameraServer;
import com.example.cameraxjavaactivity.utils.AnimationUtil;

public class VideoFragment extends Fragment {
    private CameraServer cameraServer;
    private AnimationUtil animation;
    ImageButton takeVideoButton;
    ImageButton switchCameraButton;
    ImageButton viewVideoButton;
    //预览
    PreviewView previewView;

    View icon ;

    boolean isRecording = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        bindViewInit(view);
        cameraServer = new CameraServer(requireContext(), previewView, (LifecycleOwner) getContext());
        animation = new AnimationUtil();
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        cameraServer.initCamera(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraServer.unbindingCamera();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraServer.releaseCamera();
    }

    private void bindViewInit(View view){
        takeVideoButton = view.findViewById(R.id.takeVideoBtn);
        switchCameraButton = view.findViewById(R.id.video_switchBtn);
        viewVideoButton = view.findViewById(R.id.viewVideoBtn);
        previewView = view.findViewById(R.id.video_preview);
        icon = view.findViewById(R.id.icon);
        takeVideoButton.setOnClickListener(v -> {
            animation.setButtonClickAnimation(v);
            recordingCallback();
        });
        switchCameraButton.setOnClickListener(v -> {
            animation.setButtonRscTurnAnimation(switchCameraButton);
            cameraServer.switchCamera();
        });
        viewVideoButton.setOnClickListener(v -> {
            animation.setButtonClickAnimation(v);
            cameraServer.viewPhoto();
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void recordingCallback() {
        isRecording = !isRecording;
        if(isRecording){
            icon.setBackground(getResources().getDrawable(R.drawable.radius_red));
            cameraServer.startRecording();
        }
        else{
            icon.setBackground(getResources().getDrawable(R.drawable.circular_red));
            cameraServer.stopRecording();
        }
    }
}
