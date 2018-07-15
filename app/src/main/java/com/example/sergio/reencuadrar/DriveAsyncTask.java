package com.example.sergio.reencuadrar;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

/**
 * Created by Sergio on 02/05/2018.
 */

public class DriveAsyncTask extends AsyncTask<Void, Sitio, Void> {
    private com.google.api.services.drive.Drive mService = null;
    private String idFolder = "", idFotoAsync = "", IDFotoCursor = "", nombreFoto = "";
    private final int DESCARGAR = 100, SUBIR = 99;
    private java.io.File ficheroFoto;
    private IDrive ifz;
    private Context aContext;
    private int accion;

    //Constructor para loguearse
    public DriveAsyncTask(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Reencuadrar")
                .build();
    }

    //Constructor para subir foto
    public DriveAsyncTask(GoogleAccountCredential credential, IDrive ifz, Context aContext, int accion, java.io.File ficheroFoto, String nombreFoto) {
        this.ifz = ifz;
        this.aContext = aContext;
        this.accion = accion;
        this.ficheroFoto = ficheroFoto;
        this.nombreFoto = nombreFoto;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Reencuadrar")
                .build();
    }

    //Constructor para descargar foto
    public DriveAsyncTask(GoogleAccountCredential credential, IDrive ifz, Context aContext, int accion, String IDFotoCursor) {
        this.ifz = ifz;
        this.aContext = aContext;
        this.accion = accion;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        this.IDFotoCursor = IDFotoCursor;
        mService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Reencuadrar")
                .build();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            //Si la accion es descargar compruebo que la carpeta esté creada y descargo la imagen
            if (accion == DESCARGAR) {
                if (getFolders()) {
                    //descargar foto
                    downloadFile();
                } else {
                    //Si no esta creada la creo
                    createFolder();
                    downloadFile();
                }
                //Si la accion es subir compruebo que la carpeta este creado y subo y bajo la imagen
            } else if (accion == SUBIR) {

                if (getFolders()) {
                    uploadFile();
                    downloadFile();
                } else {
                    createFolder();
                    uploadFile();
                    downloadFile();
                }

            }
        } catch (Exception e) {
            if (e instanceof UserRecoverableAuthIOException) {
                //Autenticacion google drive
                Intent anIntent = ((UserRecoverableAuthIOException) e).getIntent();
                anIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                aContext.getApplicationContext().startActivity(anIntent);
            } else {
                publishProgress(null);
            }
        }
        return null;
    }

    //Metodo para descargar la foto de google drive
    private void downloadFile() {
        ByteArrayOutputStream anOutputStream = new ByteArrayOutputStream();
        Bitmap bm = null;
        try {
            //Si aun no conozco el id de la foto del drive lo cogeré del metodo que he usado para subir la foto
            if (IDFotoCursor.isEmpty() || IDFotoCursor == null) {
                mService.files().get(idFotoAsync).executeMediaAndDownloadTo(anOutputStream);
                byte[] bitmapData = anOutputStream.toByteArray();
                bm = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                Sitio sitio = new Sitio();
                sitio.setIdFoto(idFotoAsync);
                sitio.setIcono(bm);
                publishProgress(sitio);
            } else {
                //Si lo conozco lo utilizo
                mService.files().get(IDFotoCursor).executeMediaAndDownloadTo(anOutputStream);
                byte[] bitmapData = anOutputStream.toByteArray();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                bm = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length, options);
                Sitio sitio = new Sitio();
                sitio.setIcono(bm);
                sitio.setIdFoto(IDFotoCursor);
                publishProgress(sitio);
            }
        } catch (IOException e) {
            publishProgress(null);
        }

    }

    //Método para subir una foto a la carpeta de google drive
    private void uploadFile() {
        File fileMetadata = new File();
        fileMetadata.setName(nombreFoto + ".jpg");
        fileMetadata.setParents(Collections.singletonList(idFolder));
        FileContent mediaContent = new FileContent("image/jpeg", ficheroFoto);
        try {
            Sitio sitio = new Sitio();
            File file = mService.files().create(fileMetadata, mediaContent)
                    .setFields("id, parents")
                    .execute();
            idFotoAsync = file.getId();
            sitio.setIdFoto(idFotoAsync);
            publishProgress(sitio);
        } catch (IOException e) {
            publishProgress(null);
        }
    }

    //Devolvemos el valor mediante el metodo de la interfaz
    @Override
    protected void onProgressUpdate(Sitio... values) {
        super.onProgressUpdate(values);
        if (values != null) {
            ifz.obtenerFoto(values[0]);
        } else {
            ifz.finalizar();
        }
    }
    //Método para crear la carpeta en la que almacenaremos las fotos a subir a google drive
    private void createFolder() {
        File fileMetadata = new File();
        fileMetadata.setName("ReencuadrarFotos");
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        try {
            File file = mService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            idFolder = file.getId();
        } catch (IOException e) {
            publishProgress(null);
        }
    }

    //Método que devuelve si la carpeta en la que se va a almacenar las fotos que subiremos a google drive está creada
    public boolean getFolders() {
        FileList result = null;
        boolean r = false;
        try {
            result = mService.files().list()
                    .setQ("mimeType = 'application/vnd.google-apps.folder'").setSpaces("drive").
                            setFields("files(id, name)").execute();
            for (File file : result.getFiles()) {
                if (file.getName().equals("ReencuadrarFotos")) {
                    idFolder = file.getId();
                    r = true;
                    break;
                } else {
                    r = false;
                }
            }
        } catch (UserRecoverableAuthIOException e) {
            Intent anIntent = e.getIntent();
            anIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            aContext.getApplicationContext().startActivity(anIntent);
        } catch (IOException e) {
            publishProgress(null);
        } catch (Exception e) {
            publishProgress(null);
        }
        return r;
    }
}

