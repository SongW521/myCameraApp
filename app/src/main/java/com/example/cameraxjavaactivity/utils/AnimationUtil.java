package com.example.cameraxjavaactivity.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;

public class AnimationUtil {
    public void setButtonRscTurnAnimation(ImageButton button) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(button, "rotation", 0f, 180f);
        rotateAnimator.setDuration(300); // 设置动画持续时间
        rotateAnimator.start(); // 开始动画
    }

    public void setButtonClickAnimation(View view) {

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

    public void setFlashViewAnimation(View flashView) {
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
