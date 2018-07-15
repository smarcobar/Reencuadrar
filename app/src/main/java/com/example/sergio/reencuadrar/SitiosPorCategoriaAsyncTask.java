package com.example.sergio.reencuadrar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by Sergio on 27/02/2018.
 */

public class SitiosPorCategoriaAsyncTask extends AsyncTask<String, Sitio, Void> {
    private static final String KEY = "AIzaSyCkaptcrTl1ifE1XBq2tjLM-oUWso7IbYs";
    private static final String STATUS = "status"; //if ok, a leer
    private static final String RESULTS = "results";//array
    private static final String GEOMETRY = "geometry";
    private static final String LOCATION = "location";
    private static final String LATITUD = "lat";
    private static final String LONGITUD = "lng";
    private static final String NAME = "name";
    private static final String VICINITY = "vicinity";
    private static final String RATING = "rating";
    private static final String PHOTOS = "photos";//array fotos
    private static final String REFERENCE = "photo_reference";
    private static final String WIDTH = "width";
    private static final String ICON = "icon";
    private static final String OPENING_HOURS = "opening_hours";
    private static final String OPEN_NOW = "open_now";
    private ISitiosCercanosPorCategoria ifz;
    Context miContext;
    ArrayList lugaresCercanos;

    public SitiosPorCategoriaAsyncTask(ISitiosCercanosPorCategoria ifz, Context aContext, ArrayList lugaresCercanos) {
        this.ifz = ifz;
        this.miContext = aContext;
        this.lugaresCercanos=lugaresCercanos;
    }

    @Override
    protected Void doInBackground(String... params) {
        String latitud = params[0];
        String longitud = params[1];
        String tipo = params[2];
        if (!latitud.isEmpty() && !longitud.isEmpty() && !tipo.isEmpty()) {
            //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=41.6291366,-0.9350763&radius=1000&types=movie_theater&key=AIzaSyCkaptcrTl1ifE1XBq2tjLM-oUWso7IbYs
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?radius=1000&location=" + latitud + "," + longitud + "&types=" + tipo + "&key=" + KEY;
            try {
                lugaresCercanos.clear();
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
                    Sitio sitio;
                    for (int i = 0; i < anArray.length(); i++) {
                        sitio = new Sitio();
                        JSONObject contenedor = anArray.getJSONObject(i);
                        JSONObject geometry = contenedor.getJSONObject(GEOMETRY);
                        JSONObject location = geometry.getJSONObject(LOCATION);
                        sitio.setLatitud(location.getString(LATITUD));
                        sitio.setLongitud(location.getString(LONGITUD));
                        sitio.setNombre(contenedor.getString(NAME));
                        sitio.setDireccion(contenedor.getString(VICINITY));
                        try {
                            //Recorremos array de fotos y obtenemos la primera
                            JSONArray photo = contenedor.getJSONArray(PHOTOS);
                            JSONObject firstPhoto = photo.getJSONObject(0);
                            String UrlFoto = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=" + firstPhoto.getInt(WIDTH) + "&photoreference=" + firstPhoto.getString(REFERENCE) + "&key=" + KEY;
                            sitio.setIcono(fotoAPI(UrlFoto, contenedor.getString(ICON)));
                        } catch (JSONException ex) {
                            sitio.setIcono(fotoAPI(contenedor.getString(ICON), ""));
                        }
                        try {
                            JSONObject opening = contenedor.getJSONObject(OPENING_HOURS);
                            if (opening.getBoolean(OPEN_NOW)) {
                                sitio.setDescripcion("ABIERTO");
                            } else {
                                sitio.setDescripcion("CERRADO");
                            }
                        } catch (JSONException ex) {
                            sitio.setDescripcion("SIN DATOS");
                        }
                        try {
                            sitio.setValoracion(contenedor.getInt(RATING));
                        } catch (JSONException ex) {
                            sitio.setValoracion(-1);
                        }
                        //updates en el uithread, llama a onprogressupdate
                        publishProgress(sitio);
                    }
                    return null;
                } else {
                    publishProgress(null);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Sitio... values) {
        super.onProgressUpdate(values);
        if (values != null) {
            //llamo al método de la interfaz creada
            ifz.listaSitioxCategoria(values[0]);
        } else {
            Toast.makeText(miContext, "Lo sentimos no está disponible ningún sitio de esta categoría a tus alrededores", Toast.LENGTH_SHORT).show();
        }
    }

    public Bitmap fotoAPI(String url, String urlSinIcono) {
        Bitmap bm = null;
        InputStream is = null;
        BufferedInputStream bis = null;
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.connect();
            is = conn.getInputStream();
            bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            if (bm == null) {
                URLConnection conn2 = new URL(urlSinIcono).openConnection();
                conn2.connect();
                InputStream is2 = conn2.getInputStream();
                BufferedInputStream bis2 = new BufferedInputStream(is);
                bm = BitmapFactory.decodeStream(bis);
                bis2.close();
                is2.close();
            }
            bis.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bm;
    }
}
