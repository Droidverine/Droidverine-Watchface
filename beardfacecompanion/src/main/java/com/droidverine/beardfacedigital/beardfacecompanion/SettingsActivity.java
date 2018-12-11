package com.droidverine.beardfacedigital.beardfacecompanion;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import java.util.Date;

/**
 * Created by Shivraj on 12/12/2018.
 */
public class SettingsActivity extends AppCompatActivity {
Switch screensw;
    Boolean status;
    SharedPreferences.Editor editor;
    GoogleApiClient mGoogleApiClient;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        screensw=findViewById(R.id.ScreenSwitch);
        progressDialog = new ProgressDialog(this);

        editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d("connection", "estabished");
//                        new DownloadFilesTask().execute();

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
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
         status = prefs.getBoolean("status",false);

        if (status==true )
        {
            screensw.setChecked(true);

        }
        if (status != null) {
            String name = prefs.getString("name", "No name defined");//"No name defined" is the default value.
            int idName = prefs.getInt("idName", 0); //0 is the default value.
        }
        screensw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if(isChecked==true)
               {
                   status=true;
                   editor.putBoolean("status",true);
                   editor.apply();

                   new DownloadFilesTask().execute();



               }
               else {
                   status=false;
                   editor.putBoolean("status",false);
                   editor.apply();
                   new DownloadFilesTask().execute();

               }
            }
        });






    }

    private class DownloadFilesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {

                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/Settings-Screen");
                putDataMapRequest.getDataMap().putDouble("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
                putDataMapRequest.getDataMap().putBoolean("AlwaysOn", status);
                PutDataRequest request = putDataMapRequest.asPutDataRequest();
                request.setUrgent();
                Task<DataItem> dataItemTask = Wearable.getDataClient(getApplicationContext()).putDataItem(request);

                dataItemTask.addOnSuccessListener(
                        new OnSuccessListener<DataItem>() {
                            @Override
                            public void onSuccess(DataItem dataItem) {
                                Log.d("Droidverine", " preference sent successfully:");
                            }
                        });
                if (mGoogleApiClient == null) {
                    Log.v("Droidverine", "NOOOOOOOOOOOOOOOOO, life is no good");
                }

                Wearable.DataApi.putDataItem(mGoogleApiClient, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {


                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        if (!dataItemResult.getStatus().isSuccess()) {
                            Log.v("Settings Activity", "Something went wrong, watch was not notified");
                        } else {
                            Log.v("Settings Activity", "Success, Watch Notified");
                        }
                    }
                });

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Setting WatchFace...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            super.onPreExecute();

        }
    }
}
