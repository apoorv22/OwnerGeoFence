package com.example.dell_15.final_call;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.barcode.Barcode;
import com.parse.GetCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapsActivity_track extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    ArrayList<LatLng> pathList = new ArrayList<LatLng>();
    ArrayList<LatLng> leftFenceList = new ArrayList<LatLng>();
    ArrayList<LatLng> rightFenceList = new ArrayList<LatLng>();
    LatLng driverLoc;
    private PolylineOptions lineOpt;
    private PolygonOptions polyOpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_activity_track);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent i = getIntent();
        Bundle bPath = i.getBundleExtra("pathBundle");


        if (bPath != null) {
            //  list1 = (ArrayList<String>) b.getStringArrayList("list");
            pathList = bPath.getParcelableArrayList("pathLatLng");
            leftFenceList = bPath.getParcelableArrayList("leftFenceLatLng");
            rightFenceList = bPath.getParcelableArrayList("rightFenceLatLng");
            driverLoc = bPath.getParcelable("driverLoc");

            Log.d("pathList", pathList.toString());
        }

        initializemap();


        Button updateBtn = (Button)findViewById(R.id.btn_update);

        updateBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> query1 = ParseQuery.getQuery("PathFence");
                query1.orderByDescending("updatedAt").setLimit(1);
                query1.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject objects, com.parse.ParseException e) {
                        ParseGeoPoint pgPt = objects.getParseGeoPoint("DriverLocation");
                        Log.d("Point", pgPt.toString());
                        MarkerOptions mopt = new MarkerOptions();
                        mopt.position(new LatLng(pgPt.getLatitude(),pgPt.getLongitude())).title("Driver Location");

                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(pgPt.getLatitude(),pgPt.getLongitude())));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                        mMap.addMarker(mopt);
                    }
                });
            }
        });
    }

    private void initializemap() {
        setUpMapIfNeeded();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        Location myLocation = getLastKnownLocation();

        //set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        double latitude = myLocation.getLatitude();

        double longitude = myLocation.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You are here!"));

//        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(pathList.get(0)).title("Start"));
        mMap.addMarker(new MarkerOptions().position(pathList.get(pathList.size() - 1)).title("End"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pathList.get(0)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        lineOpt = new PolylineOptions();
        polyOpt = new PolygonOptions();

        lineOpt.addAll(pathList);
        lineOpt.width(10);
        lineOpt.color(Color.RED);

        mMap.addPolyline(lineOpt);

        Collections.reverse(rightFenceList);
        polyOpt.addAll(leftFenceList).strokeColor(Color.BLUE).strokeWidth(3);
        polyOpt.addAll(rightFenceList).strokeColor(Color.BLUE).strokeWidth(3);

        mMap.addPolygon(polyOpt);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    private Location getLastKnownLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {

            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
