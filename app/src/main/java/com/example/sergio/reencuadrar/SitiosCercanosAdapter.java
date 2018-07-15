package com.example.sergio.reencuadrar;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class SitiosCercanosAdapter extends ArrayAdapter {
    private Context context;
    private int resource;
    private ArrayList lugares;

    public SitiosCercanosAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList lugares) {
        super(context, resource, lugares);
        this.context = context;
        this.resource = resource;
        this.lugares = lugares;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater anInflater = ((Activity) context).getLayoutInflater();
            view = anInflater.inflate(resource, parent, false);
        }
        TextView nombre = (TextView) view.findViewById(R.id.textViewNombreLugarCercano);
        TextView direccion = (TextView) view.findViewById(R.id.textViewDireccionCercano);
        TextView valoracion = (TextView) view.findViewById(R.id.textViewRating);
        ImageView imagen = (ImageView) view.findViewById(R.id.imageViewLugarCercano);
        TextView opened = (TextView) view.findViewById(R.id.textViewOpened);
        if (lugares != null) {
            Sitio sitio = (Sitio) lugares.get(position);
            nombre.setText(sitio.getNombre());
            direccion.setText(sitio.getDireccion());
            if (sitio.getValoracion() != -1) {
                valoracion.setText(String.valueOf(sitio.getValoracion()) + " / 5");
            } else {
                valoracion.setText("ND");
            }
            imagen.setImageBitmap(sitio.getIcono());
            opened.setText(sitio.getDescripcion());
        }
        return view;
    }
}
