package com.droidverine.beardfacedigital.beardfacecompanion;
/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't shown. On
 * devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient mode.
 */
public class MyWatchFaceService extends CanvasWatchFaceService
{

    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);


    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }




    private class Engine extends CanvasWatchFaceService.Engine implements GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks,
            ResultCallback<DataItemBuffer> {

        /* Handler to update the time once a second in interactive mode. */
        private final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (R.id.message_update == message.what) {
                    invalidate();
                    if (shouldTimerBeRunning()) {
                        long timeMs = System.currentTimeMillis();
                        long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                        mUpdateTimeHandler.sendEmptyMessageDelayed(R.id.message_update, delayMs);
                    }
                }
            }
        };

        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };

        private boolean mRegisteredTimeZoneReceiver = false;
        private  GoogleApiClient googleApiClient;
        // Feel free to change these values and see what happens to the watch face.
        private static final float HAND_END_CAP_RADIUS = 4f;
        private static final float STROKE_WIDTH = 4f;
        private static final int SHADOW_RADIUS = 6;
        SurfaceHolder srf;
        private Calendar mCalendar;
        public int pic=0;
        private Paint mBackgroundPaint;
        private Paint mHandPaint;
        String WatchFaceWakelockTag="unizq";
        PowerManager.WakeLock wakeLock;
        private boolean mAmbient;
        private Bitmap nBackgroundBitmap;
        private Bitmap mBackgroundBitmap;
        private Bitmap mGrayBackgroundBitmap;
        private Bitmap AmbientBitmap;
        PowerManager powerManager;
        GoogleApiClient mGoogleApiClient ;
        private float mHourHandLength;
        private float mMinuteHandLength;
        private float mSecondHandLength;

        /**
         * Whether the display supports fewer bits for each color in ambient mode.
         * When true, we disable anti-aliasing in ambient mode.
         */
        private boolean mLowBitAmbient;
        /**
         * Whether the display supports burn in protection in ambient mode.
         * When true, remove the background in ambient mode.
         */
        private boolean mBurnInProtection;
        Bitmap mutableBitmap=null;
        private int mWidth;
        private int mHeight;
        private float mCenterX;
        private float mCenterY;
        private float mScale = 1;
        private float mScalegr=1;
        private float getmScalenw=1;
        private float mScalemutable=1;
        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            powerManager = (PowerManager) getSystemService(POWER_SERVICE);

            wakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE), "wakeLock");

            srf=holder;
            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFaceService.this).build());
            //  googleApiClient=new GoogleApiClient.Builder(getApplicationContext())
           // .addApi(Wearable.API)
           // .addConnectionCallbacks(this).addConnectionCallbacks(this).build();
         // googleApiClient.connect();


            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.BLACK);

            final int backgroundResId = R.drawable.bg;

            final int backgroundResId1 = R.drawable.ambi;

            mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), backgroundResId);
            AmbientBitmap=BitmapFactory.decodeResource(getResources(), backgroundResId1);
            mHandPaint = new Paint();
            mHandPaint.setColor(Color.WHITE);
            mHandPaint.setStrokeWidth(STROKE_WIDTH);
            mHandPaint.setAntiAlias(true);
            mHandPaint.setStrokeCap(Paint.Cap.ROUND);
            mHandPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, Color.BLACK);
            mHandPaint.setStyle(Paint.Style.STROKE);

            mCalendar = Calendar.getInstance();


            mGoogleApiClient = new GoogleApiClient.Builder(MyWatchFaceService.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();

        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(R.id.message_update);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
          //  googleApiClient.connect();
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);

        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient || mBurnInProtection) {
                    mHandPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            /*
             * Whether the timer should be running depends on whether we're visible (as well as
             * whether we're in ambient mode), so we may need to start or stop the timer.
             */
            updateTimer();
        }


        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
         //   googleApiClient.connect();
            super.onSurfaceChanged(holder, format, width, height);
            mWidth = width;
            mHeight = height;

            /*
             * Find the coordinates of the center point on the screen.
             * Ignore the window insets so that, on round watches
             * with a "chin", the watch face is centered on the entire screen,
             * not just the usable portion.
             */
            mCenterX = mWidth / 2f;
            mCenterY = mHeight / 2f;
            mScale = ((float) width) / (float) mBackgroundBitmap.getWidth();
            mScalegr = ((float) width) / (float) AmbientBitmap.getWidth();
            if (mutableBitmap!=null) {

            }

           /*
             * Calculate the lengths of the watch hands and store them in member variables.
             */
            mHourHandLength = mCenterX * 0.5f;
            mMinuteHandLength = mCenterX * 0.7f;
            mSecondHandLength = mCenterX * 0.9f;



            if (!mBurnInProtection || !mLowBitAmbient) {
               // initGrayBackgroundBitmap();
                AmbientBitmap = Bitmap.createScaledBitmap(AmbientBitmap,
                        (int) (AmbientBitmap.getWidth() * mScalegr),
                        (int) (AmbientBitmap.getHeight() * mScalegr), true);
            }


                mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                        (int) (mBackgroundBitmap.getWidth() * mScalegr),
                        (int) (mBackgroundBitmap.getHeight() * mScalegr), true);


        }


        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            long now = System.currentTimeMillis();

            mCalendar.setTimeInMillis(now);

            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawBitmap(AmbientBitmap, 0, 0, mBackgroundPaint);


            } else if (mAmbient) {
                canvas.drawBitmap(AmbientBitmap, 0, 0, mBackgroundPaint);
            }
            else if ( nBackgroundBitmap != null){

                canvas.drawBitmap(nBackgroundBitmap, 0, 0, mBackgroundPaint);

            }else {
                canvas.drawBitmap(mBackgroundBitmap, 0, 0, mBackgroundPaint);

            }

            /*
             * These calculations reflect the rotation in degrees per unit of time, e.g.,
             * 360 / 60 = 6 and 360 / 12 = 30.
             */
            final float seconds =
                    (mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f);
            final float secondsRotation = seconds * 6f;

            final float minutesRotation = mCalendar.get(Calendar.MINUTE) * 6f;

            final float hourHandOffset = mCalendar.get(Calendar.MINUTE) / 2f;
            final float hoursRotation = (mCalendar.get(Calendar.HOUR) * 30) + hourHandOffset;

            // save the canvas state before we begin to rotate it
            canvas.save();

            canvas.rotate(hoursRotation, mCenterX, mCenterY);
            drawHand(canvas, mHourHandLength);

            canvas.rotate(minutesRotation - hoursRotation, mCenterX, mCenterY);
            drawHand(canvas, mMinuteHandLength);

            /*
             * Make sure the "seconds" hand is drawn only when we are in interactive mode.
             * Otherwise we only update the watch face once a minute.
             */
            if (!mAmbient) {
                canvas.rotate(secondsRotation - minutesRotation, mCenterX, mCenterY);
                canvas.drawLine(mCenterX, mCenterY - HAND_END_CAP_RADIUS, mCenterX,
                        mCenterY - mSecondHandLength, mHandPaint);
            }
            canvas.drawCircle(mCenterX, mCenterY, HAND_END_CAP_RADIUS, mHandPaint);
            // restore the canvas' original orientation.
            canvas.restore();
        }

        private void drawHand(Canvas canvas, float handLength) {
            canvas.drawRoundRect(mCenterX - HAND_END_CAP_RADIUS, mCenterY - handLength,
                    mCenterX + HAND_END_CAP_RADIUS, mCenterY + HAND_END_CAP_RADIUS,
                    HAND_END_CAP_RADIUS, HAND_END_CAP_RADIUS, mHandPaint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
       // googleApiClient.connect();

            if (visible) {
                registerReceiver();
                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            /*
             * Whether the timer should be running depends on whether we're visible
             * (as well as whether we're in ambient mode),
             * so we may need to start or stop the timer.
             */
            updateTimer();
        }

        private void registerReceiver() {
//            googleApiClient.connect();

            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MyWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MyWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(R.id.message_update);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(R.id.message_update);
            }
        }


        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        @Override
        public void onConnected(Bundle bundle) {
            Log.d("Android Wear ", "connected");
            Wearable.DataApi.addListener(mGoogleApiClient,mDataListener);
            Wearable.DataApi.getDataItems(mGoogleApiClient).setResultCallback(this);
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d("Android Wear", "Connection failed");

        }
        @Override
        public void onConnectionSuspended(int i) {
            Log.d("Android Wear", "Connection failed");

        }

        @Override
        public void onResult(DataItemBuffer dataItems) {
              Log.v("SunshineWatchFace", "onResult 1");
            for (DataItem dataItem:dataItems){
                if (dataItem.getUri().getPath().compareTo("/weather-update") == 0) {
                     Log.v("SunshineWatchFace", "found data items 1");
                    DataMapItem dataMap = DataMapItem.fromDataItem(dataItem);
                 //   Asset weatherImage = dataMap.getDataMap().getAsset("weather-image");

               //     DownloadFilesTask task = new DownloadFilesTask();
                 //   task.execute(weatherImage);




                }
            }
            dataItems.release();
            if (isVisible() && !isInAmbientMode()) {

                invalidate();
            }

        }


        DataApi.DataListener mDataListener = new DataApi.DataListener(){
            @Override
            public void onDataChanged(DataEventBuffer dataEvents) {
                 Log.v("SunshineWatchFace", "onDataChanged");
                Vibrator vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(2000);

                try{
                    for(DataEvent dataEvent: dataEvents){

                        DataItem dataItem = dataEvent.getDataItem();
                       if(dataItem.getUri().getPath().compareTo("/Settings-Screen")==0)
                       {
                           DataMapItem dataMapSettings = DataMapItem.fromDataItem(dataEvent.getDataItem());
                          Boolean Screenpref= dataMapSettings.getDataMap().getBoolean("AlwaysOn");
                          Log.d("pref",Screenpref.booleanValue()+"");
                           if(Screenpref)
                           {
                               wakeLock.acquire();

                               Log.d("wakelocky","gained");

                           }
                           else
                           {
                              wakeLock.release();

                               Log.d("wakelocky","released");


                           }

                       }

                        if(dataItem.getUri().getPath().compareTo("/weather-update") == 0){
                            // Log.v("SunshineWatchFace", "found data items");
                            DataMapItem dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem());
                            Asset weatherImage = dataMap.getDataMap().getAsset("weather-image");
                           // bitmap = loadBitmapFromAsset(weatherImage);

                            DownloadFilesTask task = new DownloadFilesTask();
                            task.execute(weatherImage);



                        }
                    }
                    dataEvents.release();

                    if(!isInAmbientMode()){


                        invalidate();
                    }
                }catch (Exception e){
                }
            }

        };

        private class DownloadFilesTask extends AsyncTask<Asset, Void, Bitmap> {
            @Override
            protected Bitmap doInBackground(Asset... params) {
                 Log.v("SunshineWatchFace", "Doing Background");
                return loadBitmapFromAsset(params[0]);
            }

            @Override
            protected void onPostExecute(Bitmap b) {


                mScalemutable = ((float) mWidth) / (float) b.getWidth();
                nBackgroundBitmap = Bitmap.createScaledBitmap(b,
                        (int) (b.getWidth() * mScalemutable),
                        (int) (b.getHeight() * mScalemutable), true);
                PowerManager.WakeLock mWakeLock=powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE), "wakeLock") ;
                mWakeLock.acquire(2000);


            }

            public Bitmap loadBitmapFromAsset(Asset asset) {
                if (asset == null) {
                    throw new IllegalArgumentException("Asset must be non-null");
                }
                ConnectionResult result =
                        mGoogleApiClient.blockingConnect(5000, TimeUnit.MILLISECONDS);
                if (!result.isSuccess()) {
                    return null;
                }
                // convert asset into a file descriptor and block until it's ready
                InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                        mGoogleApiClient, asset).await().getInputStream();
              //  mGoogleApiClient.disconnect();

                if (assetInputStream == null) {
                    // Log.v("SunshineWatchFace", "Requested an unknown Asset.");
             //       mGoogleApiClient.disconnect();
                    return null;
                }
                // decode the stream into a bitmap
                Log.v("SunshineWatchFace", "Returning Background");
                return BitmapFactory.decodeStream(assetInputStream);
            }

        }



    }}

