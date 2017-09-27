package com.example.dell.camera;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * Created by ZXH on 2017/9/13.
 */
public class DisplayViewActivity extends AppCompatActivity {

    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allview);
        Intent intent = getIntent();
        imageView = (ImageView) findViewById(R.id.allview);
        imageView.setImageBitmap(BitmapFactory.decodeFile(intent.getStringExtra(MainActivity.VIEW)));
    }
}
