package com.droidverine.beardfacedigital.beardfacecompanion.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.droidverine.beardfacedigital.beardfacecompanion.HomeActivity;
import com.droidverine.beardfacedigital.beardfacecompanion.R;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

/**
 * Created by Shivraj on 12/12/2018.
 */
public class CustomAdapter extends BaseAdapter {
    Context c;
    GoogleApiClient googleApiClient;
    ArrayList<Spacecraft> spacecrafts;
    HomeActivity homeActivity;

    public CustomAdapter(Context c, ArrayList<Spacecraft> spacecrafts, GoogleApiClient googleApiClient) {
        this.c = c;
        this.spacecrafts = spacecrafts;
        this.googleApiClient = googleApiClient;
    }

    @Override
    public int getCount() {
        return spacecrafts.size();
    }

    @Override
    public Object getItem(int i) {
        return spacecrafts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(c).inflate(R.layout.watchfaces_card_item, viewGroup, false);
        }

        final Spacecraft s = (Spacecraft) this.getItem(i);
        final ImageView img;

        img = (ImageView) view.findViewById(R.id.watchface_img);
        final TextView nameTxt = (TextView) view.findViewById(R.id.watchface_txt);
        nameTxt.setText(s.getName());
        img.setImageResource(s.getImage());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
ProgressDialog progressDialog=new ProgressDialog(c);
                Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
                homeActivity = new HomeActivity();
                progressDialog.setTitle("Setting Watchface");
progressDialog.setMessage(nameTxt.getText());
                progressDialog.show();
                homeActivity.syncWatch("ranodm", "w", 122, bitmap, c,progressDialog);


            }
        });

        return view;
    }
}








