package com.example.sergio.reencuadrar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class LoadScreen extends AppCompatActivity {
    private TextView aTextView;
    private ImageView anImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_screen);
        aTextView = (TextView) findViewById(R.id.txtLoadScreen);
        anImageView = (ImageView) findViewById(R.id.imagenLoadScreen);
        //Animacion de carga
        Animation myAnimation = AnimationUtils.loadAnimation(this, R.anim.mytransition);
        aTextView.startAnimation(myAnimation);
        anImageView.startAnimation(myAnimation);
        final Intent i = new Intent(this, PrincipalActivity.class);
        Thread t = new Thread() {
            public void run() {
                try {
                    sleep(4000);
                } catch (InterruptedException e) {
                    finish();
                } finally {
                    startActivity(i);
                    finish();
                }
            }
        };
        t.start();
    }
}
