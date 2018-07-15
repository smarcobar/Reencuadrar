package com.example.sergio.reencuadrar;

import android.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    OpenHelper aHelper;
    FusedLocationProviderClient cliente;
    LocationRequest aRequest;
    LocationCallback callback;
    private final int PERMISSIONRESULT = 1;
    SharedPreferences prefs;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        aHelper = new OpenHelper(this);
        aHelper.openDB();
        cliente = LocationServices.getFusedLocationProviderClient(this);
        aRequest = new LocationRequest();
        aRequest.setInterval(330000);
        aRequest.setFastestInterval(111000);
        aRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        prefs = getSharedPreferences(Personalizacion.ARCHIVO, MODE_PRIVATE);
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
// Añadir marcadores
        LatLng latLng;
        Cursor aCursor = aHelper.getAllData();
        MarkerOptions aMarker = new MarkerOptions();
        while (aCursor.moveToNext()) {
            //posicion de los lugares
            latLng = new LatLng(aCursor.getDouble(3), aCursor.getDouble(4));
            aMarker.position(latLng);
            aMarker.title(aCursor.getString(1));
            mMap.addMarker(aMarker).setTag(aCursor.getInt(0));
        }
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //tu posicion
                super.onLocationResult(locationResult);
                Location result = locationResult.getLastLocation();
                LatLng latLng = new LatLng(result.getLatitude(), result.getLongitude());
                MarkerOptions aMarker = new MarkerOptions();
                aMarker.position(latLng);
                if (!prefs.getString(Personalizacion.NAME, "").isEmpty()) {
                    aMarker.title(prefs.getString(Personalizacion.NAME, ""));
                } else {
                    aMarker.title("ESTAS AQUI");
                }
                if (prefs.getString(Personalizacion.GENERO, "").equals("chico")) {
                    BitmapDescriptor iconoChico = BitmapDescriptorFactory.fromResource(R.drawable.chico);
                    aMarker.icon(iconoChico);
                } else if (prefs.getString(Personalizacion.GENERO, "").equals("chica")) {
                    BitmapDescriptor iconoChica = BitmapDescriptorFactory.fromResource(R.drawable.chica);
                    aMarker.icon(iconoChica);
                } else {
                    BitmapDescriptor moñigote = BitmapDescriptorFactory.fromResource(R.drawable.icono);
                    aMarker.icon(moñigote);
                }
                mMap.addMarker(aMarker).showInfoWindow();
                CameraUpdate camMovement = CameraUpdateFactory.newLatLngZoom(latLng, 12);
                mMap.animateCamera(camMovement);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONRESULT);
        } else {
            if (isDeviceOnline()) {
                //hacemos peticion ya que hay permisos
                cliente.requestLocationUpdates(aRequest, callback, null);
            } else {
                Toast.makeText(this, "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
            }
        }
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.getTag() != null) {
                    if (isDeviceOnline()) {
                        Intent anIntent = new Intent(getApplicationContext(), EstablecimientoActivity.class);
                        anIntent.putExtra("ID", (int) marker.getTag());
                        startActivity(anIntent);
                    } else {
                        Toast.makeText(MapsActivity.this, "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                if (marker.getTag() != null) {
                    Toast.makeText(MapsActivity.this, "Ver este establecimiento", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity.this, "Estás situado aquí", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Comprobar si dispone de conexion a internet
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == PERMISSIONRESULT) && (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            if (isDeviceOnline()) {
                cliente.requestLocationUpdates(aRequest, callback, null);
            } else {
                Toast.makeText(this, "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
            }
        } else {
            finish();
        }
    }
}
