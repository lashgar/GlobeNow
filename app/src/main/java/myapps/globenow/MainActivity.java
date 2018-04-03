package myapps.globenow;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class MainActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextSwitcher glbnow = (TextSwitcher) findViewById(R.id.textSwitcherGLBNOW);
        glbnow.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // TODO Auto-generated method stub
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(MainActivity.this);
                myText.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(20);
                myText.setTextColor(Color.argb(0xFF, 0xD5, 0, 0));
                myText.setTypeface(Typeface.create("sans-serif-smallcaps", Typeface.BOLD));
                myText.setShadowLayer(5, 0, 0, Color.BLACK);
                myText.setPadding(0, 0, 0, 0);
                return myText;
            }
        });
        Animation in = AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);
        glbnow.setInAnimation(in);
        glbnow.setOutAnimation(out);
        glbnow.setText("Globe Now!");

        /*
        final ImageView imvt = (ImageView) findViewById(R.id.imageViewt);
        final ImageView imvb = (ImageView) findViewById(R.id.imageViewb);
        final TranslateAnimation animationMoveToTop = new TranslateAnimation(imvt.getX(), 0, imvt.getY(), 0);
        animationMoveToTop.setDuration(SPLASH_DISPLAY_LENGTH/8);
        animationMoveToTop.setFillAfter(true);
        */
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                glbnow.setText("");
                // imvt.startAnimation(animationMoveToTop);
            }
        }, 7*SPLASH_DISPLAY_LENGTH/8);
        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(MainActivity.this, Main2Activity.class);
                MainActivity.this.startActivity(mainIntent);
                MainActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);

        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
    }

}
