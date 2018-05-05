package com.example.laurens.meet_combi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

public class Oscilloscope extends ContentFragment
        implements View.OnTouchListener {

    OscilloscopeScreen screen;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parentVG, Bundle savedInstanceState) {

        // Create surfaceView
        screen = new OscilloscopeScreen(this.getActivity());

        // Set viewer interaction listeners
        screen.setOnTouchListener(this);

        // Attach the screen
        return screen;
    }

    @Override
    public void onPause() {

        super.onPause();
        screen.pause();
    }

    @Override
    public void onResume() {

        super.onResume();
        screen.resume();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //if (motionEvent.getAction() == MotionEvent.ACTION_UP) view.performClick(); // default methode
        return screen.onTouch(view, motionEvent);
    }

    public class OscilloscopeScreen extends SurfaceView
            implements Runnable {

        // Thread variables
        private Thread thread = null;
        private boolean isPaused = true; // Don't run thread by default

        // Drawing variables
        private SurfaceHolder holder;

        public OscilloscopeScreen(Context context) {
            super(context);
            holder = getHolder();
        }

        @Override
        public void run() {
            while (!isPaused) {
                // Check holder
                if (!holder.getSurface().isValid()) continue; // wait till holder is value

                // Lock the canvas
                Canvas canvas = holder.lockCanvas();

                // Get the dimensions
                int canvasWidth = canvas.getWidth();
                int canvasHeight = canvas.getHeight();

                // Draw background
                int color = ContextCompat.getColor(getContext(), R.color.colorOscilloscope_Background);
                canvas.drawARGB(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color));

                // Draw oscilloscope screen
                int offset = 25;
                drawOscilloscopeScreen(canvas, (int) (canvasWidth*.5), (int) (canvasWidth - offset), offset, (int) (canvasHeight*.5));

                // Unlock the canvas
                holder.unlockCanvasAndPost(canvas);
            }
        }

        public void resume() {
            isPaused = false;
            thread = new Thread(this);
            thread.start();
        }

        public void pause() {
            isPaused = true;

            while(true) {
                try {
                    thread.join(); // End thread
                } catch (InterruptedException e) {
                    e.printStackTrace(); // TODO
                }
                break;
            }

            thread = null;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {

            float touchX, touchY;


            // Let the thread sleep a bit so we don't get too much actions called
            try{
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Get the action
            switch (motionEvent.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    // get touch position
                    touchX = motionEvent.getX();
                    touchY = motionEvent.getY();
                    break;

                case MotionEvent.ACTION_UP:
                    // get touch position
                    touchX = motionEvent.getX();
                    touchY = motionEvent.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    break;

                default:
                    break;
            }

            return true; // Check for all actions instead of only one
        }

        private void drawOscilloscopeScreen(Canvas canvas, int screenLeft, int screenRight, int screenTop, int screenBottom) {

            Rect rect = new Rect(screenLeft, screenTop, screenRight, screenBottom);
            Paint paint = new Paint();

            // Draw background of the screen
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorOscilloscope_ScreenBackground));
            canvas.drawRect(rect, paint);

            // Draw the outside of the screen
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorOscilloscope_ScreenDivisionLines));
            canvas.drawRect(rect, paint);
        }
    }
}
