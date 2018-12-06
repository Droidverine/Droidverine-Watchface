package com.droidverine.beardfacedigital.beardfacecompanion;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener//,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    TextView connecteddevice;
    Button btnsend, btnsend1;
    private GoogleApiClient mGoogleApiClient;
    Bitmap newImg;
    String nodename;
    ProgressDialog dlg;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        dlg = new ProgressDialog(this);
        connecteddevice = (TextView) findViewById(R.id.connecteddevice);
        btnsend = (Button) findViewById(R.id.sendbtn);
        btnsend1 = (Button) findViewById(R.id.sendbtn1);
        btnsend1.setOnClickListener(this);

        btnsend.setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d("connection", "estabished");
//                        new DownloadFilesTask().execute();
                        new DownloadFilesTask().execute();

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
        int id = v.getId();
        switch (id) {
            case R.id.sendbtn:
                newImg = BitmapFactory.decodeResource(getResources(), R.drawable.ambip);
                syncWatch("oo", "sjdskkk", 11, newImg);
                break;
            case R.id.sendbtn1:
                newImg = BitmapFactory.decodeResource(getResources(), R.drawable.droidverine);
                syncWatch("oo", "sjdskkk", 11, newImg);
                break;
        }

    }


    private void syncWatch(String min, String max, int weatherId, Bitmap bitmap) {
        new DownloadFilesTask().execute();
    }



    private class DownloadFilesTask extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... voids) {
            List<Node> connectedNodes =
                    Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();
            nodename = connectedNodes.get(0).getDisplayName();
            if (newImg != null) {

                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/weather-update");
                putDataMapRequest.getDataMap().putDouble("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
                final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                newImg.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
                Asset asset = Asset.createFromBytes(byteStream.toByteArray());
                putDataMapRequest.getDataMap().putAsset("weather-image", asset);
                PutDataRequest request = putDataMapRequest.asPutDataRequest();
                request.setUrgent();
                Task<DataItem> dataItemTask = Wearable.getDataClient(getApplicationContext()).putDataItem(request);

                dataItemTask.addOnSuccessListener(
                        new OnSuccessListener<DataItem>() {
                            @Override
                            public void onSuccess(DataItem dataItem) {
                                Log.d("Droidverine", " image sent successfully:");
                            }
                        });
                if (mGoogleApiClient == null) {
                    Log.v("Droidverine", "NOOOOOOOOOOOOOOOOO, life is no good");
                }

                Wearable.DataApi.putDataItem(mGoogleApiClient, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {


                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        if (!dataItemResult.getStatus().isSuccess()) {
                            Log.v("HomeActivity", "Something went wrong, watch was not notified");
                        } else {
                            Log.v("HomeActivity", "Success, Watch Notified");
                        }
                    }
                });
            }
            return nodename;
        }

        @Override
        protected void onPreExecute() {
            dlg.setMessage("Setting WatchFace...");
            dlg.setCancelable(false);
            dlg.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("Connected device name", nodename + "");
            dlg.dismiss();
            if (s != null) {
                connecteddevice.setText("Connected Device : " + s);
            }
            super.onPostExecute(s);
        }
    }
}
