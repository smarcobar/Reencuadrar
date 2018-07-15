package com.example.sergio.reencuadrar;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Alumno on 20/02/2018.
 */

public class CursorAdapter extends android.widget.CursorAdapter implements IDrive {
    private Bitmap bm;
    private Sitio sitio;
    private Context context;
    private final int DESCARGAR = 100;
    private ArrayList<Sitio> sitios = new ArrayList<>();
    final IDrive ifz = this;

    public CursorAdapter(Context context, Cursor c) {
        super(context, c);
        bm = null;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater anInflater = ((Activity) context).getLayoutInflater();
        return anInflater.inflate(R.layout.row, parent, false);
    }


    // Uri uri = Uri.parse("https://www.googleapis.com/drive/v3/files/1C822Lr9zkI7r4Ms1wLb5SrFzqV3QYiOd");
    //  Picasso.with(context).load("https://drive.google.com/file/d/1dL9U6vu_lfr8QoH16o3U91YNbgjch9VN/view?usp=sharing").into(imagen);
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nombre = (TextView) view.findViewById(R.id.textViewNombreLugarCercano);
        TextView direccion = (TextView) view.findViewById(R.id.textViewDireccionCercano);
        ImageView imagen = (ImageView) view.findViewById(R.id.imageViewLugarCercano);
        this.context = context;
        sitio = new Sitio();
        sitio.set_id(cursor.getInt(0));
        sitio.setNombre(cursor.getString(1));
        sitio.setDireccion(cursor.getString(2));
        sitio.setRuta(cursor.getString(3));
        sitio.setIdFoto(cursor.getString(4));
        nombre.setText(sitio.getNombre());
        direccion.setText(sitio.getDireccion());
        //Si tenemos establecimiento de los cargados por defecto
        if (sitio.getRuta() != null && !sitio.getRuta().isEmpty()) {
            Bitmap mp = BitmapFactory.decodeResource(context.getResources(), Integer.parseInt(sitio.getRuta()));
            imagen.setImageBitmap(mp);
        } else {
            //Comprobamos si tiene foto asignada
            if (tieneFoto(sitios, sitio)) {
                //si tenemos icono
                if (sitio.getIcono() != null) {
                    //asignamos foto y notificamos
                    imagen.setImageBitmap(sitio.getIcono());
                    notifyDataSetChanged();
                }
            } else {
                //Pedimos a drive que nos traiga la foto
                DriveAsyncTask asyncTask = new DriveAsyncTask(PrincipalActivity.mCredential, ifz, context, DESCARGAR, sitio.getIdFoto());
                asyncTask.execute();
                imagen.setImageBitmap(bm);
            }
        }
        //primer sitio que añadimos al array
        if (sitios.isEmpty()) {
            sitios.add(sitio);
        } else {
            //Si no existe el sitio lo añadimos
            if (!existeSitio(sitios, sitio)) {
                sitios.add(sitio);
            }
        }
        //establecemos tag para luego poder llamar al establecimiento correcto
        view.setTag(dameTag(sitios, sitio));
    }

    //Metodo que devuelve el tag del sitio correspondiente
    private int dameTag(ArrayList<Sitio> sitios, Sitio sitio) {
        int result = 0;
        if (!sitios.isEmpty()) {
            for (Sitio sit : sitios) {
                if (sit.getNombre().equals(sitio.getNombre()) && sit.getDireccion().equals(sitio.getDireccion())) {
                    result = sitio.get_id();
                }
            }
        }
        return result;
    }

    //metodo para comprobar si existe el sitio
    private boolean existeSitio(ArrayList<Sitio> sitios, Sitio sitio) {
        boolean result = false;
        for (Sitio sit : sitios) {
            if (sit.getIdFoto() != null) {
                if (sit.getIdFoto().equals(sitio.getIdFoto())) {
                    result = true;
                }
            } else {
                if (sit.getNombre().equals(sitio.getNombre()) && sit.getDireccion().equals(sitio.getDireccion())) {
                    result = true;
                }
            }
        }
        return result;
    }

    //metodo para comprobar si tiene foto
    private boolean tieneFoto(ArrayList<Sitio> sitios, Sitio sitio) {
        boolean result = false;
        if (!sitios.isEmpty()) {
            for (Sitio sit : sitios) {
                if (sit.getIdFoto() != null && sit.getIdFoto().equals(sitio.getIdFoto()) && sit.getIcono() != null) {
                    sitio.setIcono(sit.getIcono());
                    result = true;
                }
            }
        }
        return result;
    }

    //metodo de la interfaz que nos devuelve la foto del drive
    @Override
    public void obtenerFoto(Sitio _sitio) {
        //Si el array no esta vacio
        if (!sitios.isEmpty()) {
            //buscamos el sitio al que pertenece
            for (Sitio sit : sitios) {
                if (sit.getIdFoto() != null && sit.getIdFoto().equals(_sitio.getIdFoto())) {
                    //si viene cargado el icono se lo asignamos y lo notificamos
                    if (_sitio.getIcono() != null) {
                        sit.setIcono(_sitio.getIcono());
                        notifyDataSetChanged();
                    }
                }
            }
        } else {
            //enchufamos foto y notificamos
            bm = _sitio.getIcono();
            notifyDataSetChanged();
        }
    }

    @Override
    public void finalizar() {
    }
}
