package com.example.sergio.reencuadrar;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class SitiosCercanosPorCategoriaActivity extends AppCompatActivity implements ISitiosCercanosPorCategoria {
    String[] categorias = {"bar", "cafe", "movie_theater", "restaurant", "airport", "hospital", "church", "police", "stadium", "gym", "gas_station", "pharmacy",
            "taxi_stand", "bicycle_store","clothing_store", "hair_care", "university", "shopping_mall", "bowling_alley"};
    ListView listado;
    SitiosCercanosAdapter anAdapter;
    ArrayList arraySitiosCercanos = new ArrayList();
    FusedLocationProviderClient cliente;
    LocationRequest aRequest;
    LocationCallback callback;
    private static final int RESULTPERMISSION = 1;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sitios_cercanos_por_categoria);
        cliente = LocationServices.getFusedLocationProviderClient(this);
        aRequest = new LocationRequest();
        aRequest.setInterval(330000);
        aRequest.setFastestInterval(110000);
        aRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        listado = (ListView) findViewById(R.id.listaSitiosXCategoria);
        anAdapter = new SitiosCercanosAdapter(this, R.layout.rownearsites, arraySitiosCercanos);
        listado.setAdapter(anAdapter);
        listado.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isDeviceOnline()) {
                    Sitio sitio = (Sitio) arraySitiosCercanos.get(position);
                    String uri = "google.navigation:q=" + sitio.getLatitud() + "," + sitio.getLongitud() + "&mode=w";
                    Intent intentMapeado = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                    intentMapeado.setPackage("com.google.android.apps.maps");
                    if (intentMapeado.resolveActivity(getPackageManager()) != null) {
                        startActivity(Intent.createChooser(intentMapeado, "Elige una aplicacion"));
                    }
                } else {
                    Toast.makeText(SitiosCercanosPorCategoriaActivity.this, "No se dispone de conexión a internet.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        final Intent anIntent = getIntent();
        final SitiosCercanosPorCategoriaActivity scpc = this;
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (isDeviceOnline()) {
                    Location result = locationResult.getLastLocation();
                    String[] datos = {String.valueOf(result.getLatitude()), String.valueOf(result.getLongitude()), categorias[anIntent.getIntExtra(SitiosCercanosActivity.TIPO, 0)]};
                    arraySitiosCercanos.clear();
                    SitiosPorCategoriaAsyncTask anAsyncTask = new SitiosPorCategoriaAsyncTask(scpc, getApplicationContext(), arraySitiosCercanos);
                    anAsyncTask.execute(datos);
                } else {
                    Toast.makeText(SitiosCercanosPorCategoriaActivity.this, "No se dispone de conexión a internet.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si no tenemos permisos, se solicitan
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, RESULTPERMISSION);
        } else {
            // Tenemos permisos
            if (isDeviceOnline()) {
                cliente.requestLocationUpdates(aRequest, callback, null);
            } else {
                Toast.makeText(this, "No se dispone de conexión a internet.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == RESULTPERMISSION) && (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            cliente.requestLocationUpdates(aRequest, callback, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isDeviceOnline()) {
            cliente.removeLocationUpdates(callback);
        } else {
            Toast.makeText(this, "No se dispone de conexión a internet.", Toast.LENGTH_SHORT).show();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void refrescar(View v) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, RESULTPERMISSION);
        } else {
            if (isDeviceOnline()) {
                cliente.requestLocationUpdates(aRequest, callback, null);
            } else {
                Toast.makeText(this, "No se dispone de conexión a internet.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Comprobar si dispone de conexion a internet
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public void listaSitioxCategoria(Sitio sitio) {
        if (isDeviceOnline()) {
            //Añadimos sitio y notificamos que añadimos
            arraySitiosCercanos.add(sitio);
            anAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "No se dispone de conexión a internet.", Toast.LENGTH_SHORT).show();
        }
    }
}

