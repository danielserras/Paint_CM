package com.example.paint;

import static org.osmdroid.tileprovider.util.StorageUtils.getStorage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;


public class MapActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private int PERMISSION_ID;
    private MapView mMapView;
    private MapController mMapController;
    private Context context;
    private Polyline line = new Polyline();
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private boolean draw = false;
    private Marker marker;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        IConfigurationProvider provider = Configuration.getInstance();
        provider.setUserAgentValue(BuildConfig.APPLICATION_ID);
        provider.setOsmdroidBasePath(getStorage());
        provider.setOsmdroidTileCache(getStorage());
        setContentView(R.layout.activity_map);

        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mMapView.setBuiltInZoomControls(true);
        mMapController = (MapController) mMapView.getController();
        mMapController.setZoom(15);
        GeoPoint gPt = new GeoPoint(39.399872, -8.224454);
        mMapController.setCenter(gPt);

        marker = new Marker(mMapView);
        marker.setPosition(gPt);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker.setIcon(context.getResources().getDrawable(R.drawable.ic_menu_mylocation));
        mMapView.getOverlays().add(marker);

        if(checkPermissions()){
            if(isLocationEnabled()){
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                buildLocationRequest();
                buildLocationCallBack();
                fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,
                        Looper.getMainLooper());
            }
            else{
                Toast.makeText(this, "Please turn on" + " your location...",
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
        else{
            requestPermissions();
        }
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(100);
        locationRequest.setSmallestDisplacement(1);
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location: locationResult.getLocations()){
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    GeoPoint geoPoint = new GeoPoint(latitude,longitude);
                    marker.setPosition(geoPoint);
                    mMapController.setCenter(geoPoint);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    if(draw){
                        draw(latitude,longitude);
                    }
                }
            }

        };
    }


   private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
   }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }

    private void draw(double latitude, double longitude){
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        mMapController.setCenter(geoPoint);
        line.addPoint(geoPoint);
        mMapView.getOverlays().add(line);
    }

    public void drawBtn(View view){
        Button b = findViewById(R.id.drawBtn);
        if(draw){
            draw = false;
            b.setText("Start Draw");
        }
        else{
            draw = true;
            b.setText("Stop Draw");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

}
