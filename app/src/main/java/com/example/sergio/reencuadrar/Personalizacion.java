package com.example.sergio.reencuadrar;

import android.content.SharedPreferences;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class Personalizacion extends AppCompatActivity {
    public static final String NAME = "NAME";
    public static final String GENERO = "GENERO";
    public static final String ARCHIVO = "SHARED";
    public static final String EDAD = "EDAD";
    SharedPreferences preferencias;
    RadioButton chico, chica;
    EditText nombre, edad;
    TextInputLayout tILnombre, tILedad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalizacion);
        preferencias = getSharedPreferences(ARCHIVO, MODE_PRIVATE);
        chico = (RadioButton) findViewById(R.id.radioButtonChicoPers);
        chica = (RadioButton) findViewById(R.id.radioButtonChicaPers);
        nombre = (EditText) findViewById(R.id.editTextNombrePers);
        edad = (EditText) findViewById(R.id.editTextEdadPers);
        tILedad = (TextInputLayout) findViewById(R.id.tILEdadUsr);
        tILnombre = (TextInputLayout) findViewById(R.id.tILNombreUsr);
        String name = preferencias.getString(NAME, "");
        String anyos = preferencias.getString(EDAD, "");
        if (!name.isEmpty()) {
            nombre.setText(name);
            if (!preferencias.getString(GENERO, "chico").equals("chico")) {
                chica.setChecked(true);
            } else if (preferencias.getString(GENERO, "chico").equals("chico")) {
                chico.setChecked(true);
            }
            if (!anyos.isEmpty()) {
                edad.setText(anyos);
            }
        }
    }

    public void guardar(View v) {
        SharedPreferences.Editor anEditor = preferencias.edit();
        String nombreUser = nombre.getText().toString();
        String generoUser = "";
        String anyos = edad.getText().toString();
        if ((!nombreUser.isEmpty() && nombreUser != null)) {
            if ((!anyos.isEmpty() && anyos != null)) {
                if (Integer.parseInt(anyos) > 0) {
                    anEditor.putString(NAME, nombreUser);
                    if (chico.isChecked()) {
                        generoUser = "chico";
                    } else if (chica.isChecked()) {
                        generoUser = "chica";
                    }
                    anEditor.putString(GENERO, generoUser);
                    anEditor.putString(EDAD, anyos);
                    Toast.makeText(this, "Te llamas " + nombreUser + ", tu género es " + generoUser + ", y tienes " + anyos + " años.", Toast.LENGTH_SHORT).show();
                    anEditor.commit();
                    tILedad.setError(null);
                    tILnombre.setError(null);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    tILedad.setError("La edad debe ser mayor que 0");
                }
            } else {
                tILedad.setError("Debes rellenar la edad del usuario");
                if ((!nombreUser.isEmpty() && nombreUser != null)) {
                    tILnombre.setError(null);
                }
            }
        } else {
            tILnombre.setError("Debes rellenar el nombre del usuario");
        }
    }

    public void volver(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }

}

