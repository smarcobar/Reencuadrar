package com.example.sergio.reencuadrar;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SitiosCercanosActivity extends AppCompatActivity {
    String[] lugCercanos = {"BAR", "CAFETERIA", "CINE", "RESTAURANTE", "AEROPUERTO", "HOSPITAL", "IGLESIA", "POLICIA", "ESTADIO", "GIMNASIO", "GASOLINERA", "FARMACIA",
            "PARADA DE TAXIS", "TIENDA DE BICIS", "TIENDA DE ROPA","PELUQUERÍA","UNIVERSIDAD",
            "CENTRO COMERCIAL", "BOLERA"};
    ArrayAdapter anAdapter;
    ListView listaSitiosCercanos;
    public static final String TIPO = "TIPO";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sitios_cercanos);
        listaSitiosCercanos = (ListView) findViewById(R.id.listaLugaresCercanos);
        anAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, lugCercanos);
        listaSitiosCercanos.setAdapter(anAdapter);
        listaSitiosCercanos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //internet
                ConnectivityManager aManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo aInfo = aManager.getActiveNetworkInfo();
                if (aInfo != null && (aInfo.isConnected()) && (aInfo.getType() == ConnectivityManager.TYPE_WIFI || aInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
                    Intent anIntent = new Intent(parent.getContext(), SitiosCercanosPorCategoriaActivity.class);
                    anIntent.putExtra(TIPO, position);
                    startActivity(anIntent);
                } else {
                    Toast.makeText(parent.getContext(), "No tienes ninguna conexión wifi, ni red móvil, si desea acceder active una conexión", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
