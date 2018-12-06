package com.droidverine.beardfacedigital.beardfacecompanion;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import static android.util.Config.LOGD;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener//,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
  EditText editText;
  Button btnsend,btnsend1;
  GoogleApiClient googleApiClient;
  private GoogleApiClient mGoogleApiClient;
  Bitmap newImg;

    @Override
    protected void onStart() {
//       googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
      //  googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
   /*    commented 15:11
    GoogleApiClient.Builder builder=new GoogleApiClient.Builder(this);
        builder.addApi(Wearable.API);
        builder.addConnectionCallbacks(this).addConnectionCallbacks(this);
        googleApiClient= builder.build();

*/        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnsend=(Button)findViewById(R.id.sendbtn);
        btnsend1=(Button)findViewById(R.id.sendbtn1);
        btnsend1.setOnClickListener(this);

        btnsend.setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d("connection","estabished");
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                    }
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.sendbtn:
               newImg=BitmapFactory.decodeResource(getResources(), R.drawable.ambip);
                   Asset asset = ConvertAsset(newImg);
                  // SendImgtoWearable(asset);
                syncWatch("oo","sjdskkk",11);
break;
            case R.id.sendbtn1:
                syncWatch1("22","ss",00);
        }

    }

/* commented 15:11
    @Override
    public void onConnected(@Nullable Bundle bundle) {
    Log.d("con","hakon");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    */


    private Asset ConvertAsset(Bitmap newImg)
    {
        ByteArrayOutputStream byteArrayOutputStream=null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            newImg.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            return Asset.createFromBytes(byteArrayOutputStream.toByteArray());
        }
        finally {
            if (null!=byteArrayOutputStream)
            {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
    //new code
    private void syncWatch(String min, String max, int weatherId){
        //  Log.v("SunshineSyncAdapter", "syncWatch");
        String time =  String.valueOf(new Date().getTime());
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/weather-update");
        putDataMapRequest.getDataMap().putDouble("time",  new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
        //Bitmap bm = BitmapFactory.decodeResource(getContext().getResources(), Utility.getArtResourceForWeatherCondition(weatherId));
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.droidverine);
        //Asset asset = getContext().getResources().createAssetFromBitmap(bitmap);
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        Asset asset = Asset.createFromBytes(byteStream.toByteArray());
        putDataMapRequest.getDataMap().putAsset("weather-image", asset);
        putDataMapRequest.getDataMap().putString("min-temp", min);
        putDataMapRequest.getDataMap().putString("max-temp", max);
        // Log.v("SunshineSyncAdapter", min + time + " " + max + time);
        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        request.setUrgent();
        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);

        dataItemTask.addOnSuccessListener(
                new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Log.d("lavdechal", "Sending image was successful:");
                    }
                });
        if (mGoogleApiClient == null){
             Log.v("SunshineSyncAdapter", "NOOOOOOOOOOOOOOOOO, life is no good");
            return;
        }

        Wearable.DataApi.putDataItem(mGoogleApiClient,request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {


            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (!dataItemResult.getStatus().isSuccess()) {
                    Log.v("MainActivity", "Something went wrong, watch was not notified");
                } else {
                    Log.v("MainActivity", "Success, Watch Notified");
                }
            }
        });
    }
    private void syncWatch1(String min, String max, int weatherId){
        //  Log.v("SunshineSyncAdapter", "syncWatch");
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.ambip);
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/weather-update");
        putDataMapRequest.getDataMap().putDouble("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap1.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        Asset asset = Asset.createFromBytes(byteStream.toByteArray());
        putDataMapRequest.getDataMap().putAsset("weather-image", asset);
        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        request.setUrgent();
        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);

        dataItemTask.addOnSuccessListener(
                new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Log.d("lavdechal1", "Sending image was successful:");
                    }
                });
        if (mGoogleApiClient == null){
            Log.v("SunshineSyncAdapter", "NOOOOOOOOOOOOOOOOO, life is no good");
            return;
        }

        Wearable.DataApi.putDataItem(mGoogleApiClient,request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {


            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (!dataItemResult.getStatus().isSuccess()) {
                    Log.v("MainActivity", "Something went wrong, watch was not notified");
                } else {
                    Log.v("MainActivity", "Success, Watch Notified");
                }
            }
        });
    }
}
