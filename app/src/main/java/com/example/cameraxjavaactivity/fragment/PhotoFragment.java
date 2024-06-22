package com.example.cameraxjavaactivity.fragment;

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

public class PhotoFragment extends Fragment {

    private CameraServer cameraServer;
    private AnimationUtil animation;
    ImageButton takePhotoButton;
    ImageButton switchCameraButton;
    ImageButton viewPhotoButton;
    //预览
    PreviewView previewView;
    View flashView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        PreviewView previewView = view.findViewById(R.id.preview_view);
        cameraServer = new CameraServer(requireContext(), previewView, (LifecycleOwner) getContext());
        animation = new AnimationUtil();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraServer.releaseCamera();
    }

    private void bindViewInit(View view){
        takePhotoButton = view.findViewById(R.id.takePhotoBtn);
        switchCameraButton = view.findViewById(R.id.switchCameraBtn);
        viewPhotoButton = view.findViewById(R.id.viewPhotoBtn);
        previewView = view.findViewById(R.id.preview_view);
        flashView = view.findViewById(R.id.flashView);
        takePhotoButton.setOnClickListener(v -> {
            animation.setFlashViewAnimation(v);
            animation.setButtonClickAnimation(v);
            cameraServer.takePhoto(viewPhotoButton);
        });
        switchCameraButton.setOnClickListener(v -> {
            animation.setButtonRscTurnAnimation(switchCameraButton);
            cameraServer.switchCamera();
        });
        viewPhotoButton.setOnClickListener(v -> {
            animation.setButtonClickAnimation(v);
            cameraServer.viewPhoto();
        });
    }
}
