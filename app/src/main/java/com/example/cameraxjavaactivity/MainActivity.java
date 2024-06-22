package com.example.cameraxjavaactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

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
import com.example.cameraxjavaactivity.fragment.PhotoFragment;
import com.example.cameraxjavaactivity.fragment.VideoFragment;
import com.example.cameraxjavaactivity.utils.AnimationUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

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
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //绑定控件
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.bringToFront();

        viewPager.setAdapter(new FragmentAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("拍照");
                    break;
                case 1:
                    tab.setText("录像");
                    break;
            }
        }).attach();
        //获取权限
        if (havePermissions()) {
            //cameraServer = new CameraServer(this, previewView, this);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRE_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            //cameraServer = new CameraServer(this, previewView, this);
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
    private static class FragmentAdapter extends FragmentStateAdapter {

        public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new PhotoFragment();
                case 1:
                    return new VideoFragment();
                default:
                    return new PhotoFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
