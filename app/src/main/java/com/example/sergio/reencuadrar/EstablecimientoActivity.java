package com.example.sergio.reencuadrar;

import android.*;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

public class EstablecimientoActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, IDrive {
    Sitio sitio;
    Button aButton;
    ImageView ImagenEstablecimiento, tipoEstablecimiento, nombreReproducir, botonFavorito;
    TextView titulo, descripcion, favoritos;
    OpenHelper aHelper;
    Cursor aCursor;
    FusedLocationProviderClient cliente;
    LocationRequest aRequest;
    LocationCallback callback;
    private final int PERMISSIONRESULT = 1, DESCARGAR = 100;
    private TextToSpeech tts;
    int id;
    static double lat;
    static double lng;
    IDrive ifz;
    AlertDialog actions;
    LottieAnimationView animacion;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_establecimiento);
        tts = new TextToSpeech(this, this);
        favoritos = (TextView) findViewById(R.id.textViewFavoritos);
        botonFavorito = (ImageView) findViewById(R.id.imageViewFavorito);
        //tooltip para botón de favoritos
        botonFavorito.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Botón para añadir un sitio a Favoritos", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        Intent anIntent = getIntent();
        aButton = (Button) findViewById(R.id.buttonLlevame);
        titulo = (TextView) findViewById(R.id.textViewTitulo);
        descripcion = (TextView) findViewById(R.id.textViewDescEstablecimiento);
        ImagenEstablecimiento = (ImageView) findViewById(R.id.imageViewEstablecimiento);
        tipoEstablecimiento = (ImageView) findViewById(R.id.imageViewTipoEstablecimiento);
        nombreReproducir = (ImageView) findViewById(R.id.imageViewNombre);
        nombreReproducir.setImageResource(R.drawable.audio);
        nombreReproducir.setEnabled(false);
        aHelper = new OpenHelper(this);
        aHelper.openDB();
        animacion = (LottieAnimationView) findViewById(R.id.loading);
        animacion.loop(true);
        animacion.setAnimation("loading.json");
        ifz = this;
        //filtramos por id que pasamos por parametro en el intent
        id = anIntent.getIntExtra("ID", 0);
        aCursor = aHelper.QueryEstablecimiento(id);
        aCursor.moveToFirst();
        //id, nom, desc, lat, lon,desc,cat, ruta, zona, favoritos, idfoto
        sitio = new Sitio(aCursor.getInt(0), aCursor.getString(1), aCursor.getString(2), aCursor.getString(3), aCursor.getString(4), aCursor.getString(5), aCursor.getString(6), aCursor.getString(7), aCursor.getString(8), aCursor.getInt(9), aCursor.getString(10));
        switch (sitio.getCategoria().toLowerCase()) {
            case "cafeteria":
                tipoEstablecimiento.setImageResource(R.drawable.cafeteria);
                break;
            case "tapas":
                tipoEstablecimiento.setImageResource(R.drawable.tapas);
                break;
            case "heladeria":
                tipoEstablecimiento.setImageResource(R.drawable.heladeria);
                break;
            case "discoteca":
                tipoEstablecimiento.setImageResource(R.drawable.discoteca);
                break;
        }
        titulo.setText(sitio.getNombre());
        descripcion.setText(sitio.getDescripcion());
        if (sitio.getRuta() != null) {
            ImagenEstablecimiento.setImageResource(Integer.parseInt(sitio.getRuta()));
        } else {
            if (isDeviceOnline()) {
                DriveAsyncTask asyncTask = new DriveAsyncTask(PrincipalActivity.mCredential, ifz, getApplicationContext(), DESCARGAR, sitio.getIdFoto());
                asyncTask.execute();
                animacion.setVisibility(View.VISIBLE);
                animacion.playAnimation();
            } else {
                Toast.makeText(this, "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
            }
        }
        if (sitio.isFavorito()) {
            botonFavorito.setImageResource(R.drawable.favorito);
            favoritos.setText("ES FAVORITO");
        } else {
            botonFavorito.setImageResource(R.drawable.nofavorito);
            favoritos.setText("NO ES FAVORITO");
        }
        //Localizacion
        cliente = LocationServices.getFusedLocationProviderClient(this);
        aRequest = new LocationRequest();
        aRequest.setInterval(5000);
        aRequest.setFastestInterval(1000);
        aRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//Localizacion con exactitud
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location result = locationResult.getLastLocation();
                lat = result.getLatitude();
                lng = result.getLongitude();
            }
        };
        //Solicitud de permisos
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONRESULT);
        } else {
            // Tenemos permisos

            cliente.requestLocationUpdates(aRequest, callback, null);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Cómo quieres llegar al establecimiento?");
        builder.setNeutralButton("Andando", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //abrimos google maps
                String uri = "google.navigation:q=" + sitio.getLatitud() + "," + sitio.getLongitud() + "&mode=w";
                Intent intentMapeado = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                intentMapeado.setPackage("com.google.android.apps.maps");
                if (intentMapeado.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(intentMapeado, "Elige una aplicacion"));
                }
            }
        });
        builder.setNegativeButton("En transporte", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String uriBus = "https://www.google.com/maps/dir/?api=1&origin=" + lat + "," + lng + "&destination=" + sitio.getLatitud() + "," + sitio.getLongitud() + "&travelmode=transit";
                Intent anIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uriBus));
                anIntent.setPackage("com.google.android.apps.maps");
                if (anIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(anIntent, "Elige una aplicacion"));
                }
            }
        });
        actions = builder.create();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteOption:
                aHelper.borrarEstablecimiento(id);
                Intent anIntent = new Intent(getApplicationContext(), PrincipalActivity.class);
                startActivity(anIntent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == PERMISSIONRESULT) && (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            cliente.requestLocationUpdates(aRequest, callback, null);

        }
    }

    //Mostrará diálogo para ir al establecimiento andando o en transporte
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void llevame(View v) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONRESULT);
        } else {
            if (isDeviceOnline()) {
                actions.show();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        cliente.removeLocationUpdates(callback);
    }

    //Si el textospeech está operativo habilitamos boton
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                nombreReproducir.setEnabled(false);
                Toast.makeText(this, "Idioma no disponible", Toast.LENGTH_SHORT).show();
            } else {
                nombreReproducir.setEnabled(true);
            }
        }
    }

    public void reproduceNombre(View v) {
        String text = titulo.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void makeFavorite(View v) {
        //Obtengo si el establecimiento esta en favoritos
        aCursor = aHelper.esFavorito(id);
        aCursor.moveToFirst();
        int result = aCursor.getInt(0);
        //Si no es favorito
        if (result == 0) {
            //Actualizo a favorito
            if (aHelper.favorito(id, false) == 1) {
                //Si va bien
                botonFavorito.setImageResource(R.drawable.favorito);
                Toast.makeText(getApplicationContext(), "Establecimiento añadido a favoritos", Toast.LENGTH_SHORT).show();
            } else {
                //Si va mal
                Toast.makeText(getApplicationContext(), "No se ha podido actualizar", Toast.LENGTH_SHORT).show();
            }
        } else {
            //si es favorito actualizo a no fav
            if (aHelper.favorito(id, true) == 1) {
                //si va bien
                botonFavorito.setImageResource(R.drawable.nofavorito);
                Toast.makeText(getApplicationContext(), "Establecimiento eliminado de favoritos", Toast.LENGTH_SHORT).show();
            } else {
                //si va mal
                Toast.makeText(getApplicationContext(), "No se ha podido actualizar", Toast.LENGTH_SHORT).show();
            }
        }
        //reinicio activity para refrescar datos en pantalla
        finish();
        startActivity(getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Al salir destruir el texttospeech
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void obtenerFoto(Sitio sitio) {
        if (isDeviceOnline()) {
            ImagenEstablecimiento.setImageBitmap(sitio.getIcono());
            animacion.setVisibility(View.INVISIBLE);
            animacion.cancelAnimation();
        } else {
            Toast.makeText(this, "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void finalizar() {
        if (!isDeviceOnline()) {
            Toast.makeText(this, "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}

