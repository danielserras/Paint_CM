package com.example.paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class PaintView extends View implements View.OnTouchListener {

    private GestureDetector mGestureDetector;
    private ArrayList<Paint> paintsList;
    private ArrayList<Path> pathsList;
    private Bitmap bitmap = null;

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
        initPaint();
    }

    public PaintView(Context context, AttributeSet attrs, GestureDetector mGestureDetector,
                     ArrayList<Paint> paintsList, ArrayList<Path> pathsList, Bitmap bitmap) {
        super(context, attrs);
        this.mGestureDetector = mGestureDetector;
        this.pathsList = pathsList;
        this.paintsList = paintsList;
        this.bitmap = bitmap;
        setOnTouchListener(this);
        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(bitmap != null){
            canvas.drawBitmap(bitmap,0,0,null);
        }
        for(int i = 0; i<this.paintsList.size(); i++){
            canvas.drawPath(this.pathsList.get(i), this.paintsList.get(i));
        }
    }

    @Override
    public boolean performClick(){
        return super.performClick();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.pathsList.get(this.pathsList.size()-1).moveTo(eventX, eventY);// updates the path initial point
                return true;
            case MotionEvent.ACTION_MOVE:
                this.pathsList.get(this.pathsList.size()-1).lineTo(eventX, eventY);// makes a line to the point each time this event is fired
                break;
            case MotionEvent.ACTION_UP:// when you lift your finger
                performClick();
                break;
            default:
                return false;
        }

        // Schedules a repaint.
        invalidate();
        return true;
    }

    private void initPaint(){
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(20f);
        paint.setColor(Main.strokeColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        this.paintsList.add(paint);
        this.pathsList.add(new Path());
    }

    public void erase(ArrayList<Paint> paintsList, ArrayList<Path> pathsList){
        this.pathsList = pathsList;
        this.paintsList = paintsList;
        initPaint();
        invalidate();

    }

    public void changeStrokeColor(){
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(20f);
        paint.setColor(Main.strokeColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        this.paintsList.add(paint);
        this.pathsList.add(new Path());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getCanvas(String encodedImage){
        byte[] decodedString = Base64.getDecoder().decode(encodedImage);
        bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length).
                copy(Bitmap.Config.ARGB_8888, true);
        this.invalidate();
        Main.bitmap = bitmap;
    }
}

