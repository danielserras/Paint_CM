package com.example.paint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void start(View view){
        //Start_Button
        Intent intent = new Intent(this, Main.class);
        TextView t = (TextView) findViewById(R.id.username);
        intent.putExtra("Username",t.getText().toString());
        startActivity(intent);
    }
}