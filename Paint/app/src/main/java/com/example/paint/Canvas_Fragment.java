package com.example.paint;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class Canvas_Fragment extends Fragment {
    private static PaintView paintView;
    private DatabaseReference databaseReference;
    private FirebaseDatabase mDatabase;

    public Canvas_Fragment() {
        // Required empty public constructor
    }

    public static Canvas_Fragment newInstance() {
        Canvas_Fragment fragment = new Canvas_Fragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GestureListener mGestureListener = new GestureListener();
        GestureDetector mGestureDetector = new GestureDetector(getContext(), mGestureListener);
        mGestureDetector.setIsLongpressEnabled(true);
        mGestureDetector.setOnDoubleTapListener(mGestureListener);
        paintView = new PaintView(getContext(), null, mGestureDetector,
                Main.paintsList, Main.pathsList, Main.bitmap);
        mGestureListener.setCanvas(paintView);
        mDatabase = FirebaseDatabase.getInstance();
        databaseReference = mDatabase.getReference();
        paintView.destroyDrawingCache();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return paintView;
    }

    public static void changeStrokeColor(){
        paintView.changeStrokeColor();
    }
    public void clearCanvas(){
        Main.paintsList.clear();
        Main.pathsList.clear();
        this.paintView.erase(Main.paintsList, Main.pathsList);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void saveCanvas(View view, String name){
        DatabaseReference dr = databaseReference.child("paints").child(Main.username);
        String paintID = null;
        if(name!=null){
            paintID = name;
        }
        else{
            paintID = dr.push().getKey();
        }
        paintView.setDrawingCacheEnabled(true);
        Bitmap b = paintView.getDrawingCache();
        if(b == null){
            b = loadLargeBitmapFromView(paintView);
        }
        Bitmap bitmap = Bitmap.createBitmap(b);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(b, 0, 0, null);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.getEncoder().encodeToString(byteArray);

        dr.child(paintID).setValue(encodedImage);
        paintView.destroyDrawingCache();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getCanvas(String canvas){
        paintView.getCanvas(canvas);
    }

    private static Bitmap loadLargeBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;
    }
}

