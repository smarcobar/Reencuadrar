package com.example.sergio.reencuadrar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class AnyadirLugarActivity extends AppCompatActivity implements IGeocoder, IDrive {
    private final int PERMISSIONRESULT = 2;
    final IGeocoder ifz = this;
    final IDrive ifzDrive = this;
    //Peticion autocomplete
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyCkaptcrTl1ifE1XBq2tjLM-oUWso7IbYs";
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 3;
    private String NameOfFolder = "/Reencuadrar", pictureImagePath = "";
    LottieAnimationView animacion, cameraAnimacion;
    ImageView fotoEstableciemiento;
    Spinner spinnerCategorias, spinnerZonas;
    OpenHelper aHelper;
    EditText nombreEst, descEst;
    AutoCompleteTextView anAutoCompleteTextView;
    Sitio sitioAnyadir;
    private final int SUBIR = 99;
    File filetoUpload;
    Button aButton;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anyadir_lugar);
        anAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        anAutoCompleteTextView.setAdapter(new GoogleAutoCompleteAdapter(this, R.layout.list_item));
        nombreEst = (EditText) findViewById(R.id.editTextNombreEstAnyadir);
        descEst = (EditText) findViewById(R.id.editTextDescEstAnyadir);
        //Se hará visible al cargar la foto realizada por el usuario
        fotoEstableciemiento = (ImageView) findViewById(R.id.imageViewFotoEst);
        fotoEstableciemiento.setVisibility(View.INVISIBLE);
        spinnerCategorias = (Spinner) findViewById(R.id.spinnerCategorias);
        spinnerZonas = (Spinner) findViewById(R.id.spinnerZonas);
//Hacer desplegable con las categorias de establecimientos
        ArrayAdapter<String> adapterCategorias = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.categorias));
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategorias.setAdapter(adapterCategorias);
        //Hacer desplegable con las zonas de establecimientos
        ArrayAdapter<String> adapterZonas = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.zonas));
        adapterZonas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerZonas.setAdapter(adapterZonas);
        aHelper = new OpenHelper(this);
        aHelper.openDB();
        sitioAnyadir = new Sitio();
        cameraAnimacion = (LottieAnimationView) findViewById(R.id.cameraAnmation);
        cameraAnimacion.loop(true);
        cameraAnimacion.setAnimation("camera.json");
        cameraAnimacion.playAnimation();
        cameraAnimacion.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Botón para añadir la foto para el establecimiento", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONRESULT);
        }else{
            if (!cameraAnimacion.isEnabled()){
                cameraAnimacion.setEnabled(true);
            }
        }
        aButton = (Button) findViewById(R.id.buttonGuardarAnyadirSitio);
        aButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Botón para añadir establecimiento.", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        animacion = (LottieAnimationView) findViewById(R.id.loading);
        animacion.loop(true);
        animacion.setAnimation("loading.json");

        fotoEstableciemiento.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Botón para añadir la foto para el establecimiento", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    //Comprobar si dispone de conexion a internet
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void addImagen(View v) {
        if (validaTodo()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONRESULT);
            } else {
                if (isDeviceOnline()) {
                    File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + NameOfFolder);
                    pictureImagePath = storageDir.getAbsolutePath() + "/" + nombreEst.getText().toString() + ".jpg";
                    File file = new File(pictureImagePath);
                    //A partir de la api 24 la uri cambia de file:// a content:// esto hace que se pueda seguir usando
                    if (Build.VERSION.SDK_INT >= 24) {
                        try {
                            Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                            m.invoke(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (mkFolder(storageDir.getAbsolutePath()) != 0) {
                        Uri outputFileUri = Uri.fromFile(file);
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                        startActivityForResult(cameraIntent, REQUEST_CODE_CAPTURE_IMAGE);
                    } else {
                        Toast.makeText(this, "No se puede crear la foto", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    public void anyadirSitio(View v) {
        if (validaTodo()) {
            if (isDeviceOnline()) {
                GeocoderAsyncTask anAsyncTask = new GeocoderAsyncTask(ifz, getApplicationContext());
                anAsyncTask.execute(anAutoCompleteTextView.getText().toString().replace(" ", "+"));
            } else {
                Toast.makeText(getApplicationContext(), "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public int mkFolder(String folderName) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return 0;
        }
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return 0;
        }
        if (ActivityCompat.checkSelfPermission(this, // request permission when it is not granted.
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONRESULT);
            }
        }
        File folder = new File(folderName);
        int result = 0;
        if (folder.exists()) {
            result = 2; // folder exist
        } else {
            try {
                if (folder.mkdirs()) {
                    result = 1; // folder created
                } else {
                    result = 0; // creat folder fails
                }
            } catch (Exception ecp) {
                ecp.printStackTrace();
            }
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CAPTURE_IMAGE:
                    File imgfile = new File(pictureImagePath);
                    if (imgfile.exists()) {
                        Bitmap bm = BitmapFactory.decodeFile(imgfile.getAbsolutePath());
                        SaveImageOnGallery(rotateImage(bm, getCameraPhotoOrientation(imgfile.getAbsolutePath())), nombreEst.getText().toString());
                        if (isDeviceOnline()) {
                            if (cameraAnimacion.isAnimating()) {
                                cameraAnimacion.cancelAnimation();
                                cameraAnimacion.setVisibility(View.INVISIBLE);
                            }
                            if (fotoEstableciemiento.getVisibility() == View.VISIBLE) {
                                fotoEstableciemiento.setVisibility(View.INVISIBLE);
                            }
                            animacion.setVisibility(View.VISIBLE);
                            animacion.playAnimation();
                            DriveAsyncTask n = new DriveAsyncTask(PrincipalActivity.mCredential, ifzDrive, getApplicationContext(), SUBIR, imgfile, nombreEst.getText().toString());
                            n.execute();
                        } else {
                            Toast.makeText(getApplicationContext(), "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No se pudo subir la foto", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    //Rotar la imagen dependiendo de los grados
    public static Bitmap rotateImage(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    //Obtener rotacion de la fotografia
    public int getCameraPhotoOrientation(String imagePath) {
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    private boolean validaTodo() {
        boolean result = false;
        if (nombreEst.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Debes rellenar el nombre del establecimiento", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            result = true;
        }
        if (descEst.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Debes rellenar la descripción del establecimiento", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            result = true;
        }
        if (anAutoCompleteTextView.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Debes rellenar la dirección del establecimiento", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            result = true;
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == PERMISSIONRESULT) && (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            if (!isDeviceOnline()) {
                Toast.makeText(getApplicationContext(), "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
            } else {
                if (!cameraAnimacion.isEnabled()){
                    cameraAnimacion.setEnabled(true);
                }
                if (!aButton.isEnabled())
                    aButton.setEnabled(true);
            }
        } else {
            if (aButton.isEnabled())
                aButton.setEnabled(false);
            if (cameraAnimacion.isEnabled()){
                cameraAnimacion.setEnabled(false);
            }
        }
    }

    @Override
    public void direccionACoordenada(Sitio sitio) {
        //se asigna latitud y longitud en asynctask, después se inserta
        if (sitio != null) {
            sitioAnyadir.setNombre(nombreEst.getText().toString());
            sitioAnyadir.setDescripcion(descEst.getText().toString());
            sitioAnyadir.setDireccion(anAutoCompleteTextView.getText().toString());
            sitioAnyadir.setZona(String.valueOf(spinnerZonas.getSelectedItemPosition()));
            sitioAnyadir.setCategoria(String.valueOf(spinnerCategorias.getSelectedItem()));
            Log.i("lat", sitio.getLatitud());
            sitioAnyadir.setFavorito(false);
            if (sitio.getLatitud() != null) {
                sitioAnyadir.setLatitud(sitio.getLatitud());
            }
            if (sitio.getLongitud() != null) {
                sitioAnyadir.setLongitud(sitio.getLongitud());
            }
            if (validaTodo()) {
                if (sitioAnyadir.getIcono() != null) {
                    aHelper.insert(sitioAnyadir);
                    Toast.makeText(getApplicationContext(), nombreEst.getText().toString() + " insertado con éxito.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Tienes que establecer una foto para añadir el establecimiento.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            //Si se quiere hacer mundial quito el country
            sb.append("&components=country:es");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            return resultList;
        } catch (IOException e) {
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    @Override
    public void finalizar() {
        finish();
    }

    @Override
    public void obtenerFoto(Sitio sitio) {
        if (sitio != null) {
            if (sitio.getIcono() != null) {
                //Obtenemos bitmap
                sitioAnyadir.setIcono(sitio.getIcono());
                cameraAnimacion.cancelAnimation();
                cameraAnimacion.setVisibility(View.INVISIBLE);
                fotoEstableciemiento.setVisibility(View.VISIBLE);
                fotoEstableciemiento.setImageBitmap(sitioAnyadir.getIcono());
                animacion.setVisibility(View.INVISIBLE);
                animacion.cancelAnimation();
            } else if (sitio.getIdFoto() != null) {
                //Obtenemos id de foto
                sitioAnyadir.setIdFoto(sitio.getIdFoto());
            }
        } else {
            if (animacion.isAnimating()) {
                animacion.setVisibility(View.INVISIBLE);
                animacion.cancelAnimation();
                if (!cameraAnimacion.isAnimating()) {
                    cameraAnimacion.setVisibility(View.VISIBLE);
                    cameraAnimacion.playAnimation();
                }
            } else if (!cameraAnimacion.isAnimating()) {
                cameraAnimacion.setVisibility(View.VISIBLE);
                cameraAnimacion.playAnimation();
            } else if (fotoEstableciemiento.getVisibility() == View.VISIBLE) {
                fotoEstableciemiento.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void SaveImageOnGallery(Bitmap ImageToSave, String NameOfFile) {
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + NameOfFolder;
        if (Environment.getExternalStorageState() == null) {
            file_path = Environment.getDataDirectory().getAbsolutePath() + NameOfFolder;
        }
        File dir = new File(file_path);
        filetoUpload = new File(dir, NameOfFile + ".jpg");
        try {
            FileOutputStream fOut = new FileOutputStream(filetoUpload);
            ImageToSave.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
            MakeSureFileWasCreatedThenMakeAvailable(filetoUpload);
            AbleToSave();
        } catch (FileNotFoundException e) {
            UnableToSave();
        } catch (IOException e) {
            UnableToSave();
        }
    }

    private void MakeSureFileWasCreatedThenMakeAvailable(File file) {
        MediaScannerConnection.scanFile(getApplicationContext(),
                new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
    }

    private void UnableToSave() {
        Toast.makeText(getApplicationContext(), "¡No se ha podido guardar la imagen!", Toast.LENGTH_SHORT).show();
    }

    private void AbleToSave() {
        Toast.makeText(getApplicationContext(), "Imagen guardada en la galería.", Toast.LENGTH_SHORT).show();
    }

    public class GoogleAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public GoogleAutoCompleteAdapter(Context context, int ResourceId) {
            super(context, ResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }
}




