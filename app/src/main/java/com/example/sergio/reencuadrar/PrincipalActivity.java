package com.example.sergio.reencuadrar;

import android.*;
import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.drive.DriveScopes;

import com.google.api.services.drive.model.*;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class PrincipalActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, EasyPermissions.PermissionCallbacks {

    String[] zonas = {"TUBO-CASCO", "UNIVERSIDAD", "MURALLAS", "LEON XIII", "TODAS LAS ZONAS"};
    ArrayAdapter anArrayAdapter;
    OpenHelper aHelper;
    ListView list;
    private final int PERSONALIZACION = 0;
    TextView nombre, email;
    SharedPreferences prefs;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE, DriveScopes.DRIVE_FILE};
    public static GoogleAccountCredential mCredential;
    NavigationView navigationView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        nombre = (TextView) headerView.findViewById(R.id.textViewFragmentNomUser);
        email = (TextView) headerView.findViewById(R.id.textViewFragmentEmailUser);
        aHelper = new OpenHelper(this);
        list = (ListView) findViewById(R.id.listViewZonasZaragoza);
        anArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, zonas);
        list.setAdapter(anArrayAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (isDeviceOnline()) {
                    Intent anIntent = new Intent(getApplicationContext(), LugarActivity.class);
                    anIntent.putExtra(Sitio.ZONA, i);
                    anIntent.putExtra("MAIL", email.getText().toString());
                    startActivity(anIntent);
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
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        getResultsFromApi();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.personalizarOption:
                Intent anIntent = new Intent(getApplicationContext(), Personalizacion.class);
                startActivityForResult(anIntent, PERSONALIZACION);
                break;
            /*case R.id.changeAccount:
                SharedPreferences settings = getPreferences(getApplicationContext().MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.remove(PREF_ACCOUNT_NAME);
                mCredential.setSelectedAccount(null);
                editor.apply();
                email.setText("");
                getResultsFromApi();

                break; */
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.anyadirSitio:
                if (isDeviceOnline()) {
                    Intent anIntent = new Intent(getApplicationContext(), AnyadirLugarActivity.class);
                    startActivity(anIntent);
                } else {
                    Toast.makeText(this, "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.favoritosOption:
                if (isDeviceOnline()) {
                    Intent intentFavoritos = new Intent(getApplicationContext(), LugarActivity.class);
                    intentFavoritos.putExtra(Sitio.ZONA, 99);
                    startActivity(intentFavoritos);
                } else {
                    Toast.makeText(this, "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.mapaBBDD:
                //mapa
                if (isDeviceOnline()) {
                    Intent intentMapaBBDD = new Intent(this, MapsActivity.class);
                    startActivity(intentMapaBBDD);
                } else {
                    Toast.makeText(this, "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sitiosCercanosOption:
                //api
                if (isDeviceOnline()) {
                    Intent intentSitiosCercanos = new Intent(this, SitiosCercanosActivity.class);
                    startActivity(intentSitiosCercanos);
                } else {
                    Toast.makeText(this, "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(getApplicationContext(), "No dispone de conexión a internet", Toast.LENGTH_SHORT).show();
        } else {
            new DriveAsyncTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, new String[]{android.Manifest.permission.GET_ACCOUNTS})) {
            String accountName = getPreferences(getApplicationContext().MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                email.setText(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "Esta aplicación necesita acceder a tu cuenta de google.",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    new String[]{android.Manifest.permission.GET_ACCOUNTS});
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_GET_ACCOUNTS && (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED)) {
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GOOGLE_PLAY_SERVICES:
                    getResultsFromApi();
                    break;
                case REQUEST_ACCOUNT_PICKER:
                    if (data != null && data.getExtras() != null) {
                        String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                        if (accountName != null) {
                            SharedPreferences settings = getPreferences(getApplicationContext().MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString(PREF_ACCOUNT_NAME, accountName);
                            editor.apply();
                            email.setText(accountName);
                            mCredential.setSelectedAccountName(accountName);
                            getResultsFromApi();
                        }
                    }
                    break;
                case REQUEST_AUTHORIZATION:
                    getResultsFromApi();
                    break;
                case PERSONALIZACION:
                    if (!prefs.getString(Personalizacion.NAME, "").isEmpty()) {
                        nombre.setText(prefs.getString(Personalizacion.NAME, ""));
                    } else {
                        nombre.setText("REENCUADRAR");
                    }
                    break;
            }
        } else {
            switch (requestCode) {
                case REQUEST_GOOGLE_PLAY_SERVICES:
                    Toast.makeText(this, "Esta aplicación necesita Google Play Services, porfavor instalalo en el dispositivo", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case REQUEST_ACCOUNT_PICKER:
                    finish();
                    break;
                case REQUEST_AUTHORIZATION:
                    finish();
                    break;
            }
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        finish();
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

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                PrincipalActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
}

