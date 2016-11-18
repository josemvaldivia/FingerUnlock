package com.example.jose.fingerunlock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class PlayActivity extends AppCompatActivity {

    final private static LinkedList<PlayActivity.MarkerView> playMarkers = new LinkedList<PlayActivity.MarkerView>();
    private static final int MIN_DXDY = 2;
    final private static int MAX_TOUCHES = 20;

    // Check correct radius
    private double RADIUS = 150.0;
    private FrameLayout mFrame;
    final private static LinkedList<MarkerView> mInactiveMarkers = new LinkedList<MarkerView>();

    @SuppressLint("UseSparseArrays")
    private static Map<Integer, MarkerView> mActiveMarkers = new HashMap<Integer, MarkerView>();


    public void unlockGame(){
        Intent intent = new Intent(this, UnlockActivity.class);
        startActivity(intent);
        finish();
    }

    public void lockGame(){
        Intent intent = new Intent(this, LockActivity.class);
        startActivity(intent);
        finish();
    }

    private String parseMarkers(Collection<MarkerView> markers){
        String parsed = "";
        int total = markers.size();
        int i = 0;
        for(MarkerView marker : markers){
            if(i+1 == total){
                parsed += String.valueOf(marker.getXLoc()) + "," + String.valueOf(marker.getYLoc());
            }else{
                parsed += String.valueOf(marker.getXLoc()) + "," + String.valueOf(marker.getYLoc()) + ",";
            }
            i += 1;
        }
        Log.i("PARSED: ", parsed);
        return parsed;
    }

    private boolean checkFingers(
            LinkedList<PlayActivity.MarkerView> oldFingers,
            LinkedList<PlayActivity.MarkerView> newFingers
    ) {
        if (oldFingers.size() != newFingers.size()) {
            Log.i("CHECKFINGERS", "Size is different");
            return false;
        }
        int matches = 0;
        for (int i = 0; i < oldFingers.size(); i++) {
            MarkerView oldFinger = oldFingers.get(i);
            for (int j = 0; j < newFingers.size(); j++) {
                MarkerView newFinger = newFingers.get(j);
                double distance = (
                        Math.pow(newFinger.mX - oldFinger.mX, 2) +
                                Math.pow(newFinger.mY - oldFinger.mY, 2)
                );
                if (distance < Math.pow(RADIUS, 2)) {
                    matches += 1;
                    Log.i("CHECKFINGERS", "MATCH"+String.valueOf(matches));
                }
            }
        }
        return (matches == oldFingers.size());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Intent intent = getIntent();
        String[] positions = intent.getStringExtra(MainActivity.POSITION_MESSAGE).split(",");
        initViews();
        mFrame = (FrameLayout)findViewById(R.id.playLayout);

        for(int i = 0; i < positions.length; i+=2){
            String x = positions[i];
            String y = positions[i+1];
            MarkerView  marker = new MarkerView(this, Float.parseFloat(x), Float.parseFloat(y));
            playMarkers.add(marker);
        }


        updateTouches(playMarkers.size());
        for (PlayActivity.MarkerView marker : playMarkers) {
            mFrame.addView(marker);
        }
        mFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    // Show new MarkerView
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN: {
                        int pointerIndex = event.getActionIndex();
                        int pointerID = event.getPointerId(pointerIndex);
                        MarkerView marker = mInactiveMarkers.remove();
                        if (null != marker) {
                            mActiveMarkers.put(pointerID, marker);
                            marker.setXLoc(event.getX(pointerIndex));
                            marker.setYLoc(event.getY(pointerIndex));
                            updateTouches(mActiveMarkers.size());
                            mFrame.addView(marker);
                        }
                        break;
                    }

                    // Remove one MarkerView
                    case MotionEvent.ACTION_UP: {
                        Collection<MarkerView> markers = mActiveMarkers.values();
                        LinkedList<MarkerView> markerslist = new LinkedList<MarkerView>();
                        markerslist.addAll(markers);
                        if(checkFingers(markerslist, playMarkers)){
                            unlockGame();
                        }else{
                            lockGame();
                        }
                        String parsed = parseMarkers(markers);

                        mActiveMarkers = new HashMap<Integer, MarkerView>();
                        updateTouches(0);
                        //Log.i("MARKERS", mInactiveMarkers.toString());
                    }
                    case MotionEvent.ACTION_MOVE: {

                        for (int idx = 0; idx < event.getPointerCount(); idx++) {

                            int ID = event.getPointerId(idx);

                            MarkerView marker = mActiveMarkers.get(ID);
                            if (null != marker) {

                                // Redraw only if finger has travel ed a minimum distance
                                if (Math.abs(marker.getXLoc() - event.getX(idx)) > MIN_DXDY
                                        || Math.abs(marker.getYLoc()
                                        - event.getY(idx)) > MIN_DXDY) {

                                    // Set new location

                                    marker.setXLoc(event.getX(idx));
                                    marker.setYLoc(event.getY(idx));

                                    // Request re-draw
                                    marker.invalidate();
                                }
                            }
                        }

                        break;
                    }
                    default:
                        Log.i("Tag", "unhandled action");
                }
                return true;
            }
            // update number of touches on each active MarkerView
            private void updateTouches(int numActive) {
                for (MarkerView marker : mActiveMarkers.values()) {
                    marker.setTouches(numActive);
                }
            }
        });
    }
    private void initViews() {
        for (int idx = 0; idx < MAX_TOUCHES; idx++) {
            mInactiveMarkers.add(new MarkerView(this, -1, -1));
        }
    }
    private void updateTouches (int numActive) {
        for (PlayActivity.MarkerView marker : playMarkers) {
            marker.setTouches(numActive);
        }
    }

    private class MarkerView extends View {
        private float mX, mY;
        final static private int MAX_SIZE = 400;
        private int mTouches = 0;
        final private Paint mPaint = new Paint();

        public MarkerView(Context context, float x, float y) {
            super(context);
            mX = x;
            mY = y;
            mPaint.setStyle(Paint.Style.FILL);

            Random rnd = new Random();
            mPaint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256),
                    rnd.nextInt(256));
        }

        float getXLoc() {
            return mX;
        }

        void setXLoc(float x) {
            mX = x;
        }

        float getYLoc() {
            return mY;
        }

        void setYLoc(float y) {
            mY = y;
        }

        void setTouches(int touches) {
            mTouches = touches;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawCircle(mX, mY, 50, mPaint);
        }
    }
}