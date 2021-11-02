package com.example.loc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    Button button;
    Button copy;
    Button sharebtn;

    TextView TV;
    TextView TVacc;
    TextView TVspeed;
    TextView TVheight;
    boolean requestingLocationUpdates = false;
    private LocationCallback locationCallback;
    LocationRequest locationRequest;

    String currentloc = "null";

    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TVacc = findViewById(R.id.TVAcurracy);
        TVspeed = findViewById(R.id.TVspeed);
        copy = findViewById(R.id.copy);
        TVheight = findViewById(R.id.TVheight);
        copy.setEnabled(false);
        sharebtn = findViewById(R.id.sharebtn);
        sharebtn.setEnabled(false);


        button = findViewById(R.id.Btn);
        TV = findViewById(R.id.TV);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                MainActivity.this
        );




        sharebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, currentloc);
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, "Share your location!");
                startActivity(shareIntent);


            }
        });
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ClipboardManager cbm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);


                ClipData clip = ClipData.newPlainText("Location", currentloc);
                cbm.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "copied", Toast.LENGTH_SHORT).show();
            }
        });


        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                start();
            }
        });
    }

    public void updateAcurracy(String accuracy){

        TVacc.setText("accuracy: " + accuracy  + " m");
    }

    public  void updateSpeed(String speed){
        TVspeed.setText("speed: " + speed + " kph");
    }
    public  void updateHeight(String Altitude, String accuracy){
        TVheight.setText("altitude: " + Altitude + " m " + "±" + accuracy + "m");

    }
    public void start() {


        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
            sharebtn.setEnabled(true);
            copy.setEnabled(true);

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //maybe remove


        if (requestCode == 100 && grantResults.length > 0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            getCurrentLocation();
        } else {
            Toast.makeText(getApplicationContext(), "ERROR1", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    public void requestLocation() {
        LocationRequest locationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(10000).setFastestInterval(10000).setNumUpdates(3);
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location1 = locationResult.getLastLocation();
                // TV.setText("SUPER");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        start();
                    }
                }, 100);
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();


                    if (location != null) {
                        requestLocation();
                        currentloc = "Latitude: " + location.getLatitude() + "\n\nLongitude: " + location.getLongitude();
                        TV.setText(currentloc);
                        updateAcurracy(String.valueOf(location.getAccuracy()));

                        updateSpeed(String.valueOf(location.getSpeed()).subSequence(0,3).toString());
                        updateHeight(String.valueOf(location.getAltitude()).subSequence(0,3).toString(),  String.valueOf(location.getVerticalAccuracyMeters()).substring(0,3));


                        //TODO
                    } else {
                        requestLocation();

                    }

                }
            });
        } else {

            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        }
    }
}