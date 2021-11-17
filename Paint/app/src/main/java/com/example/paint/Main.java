package com.example.paint;

import static com.example.paint.Canvas_Fragment.changeStrokeColor;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;

public class Main extends AppCompatActivity implements SensorEventListener{

    private static final int REQUEST_CODE = 1;
    private static int background_color = Color.WHITE;
    private static int fragment = 0;
    public static int strokeColor = Color.BLACK;
    public static ArrayList<Paint> paintsList = new ArrayList<Paint>();
    public static ArrayList<Path> pathsList = new ArrayList<Path>();
    public static Bitmap bitmap = null;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Sensor accelerometer;
    private float lastLight;
    private float maxLigth = 0;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 3000;
    private Canvas_Fragment canvasFragment;
    private Palette paletteFragment;
    public static String username = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            username = extras.getString("Username");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ConstraintLayout mLayout = (ConstraintLayout) findViewById(R.id.layout);
        mLayout.setBackgroundColor(background_color);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        canvasFragment = new Canvas_Fragment();
        paletteFragment = new Palette();

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            fragmentTransaction.replace(R.id.fragmentContainerView3, canvasFragment);
            fragmentTransaction.addToBackStack(null);
        }
        else{
            fragmentTransaction.replace(R.id.fragmentContainerView2, canvasFragment);
            fragmentTransaction.replace(R.id.fragmentContainerView4, paletteFragment);
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            ConstraintLayout mLayout = (ConstraintLayout) findViewById(R.id.layout);
            if (data.hasExtra("color")) {
                background_color = (int)data.getExtras().getInt("color");
                mLayout.setBackgroundColor(background_color);
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings_item:
                settings();
                return true;
            case R.id.about_item:
                about();
                return true;
            case R.id.map_item:
                map();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //TODO
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if(mySensor.getType() == Sensor.TYPE_LIGHT){
            float brightness = event.values[0];
            if(Math.abs(brightness-lastLight)>5) {
                if(maxLigth<brightness){
                    this.maxLigth = brightness;
                }
                
                float nBrightness = normalize(brightness, 0, this.maxLigth, 255, 0);
                Settings.System.putInt(getApplicationContext().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, Math.round(nBrightness));
                lastLight = brightness;
            }
        }
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            //alignment with x
            ConstraintLayout mLayout = (ConstraintLayout) findViewById(R.id.layout);
            if(x>9 && x<11){
                mLayout.setBackgroundColor(Color.parseColor("#d6f5d6"));
            }
            else if(x>-11 && x<-9){
                mLayout.setBackgroundColor(Color.parseColor("#ffcccc"));
            }
            //alignment with y
            else if(y>9 && y<11){
                mLayout.setBackgroundColor(Color.parseColor("#ffffff"));
            }
            else if(y>-11 && y<-9){
                mLayout.setBackgroundColor(Color.parseColor("#ffffcc"));
            }
            //alignment with z
            else if(z>9 && z<11){
                mLayout.setBackgroundColor(Color.parseColor("#d1e0e0"));
            }
            else if(z>-11 && z<-9){
                mLayout.setBackgroundColor(Color.parseColor("#ffcc99"));
            }

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    this.canvasFragment.clearCanvas();
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener( this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener( this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void settings(){
        //Settings_Button
        Intent intent = new Intent(this, com.example.paint.Settings.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void about(){
        //About_Button
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
    }

    public void map(){
        //About_Button
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    public void switchFragment(View view){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(fragment == 0) {
            fragment = 1;
            Palette paletteFragment = new Palette();
            fragmentTransaction.replace(R.id.fragmentContainerView3, paletteFragment);
            fragmentTransaction.addToBackStack(null);
        }
        else{
            fragment = 0;
            Canvas_Fragment canvasFragment = new Canvas_Fragment();
            fragmentTransaction.replace(R.id.fragmentContainerView3, canvasFragment);
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    public void buttonBlack(View view){
        Main.strokeColor = Color.BLACK;
        changeStrokeColor();
    }
    public void buttonBlue(View view){
        Main.strokeColor = Color.parseColor("#304FFE");
        changeStrokeColor();
    }
    public void buttonLightGreen(View view){
        Main.strokeColor = Color.parseColor("#AEEA00");
        changeStrokeColor();
    }
    public void buttonGreen(View view){
        Main.strokeColor = Color.parseColor("#00C853");
        changeStrokeColor();
    }
    public void buttonYellow(View view){
        Main.strokeColor = Color.parseColor("#FFD600");
        changeStrokeColor();
    }
    public void buttonYellowToasted(View view){
        Main.strokeColor = Color.parseColor("#FFAB00");
        changeStrokeColor();
    }
    public void buttonGray(View view){
        Main.strokeColor = Color.parseColor("#535353");
        changeStrokeColor();
    }
    public void buttonRed(View view){
        Main.strokeColor = Color.parseColor("#D50000");
        changeStrokeColor();
    }
    public void buttonPink(View view){
        Main.strokeColor = Color.parseColor("#D826D5");
        changeStrokeColor();
    }
    public void buttonPurple(View view){
        Main.strokeColor = Color.parseColor("#AA00FF");
        changeStrokeColor();
    }
    public void buttonViolet(View view){
        Main.strokeColor = Color.parseColor("#BA0255");
        changeStrokeColor();
    }
    public void buttonOrange(View view){
        Main.strokeColor = Color.parseColor("#FF6D00");
        changeStrokeColor();
    }
    public void buttonWhite(View view){
        Main.strokeColor = Color.WHITE;
        changeStrokeColor();
    }
    public void buttonLightBlue(View view){
        Main.strokeColor = Color.parseColor("#00B8D4");
        changeStrokeColor();
    }

    private float normalize(float x, float inMin, float inMax, float outMin, float outMax) {
        float outRange = outMax - outMin;
        float inRange  = inMax - inMin;
        return (x - inMin) *outRange / inRange + outMin;
    }

    public void saveCanvas(View view){
        AlertDialog.Builder saveDialog = new AlertDialog.Builder(view.getContext());
        saveDialog.setTitle("Save?(Choose a name)");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        saveDialog.setView(input);
        saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = input.getText().toString();
                if(name.equals("")){
                    name = null;
                }
                canvasFragment.saveCanvas(view, name);
            }
        });
        saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        saveDialog.show();
    }

    public void getCanvas(View view){
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = mDatabase.getReference();
        Task t = databaseReference.child("paints").child(this.username).get();
        t.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                try {
                    DataSnapshot dataSnapshot = (DataSnapshot) t.getResult();

                    AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                    builder.setTitle("Choose a draw!");
                    ArrayList<String> items = new ArrayList<String>();
                    HashMap<String,String> paints = new HashMap<String, String>();
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                       items.add(ds.getKey());
                       paints.put(ds.getKey(), (String)ds.getValue());
                    }
                    String[] itemsArray = items.toArray(new String[0]);
                    builder.setItems(itemsArray, new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            canvasFragment.getCanvas(paints.get(itemsArray[which]));

                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }

}