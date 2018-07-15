package com.example.sergio.reencuadrar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LugarActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    OpenHelper aHelper;
    CursorAdapter anAdapter;
    ListView listaLugares;
    TextView nombre, mail;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lugar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);//helper
        View headerView = navigationView.getHeaderView(0);
        nombre = (TextView) headerView.findViewById(R.id.NombreUsuarioLugar);
        mail = (TextView) headerView.findViewById(R.id.mailUsuarioLugar);
        aHelper = new OpenHelper(this);
        Intent intentZona = getIntent();
        //cogemos la zona que hemos pasado
        int zona = intentZona.getIntExtra(Sitio.ZONA, 0);
        String correo = intentZona.getStringExtra("MAIL");
        listaLugares = (ListView) findViewById(R.id.listaLugares);
        aHelper.openDB();
        Cursor aCursor;
        aCursor = aHelper.queryZonas(zona);
        anAdapter = new CursorAdapter(this, aCursor);
        listaLugares.setAdapter(anAdapter);
        listaLugares.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (isDeviceOnline()) {
                    Intent establecimientoIntent = new Intent(adapterView.getContext(), EstablecimientoActivity.class);
                    establecimientoIntent.putExtra("ID", Integer.parseInt(view.getTag().toString()));
                    startActivity(establecimientoIntent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
        prefs = getSharedPreferences(Personalizacion.ARCHIVO, MODE_PRIVATE);
        if (!prefs.getString(Personalizacion.NAME, "").isEmpty()) {
            nombre.setText(prefs.getString(Personalizacion.NAME, ""));
        } else {
            nombre.setText("REENCUADRAR");
        }
        mail.setText(correo);
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.anyadirSitio:
                if (isDeviceOnline()) {
                    Intent anIntent = new Intent(getApplicationContext(), AnyadirLugarActivity.class);
                    startActivity(anIntent);
                    finish();
                } else {
                    Toast.makeText(this, "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.favoritosOption:
                if (isDeviceOnline()) {
                    Intent intentFavoritos = new Intent(getApplicationContext(), LugarActivity.class);
                    intentFavoritos.putExtra(Sitio.ZONA, 99);
                    startActivity(intentFavoritos);
                    finish();
                } else {
                    Toast.makeText(this, "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.mapaBBDD:
                //mapa
                if (isDeviceOnline()) {
                    Intent intentMapaBBDD = new Intent(this, MapsActivity.class);
                    startActivity(intentMapaBBDD);
                    finish();
                } else {
                    Toast.makeText(this, "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sitiosCercanosOption:
                //api
                if (isDeviceOnline()) {
                    Intent intentSitiosCercanos = new Intent(this, SitiosCercanosActivity.class);
                    startActivity(intentSitiosCercanos);
                    finish();
                } else {
                    Toast.makeText(this, "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void onResume() {
        super.onResume();
        aHelper.openDB();
    }

    @Override
    protected void onPause() {
        super.onPause();
        aHelper.closeDB();
    }

}
