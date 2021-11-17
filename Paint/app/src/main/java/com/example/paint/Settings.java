package com.example.paint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import yuku.ambilwarna.AmbilWarnaDialog;

public class Settings extends AppCompatActivity {
    ConstraintLayout mLayout;
    int mDefaultColor;
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mLayout = (ConstraintLayout) findViewById(R.id.layout);
        mDefaultColor = ContextCompat.getColor(Settings.this, R.color.white);
        mButton = (Button) findViewById(R.id.button4);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker(v);

            }
        });

    }
    public void finish(View v, int c){
        Intent data = new Intent();
        data.putExtra("color", c);
        setResult(RESULT_OK, data);
        super.finish();
    }
    public void openColorPicker(View v) {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, mDefaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                mDefaultColor = color;
                finish(v, color);

            }
        });
        colorPicker.show();
    }

}