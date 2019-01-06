package myapps.globenow;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.view.animation.AlphaAnimation;

class BackgroundAnimation {
    private int k_transitionTime = 100; // ms

    TransitionDrawable GenerateTransitionDrawable(Context context){
        Drawable backgroundArr[] = new Drawable[2];
        backgroundArr[0] = context.getDrawable(R.drawable.circlebright);
        if (backgroundArr[0] != null) {
            backgroundArr[0].setAlpha(0);
        }
        backgroundArr[1] = context.getDrawable(R.drawable.circledark);
        if (backgroundArr[1] != null) {
            backgroundArr[1].setAlpha(50);
        }
        return new TransitionDrawable(backgroundArr);
    }

    void RunAnimationCircleTransition(View v){
        TransitionDrawable td = (TransitionDrawable) v.getBackground();
        td.startTransition(k_transitionTime);
        td.reverseTransition(k_transitionTime);
    }

    void RunAnimationAlphaFlash(View v){
        AlphaAnimation animAlpha = new AlphaAnimation(1F, 0.1F);
        animAlpha.setDuration(k_transitionTime);
        v.startAnimation(animAlpha);
    }
}
