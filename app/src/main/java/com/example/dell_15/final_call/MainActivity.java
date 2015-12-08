package com.example.dell_15.final_call;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.barcode.Barcode;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener ,LocationListener{
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;

    List<ParseGeoPoint> pathList = new ArrayList<ParseGeoPoint>();
    List<ParseGeoPoint> leftFenceList = new ArrayList<ParseGeoPoint>();
    List<ParseGeoPoint> rightFenceList = new ArrayList<ParseGeoPoint>();

    ArrayList<LatLng> leftFenceLatLng = new ArrayList<LatLng>();
    ArrayList<LatLng> rightFenceLatLng = new ArrayList<LatLng>();
    ArrayList<LatLng> pathLatLng = new ArrayList<LatLng>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         //Parse code goes here
        //Parse.enableLocalDatastore(this);
//        Parse.initialize(this, "wuKOMiIyw9mo579ITKCuAR5lz5OoiIG1m5K9krEG", "BmQtKZdNaFr2Mn3Hi4cgFs1JOXLA3JYcB1KKEv8y");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        setTitle("Owner home");
        Button b=(Button)findViewById(R.id.button2);
        b.setEnabled(false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (checkGooglePlayServices()) {
            buildGoogleApiClient();
        }
        createLocationRequest();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button trackBtn = (Button)findViewById(R.id.track);

        trackBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> query1 = ParseQuery.getQuery("PathFence");
                query1.orderByDescending("updatedAt").setLimit(1);
                query1.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject objects, com.parse.ParseException e) {
                        ParseGeoPoint driverPt = objects.getParseGeoPoint("DriverLocation");
                        Log.d("Point", driverPt.toString());
//                        MarkerOptions mopt = new MarkerOptions();
//                        mopt.position(new LatLng(pgPt.getLatitude(),pgPt.getLongitude()));
//
//                        mMap.addMarker(mopt);
                            pathList = objects.getList("ParsePoints");
                            leftFenceList = objects.getList("ParseLeftPoints");
                            rightFenceList = objects.getList("ParseRightPoints");
                            Log.d("PathSize", String.valueOf(pathList.size()));
                            Log.d("PathSize", String.valueOf(leftFenceList.size()));
                            Log.d("PathSize", String.valueOf(rightFenceList.size()));

                            ParseGeoPoint pg = pathList.get(0);
                            Log.d("GeoPoint", String.valueOf(pg.getLatitude()));
                            Log.d("GeoPoint", String.valueOf(pg.getLongitude()));

                            for (int i = 0; i < pathList.size(); i++) {
                                ParseGeoPoint pgp = pathList.get(i);
//                            Log.d("GeoPoint", pgp.toString());
                                pathLatLng.add(new LatLng(pgp.getLatitude(), pgp.getLongitude()));

                                pgp = leftFenceList.get(i);
                                leftFenceLatLng.add(new LatLng(pgp.getLatitude(), pgp.getLongitude()));

                                pgp = rightFenceList.get(i);
                                rightFenceLatLng.add(new LatLng(pgp.getLatitude(), pgp.getLongitude()));
                            }

                            Log.d("PathLng", pathLatLng.toString());
//                        Log.d("PtList", pathLatLng.toString());
                            Intent intent = new Intent(MainActivity.this, MapsActivity_track.class);

                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList("pathLatLng", pathLatLng);
                            bundle.putParcelableArrayList("leftFenceLatLng", leftFenceLatLng);
                            bundle.putParcelableArrayList("rightFenceLatLng", rightFenceLatLng);
                            bundle.putParcelable("driverLoc", new LatLng(driverPt.getLatitude(), driverPt.getLongitude()));

                            intent.putExtra("pathBundle", bundle);
                            startActivity(intent);


                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    private boolean checkGooglePlayServices(){
        Context mContext=null;
        int checkGooglePlayServices = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {

            GooglePlayServicesUtil.getErrorDialog(checkGooglePlayServices,
                    this,10).show();

            return false;
        }

        return true;
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {

            Toast.makeText(this, "Latitude:" + mLastLocation.getLatitude() + ", Longitude:" + mLastLocation.getLongitude(), Toast.LENGTH_LONG).show();

        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }
    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (LocationListener) this);
    }
    @Override
    public void onLocationChanged(Location location) {
        TextView x = (TextView) findViewById(R.id.loc);
        x.setText("Waiting for Location");
        mLastLocation = location;
        Button btn=(Button)findViewById(R.id.button2);
        btn.setEnabled(true);
        Log.d("loc=", location.toString());

        //temp

        Toast.makeText(this, "Latitude:" + mLastLocation.getLatitude()+", Longitude:"+mLastLocation.getLongitude(),Toast.LENGTH_LONG).show();
        try {
            Geocoder geo = new Geocoder(MainActivity.this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);

            if (addresses.isEmpty()) {

                x.setText("Waiting for Location");
            }
            else {
                if (addresses.size() > 0) {
                    x.setText("You are currently at \n"+ addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                    //Toast.makeText(getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
    }
    public void onPause(Location location) {
        stopLocationUpdates();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }
    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }
    public void sendMessage(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
    public void sendMessage2(View view) {
        Intent intent = new Intent(this, Search.class);
        startActivity(intent);
    }
}
