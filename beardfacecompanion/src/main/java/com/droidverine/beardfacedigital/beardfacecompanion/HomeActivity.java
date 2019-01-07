package com.droidverine.beardfacedigital.beardfacecompanion;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.TextView;
import com.droidverine.beardfacedigital.beardfacecompanion.Adapters.CustomAdapter;
import com.droidverine.beardfacedigital.beardfacecompanion.Adapters.Spacecraft;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Shivraj on 12/12/2018.
 */

public class HomeActivity extends AppCompatActivity {
//coolgit commit
    TextView connecteddevice;
    private GoogleApiClient mGoogleApiClient;
    Bitmap newImg;
    Context ctx;
    String nodename;
    ProgressDialog dlg;
    CustomAdapter adapter;
    GridView gv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        GridView gv = findViewById(R.id.gv);
        setSupportActionBar(toolbar);
        connecteddevice = (TextView) findViewById(R.id.connecteddevice);
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d("connection", "estabished");
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
        adapter = new CustomAdapter(this, getData(), mGoogleApiClient);
        gv.setAdapter(adapter);
        dlg = new ProgressDialog(this);

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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void syncWatch(String min, String max, int weatherId, Bitmap bmpy, Context ctx, ProgressDialog progressDialog) {
        dlg = progressDialog;

        newImg = bmpy;
        Log.d("chalo", "ssfsf");
        this.ctx = ctx;

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(ctx).addApi(Wearable.API).build();
            mGoogleApiClient.reconnect();
            new DownloadFilesTask().execute();
        } else {
            new DownloadFilesTask().execute();

        }

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
                Task<DataItem> dataItemTask = Wearable.getDataClient(ctx).putDataItem(request);

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


            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("Connected device name", nodename + "");
            dlg.dismiss();

            if (s != null && connecteddevice != null) {
                connecteddevice.setText("Connected Device : " + s);
            }
            super.onPostExecute(s);
        }
    }

    private ArrayList getData() {
        ArrayList<Spacecraft> spacecrafts = new ArrayList<>();

        Spacecraft s = new Spacecraft();
        s.setName("Droidverine");
        s.setImage(R.drawable.droidverine);
        spacecrafts.add(s);

        s = new Spacecraft();
        s.setName("Abut");
        s.setImage(R.drawable.ambip);
        spacecrafts.add(s);


        s = new Spacecraft();
        s.setName("BLah");
        s.setImage(R.drawable.droidverine);
        spacecrafts.add(s);


        s = new Spacecraft();
        s.setName("Venom");
        s.setImage(R.drawable.venom);
        spacecrafts.add(s);


        return spacecrafts;
    }

}
