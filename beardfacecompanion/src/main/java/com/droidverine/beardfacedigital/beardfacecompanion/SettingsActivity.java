package com.droidverine.beardfacedigital.beardfacecompanion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.wearable.intent.RemoteIntent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Shivraj on 12/12/2018.
 */
public class SettingsActivity extends AppCompatActivity implements  CapabilityClient.OnCapabilityChangedListener {

    private static final String TAG = "MainMobileActivity";

    private static final String WELCOME_MESSAGE = "Welcome to our Mobile app!\n\n";

    private static final String CHECKING_MESSAGE =
            WELCOME_MESSAGE + "Checking for Wear Devices for app...\n";

    private static final String NO_DEVICES =
            WELCOME_MESSAGE
                    + "You have no Wear devices linked to your phone at this time.\n";

    private static final String MISSING_ALL_MESSAGE =
            WELCOME_MESSAGE
                    + "You are missing the Wear app on all your Wear Devices, please click on the "
                    + "button below to install it on those device(s).\n";

    private static final String INSTALLED_SOME_DEVICES_MESSAGE =
            WELCOME_MESSAGE
                    + "Wear app installed on some your device(s) (%s)!\n\nYou can now use the "
                    + "MessageApi, DataApi, etc.\n\n"
                    + "To install the Wear app on the other devices, please click on the button "
                    + "below.\n";

    private static final String INSTALLED_ALL_DEVICES_MESSAGE =
            WELCOME_MESSAGE
                    + "Wear app installed on all your devices (%s)!\n\nYou can now use the "
                    + "MessageApi, DataApi, etc.";

    // Name of capability listed in Wear app's wear.xml.
    // IMPORTANT NOTE: This should be named differently than your Phone app's capability.
    private static final String CAPABILITY_WEAR_APP = "verify_remote_example_wear_app";
    private static final String PLAY_STORE_APP_URI =
            "market://details?id=com.example.android.wearable.wear.wearverifyremoteapp";
    Boolean status;
    SharedPreferences.Editor editor;
    GoogleApiClient mGoogleApiClient;
    ProgressDialog progressDialog;
    private final ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.d("rrrrr", "onReceiveResult: " + resultCode);

            if (resultCode == RemoteIntent.RESULT_OK) {
                Toast toast = Toast.makeText(
                        getApplicationContext(),
                        "Play Store Request to Wear device successful.",
                        Toast.LENGTH_SHORT);
                toast.show();

            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                Toast toast = Toast.makeText(
                        getApplicationContext(),
                        "Play Store Request Failed. Wear device(s) may not support Play Store, "
                                + " that is, the Wear device may be version 1.0.",
                        Toast.LENGTH_LONG);
                toast.show();

            } else {
                throw new IllegalStateException("Unexpected result " + resultCode);
            }
        }
    };
    Switch screensw;
    private TextView mInformationTextView;
    private Button mRemoteOpenButton;

    private Set<Node> mWearNodesWithApp;
    private List<Node> mAllConnectedNodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        screensw=findViewById(R.id.ScreenSwitch);
        progressDialog = new ProgressDialog(this);

        Log.d("SettingsActivity", "onCreate()");
        mInformationTextView = findViewById(R.id.information_text_view);
        mRemoteOpenButton = findViewById(R.id.remote_open_button);
        mInformationTextView.setText(CHECKING_MESSAGE);

        mRemoteOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayStoreOnWearDevicesWithoutApp();
            }
        });
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
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();

        Wearable.getCapabilityClient(this).removeListener(this, CAPABILITY_WEAR_APP);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();

        Wearable.getCapabilityClient(this).addListener(this, CAPABILITY_WEAR_APP);

        // Initial request for devices with our capability, aka, our Wear app installed.
        findWearDevicesWithApp();

        // Initial request for all Wear devices connected (with or without our capability).
        // Additional Note: Because there isn't a listener for ALL Nodes added/removed from network
        // that isn't deprecated, we simply update the full list when the Google API Client is
        // connected and when capability changes come through in the onCapabilityChanged() method.
        findAllWearDevices();
    }

    private void openPlayStoreOnWearDevicesWithoutApp() {
        Log.d("openApp", "openPlayStoreOnWearDevicesWithoutApp()");

        // Create a List of Nodes (Wear devices) without your app.
        ArrayList<Node> nodesWithoutApp = new ArrayList<>();

        for (Node node : mAllConnectedNodes) {
            if (!mWearNodesWithApp.contains(node)) {
                nodesWithoutApp.add(node);
            }
        }

        if (!nodesWithoutApp.isEmpty()) {
            Log.d("openApp", "Number of nodes without app: " + nodesWithoutApp.size());

            Intent intent =
                    new Intent(Intent.ACTION_VIEW)
                            .addCategory(Intent.CATEGORY_BROWSABLE)
                            .setData(Uri.parse(PLAY_STORE_APP_URI));

            for (Node node : nodesWithoutApp) {
                RemoteIntent.startRemoteActivity(
                        getApplicationContext(),
                        intent,
                        mResultReceiver,
                        node.getId());
            }
        }
    }

    //Below code is for sending install app from playstore request to andorid wear

    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged(): " + capabilityInfo);

        mWearNodesWithApp = capabilityInfo.getNodes();

        // Because we have an updated list of devices with/without our app, we need to also update
        // our list of active Wear devices.
        findAllWearDevices();

        verifyNodeAndUpdateUI();
    }

    private void findWearDevicesWithApp() {
        Log.d(TAG, "findWearDevicesWithApp()");

        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(this)
                .getCapability(CAPABILITY_WEAR_APP, CapabilityClient.FILTER_ALL);

        capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
            @Override
            public void onComplete(Task<CapabilityInfo> task) {

                if (task.isSuccessful()) {
                    Log.d(TAG, "Capability request succeeded.");

                    CapabilityInfo capabilityInfo = task.getResult();
                    mWearNodesWithApp = capabilityInfo.getNodes();

                    Log.d(TAG, "Capable Nodes: " + mWearNodesWithApp);

                    verifyNodeAndUpdateUI();

                } else {
                    Log.d(TAG, "Capability request failed to return any results.");
                }
            }
        });
    }

    private void findAllWearDevices() {
        Log.d(TAG, "findAllWearDevices()");

        Task<List<Node>> NodeListTask = Wearable.getNodeClient(this).getConnectedNodes();

        NodeListTask.addOnCompleteListener(new OnCompleteListener<List<Node>>() {
            @Override
            public void onComplete(Task<List<Node>> task) {

                if (task.isSuccessful()) {
                    Log.d(TAG, "Node request succeeded.");
                    mAllConnectedNodes = task.getResult();

                } else {
                    Log.d(TAG, "Node request failed to return any results.");
                }

                verifyNodeAndUpdateUI();
            }
        });
    }

    private void verifyNodeAndUpdateUI() {
        Log.d(TAG, "verifyNodeAndUpdateUI()");

        if ((mWearNodesWithApp == null) || (mAllConnectedNodes == null)) {
            Log.d(TAG, "Waiting on Results for both connected nodes and nodes with app");

        } else if (mAllConnectedNodes.isEmpty()) {
            Log.d(TAG, NO_DEVICES);
            mInformationTextView.setText(NO_DEVICES);
            mRemoteOpenButton.setVisibility(View.INVISIBLE);

        } else if (mWearNodesWithApp.isEmpty()) {
            Log.d(TAG, MISSING_ALL_MESSAGE);
            mInformationTextView.setText(MISSING_ALL_MESSAGE);
            mRemoteOpenButton.setVisibility(View.VISIBLE);

        } else if (mWearNodesWithApp.size() < mAllConnectedNodes.size()) {
            // TODO: Add your code to communicate with the wear app(s) via
            // Wear APIs (MessageApi, DataApi, etc.)

            String installMessage =
                    String.format(INSTALLED_SOME_DEVICES_MESSAGE, mWearNodesWithApp);
            Log.d(TAG, installMessage);
            mInformationTextView.setText(installMessage);
            mRemoteOpenButton.setVisibility(View.VISIBLE);

        } else {
            // TODO: Add your code to communicate with the wear app(s) via
            // Wear APIs (MessageApi, DataApi, etc.)

            String installMessage =
                    String.format(INSTALLED_ALL_DEVICES_MESSAGE, mWearNodesWithApp);
            Log.d(TAG, installMessage);
            mInformationTextView.setText(installMessage);
            mRemoteOpenButton.setVisibility(View.INVISIBLE);

        }
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
