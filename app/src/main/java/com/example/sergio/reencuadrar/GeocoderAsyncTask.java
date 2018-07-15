package com.example.sergio.reencuadrar;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sergio on 27/03/2018.
 */

public class GeocoderAsyncTask extends AsyncTask<String, Sitio, Void> {
    private static final String KEY = "AIzaSyBwrIAmUJueviKYFbBpmmFQYP1tncXLvlg";
    private static final String STATUS = "status"; //if ok, a leer
    private static final String RESULTS = "results";//array
    private static final String GEOMETRY = "geometry";
    private static final String LOCATION = "location";
    private static final String LATITUD = "lat";
    private static final String LONGITUD = "lng";
    private IGeocoder ifz;
    Context miContext;

    public GeocoderAsyncTask(IGeocoder ifz, Context miContext) {
        this.ifz = ifz;
        this.miContext = miContext;
    }

    @Override
    protected Void doInBackground(String... params) {
        String direccion = params[0];
        Log.i("TAG", direccion);
        //https://maps.googleapis.com/maps/api/geocode/json?address=Av.+de+la+Ilustraci%C3%B3n,+50012+Zaragoza&key=AIzaSyBwrIAmUJueviKYFbBpmmFQYP1tncXLvlg
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address="+direccion+"&key=AIzaSyBwrIAmUJueviKYFbBpmmFQYP1tncXLvlg";//"https://maps.googleapis.com/maps/api/geocode/json=?address=" + direccion + "&key=" + KEY;
        try {
            URL urlPeticion = new URL(url);
            HttpURLConnection httpConnection = (HttpURLConnection) urlPeticion.openConnection();
            httpConnection.setRequestMethod("GET");
            BufferedReader aReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            StringBuilder sBuilder = new StringBuilder();
            String line;
            while ((line = aReader.readLine()) != null) {
                sBuilder.append(line);
            }
            JSONObject objetoRaiz = new JSONObject(sBuilder.toString());
            if (objetoRaiz.getString(STATUS).equals("OK")) {
                JSONArray anArray = objetoRaiz.getJSONArray(RESULTS);
                Sitio sitio = new Sitio();
                for (int i = 0; i < anArray.length(); i++) {
                    JSONObject contenedor = anArray.getJSONObject(i);
                    JSONObject geometry = contenedor.getJSONObject(GEOMETRY);
                    JSONObject location = geometry.getJSONObject(LOCATION);
                    sitio.setLatitud(location.getString(LATITUD));
                    sitio.setLongitud(location.getString(LONGITUD));
                    publishProgress(sitio);
                }
            } else {
                publishProgress(null);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Sitio... values) {
        super.onProgressUpdate(values);
        if (values != null) {
            //llamo al método de la interfaz creada
            ifz.direccionACoordenada(values[0]);
        } else {
            Toast.makeText(miContext, "Lo sentimos la dirección introducida no es correcta", Toast.LENGTH_SHORT).show();
        }
    }
}
