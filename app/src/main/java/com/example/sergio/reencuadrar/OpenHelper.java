package com.example.sergio.reencuadrar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Alumno on 20/02/2018.
 */

public class OpenHelper extends SQLiteOpenHelper {
    public static final String DBNAME = "SMLUGARES";
    public static final String TNAME = "SITIOS";
    //inserts
    public static final String INSERT = "INSERT INTO " + TNAME + " ( " + Sitio.NOMBRE + ", " + Sitio.DIRECCION + ", " + Sitio.LATITUD + ", " + Sitio.LONGITUD + ", " + Sitio.DESCRIPCION + ", " + Sitio.CATEGORIA + ", " + Sitio.RUTA + ", " + Sitio.ZONA + ", " + Sitio.FAVORITO + ") VALUES ('GRAN CAFÉ ZARAGOZANO','CALLE DEL COSO 35. PLAZA DE ESPAÑA',41.6526594,-0.8807107,'CAFETERÍA CÉNTRICA','CAFETERIA', " + R.drawable.cafezaragoza + ",0,1)";
    public static final String INSERT2 = "INSERT INTO " + TNAME + " ( " + Sitio.NOMBRE + ", " + Sitio.DIRECCION + ", " + Sitio.LATITUD + ", " + Sitio.LONGITUD + ", " + Sitio.DESCRIPCION + ", " + Sitio.CATEGORIA + ", " + Sitio.RUTA + ", " + Sitio.ZONA + ", " + Sitio.FAVORITO + ") VALUES ('CAFÉ VAN GOGH','CALLE ESPOZ Y MINA 12',41.6548384,-0.8787976, 'CAFETERÍA DE ESTILO', 'CAFETERIA', " + R.drawable.cafevangogh + ",0,0)";
    public static final String INSERT3 = "INSERT INTO " + TNAME + " ( " + Sitio.NOMBRE + ", " + Sitio.DIRECCION + ", " + Sitio.LATITUD + ", " + Sitio.LONGITUD + ", " + Sitio.DESCRIPCION + ", " + Sitio.CATEGORIA + ", " + Sitio.RUTA + ", " + Sitio.ZONA + ", " + Sitio.FAVORITO + ") VALUES ('BOCATART','CALLE PEDRO CERBUNA 9',41.6407987,-0.8969809, 'LOS BOCADILLOS ESTÁN MUY RICOS','TAPAS', " + R.drawable.bocatart + ",1,1)";
    public static final String INSERT4 = "INSERT INTO " + TNAME + " ( " + Sitio.NOMBRE + ", " + Sitio.DIRECCION + ", " + Sitio.LATITUD + ", " + Sitio.LONGITUD + ", " + Sitio.DESCRIPCION + ", " + Sitio.CATEGORIA + ", " + Sitio.RUTA + ", " + Sitio.ZONA + ", " + Sitio.FAVORITO + ") VALUES ('TORTOSA','CALLE DON JAIME I, 35',41.6541221,-0.89777635,'HELADOS RIQUÍSIMOS','HELADERIA'," + R.drawable.heladostortosa + ",0,1)";
    public static final String INSERT5 = "INSERT INTO " + TNAME + " ( " + Sitio.NOMBRE + ", " + Sitio.DIRECCION + ", " + Sitio.LATITUD + ", " + Sitio.LONGITUD + ", " + Sitio.DESCRIPCION + ", " + Sitio.CATEGORIA + ", " + Sitio.RUTA + ", " + Sitio.ZONA + ", " + Sitio.FAVORITO + ") VALUES ('KARAOKE PARADIS','RESIDENCIAL PARAISO, 2',41.644305,-0.884123,'A MOVER EL ESQUELETO','DISCOTECA' ," + R.drawable.karaokeparadis + ",3,0)";

    public static final int VERSION = 12;
    public static final String CREATETABLE = "CREATE TABLE " + TNAME + " (" + Sitio._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Sitio.NOMBRE + " TEXT, " + Sitio.DIRECCION + " TEXT, " + Sitio.LATITUD + " TEXT, " + Sitio.LONGITUD + " TEXT, " + Sitio.DESCRIPCION + " TEXT, " + Sitio.CATEGORIA + " TEXT, " + Sitio.RUTA + " TEXT, " + Sitio.ZONA + " TEXT, " + Sitio.FAVORITO + " INTEGER DEFAULT 0, " + Sitio.IDFOTO + " TEXT);";
    public static final String DROPTABLE = "DROP TABLE " + TNAME;
    public static final String TAG = "TAG";
    private SQLiteDatabase aDatabase;

    public OpenHelper(Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATETABLE);
        db.execSQL(INSERT);
        db.execSQL(INSERT2);
        db.execSQL(INSERT3);
        db.execSQL(INSERT4);
        db.execSQL(INSERT5);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROPTABLE);
        db.execSQL(CREATETABLE);
        db.execSQL(INSERT);
        db.execSQL(INSERT2);
        db.execSQL(INSERT3);
        db.execSQL(INSERT4);
        db.execSQL(INSERT5);
    }

    public Cursor QueryEstablecimiento(int idEstablecimiento) {
        //Informacion de establecimiento en concreto
        String[] columns = new String[]{Sitio._ID, Sitio.NOMBRE, Sitio.DIRECCION, Sitio.LATITUD, Sitio.LONGITUD, Sitio.DESCRIPCION, Sitio.CATEGORIA, Sitio.RUTA, Sitio.ZONA, Sitio.FAVORITO, Sitio.IDFOTO};
        String whereArgs = Sitio._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(idEstablecimiento)};
        return aDatabase.query(TNAME, columns, whereArgs, selectionArgs, null, null, null);
    }

    public Cursor getAllData() {
        //Toda la informacion de los lugares
        String[] columns = new String[]{Sitio._ID, Sitio.NOMBRE, Sitio.DIRECCION, Sitio.LATITUD, Sitio.LONGITUD, Sitio.DESCRIPCION, Sitio.CATEGORIA, Sitio.RUTA, Sitio.ZONA, Sitio.FAVORITO, Sitio.IDFOTO};
        Cursor aCursor = aDatabase.query(TNAME, columns, null, null, null, null, null);
        return aCursor;
    }
    public Cursor queryZonas(int zona) {
        //Saco sitios en funcion a las zonas
        Cursor aCursor;
        if (zona != 4 && zona != 99) {
            String[] columns = new String[]{Sitio._ID, Sitio.NOMBRE, Sitio.DIRECCION, Sitio.RUTA, Sitio.IDFOTO};
            String selection = Sitio.ZONA + "=" + zona;
            aCursor = aDatabase.query(TNAME, columns, selection, null, null, null, null);
        } else if (zona == 4) {
            aCursor = getAllDataSitiosZona();
        } else {
            //Favoritos
            String[] columns = new String[]{Sitio._ID, Sitio.NOMBRE, Sitio.DIRECCION, Sitio.RUTA, Sitio.IDFOTO};
            String selection = Sitio.FAVORITO + "=1";
            aCursor = aDatabase.query(TNAME, columns, selection, null, null, null, null);
        }
        return aCursor;
    }

    public void insert(Sitio aSitio) {
        ContentValues aContentValues = new ContentValues();
        aContentValues.put(Sitio.NOMBRE, aSitio.getNombre());
        aContentValues.put(Sitio.DIRECCION, aSitio.getDireccion());
        aContentValues.put(Sitio.DESCRIPCION, aSitio.getDescripcion());
        if (aSitio.getLatitud() != null) {
            aContentValues.put(Sitio.LATITUD, aSitio.getLatitud());
        }
        if (aSitio.getLongitud() != null) {
            aContentValues.put(Sitio.LONGITUD, aSitio.getLongitud());
        }
        aContentValues.put(Sitio.CATEGORIA, aSitio.getCategoria());
        if (aSitio.getRuta() != null) {
            aContentValues.put(Sitio.RUTA, aSitio.getRuta());
        }
        if (aSitio.getIdFoto()!=null){
            aContentValues.put(Sitio.IDFOTO, aSitio.getIdFoto());
        }
        aContentValues.put(Sitio.ZONA, aSitio.getZona());
        aDatabase.insert(TNAME, null, aContentValues);
    }

    public int favorito(int id, boolean isFavorite) {
        //Actualizo si es favorito el sitio o no
        ContentValues aContentValues = new ContentValues();
        if (isFavorite) {
            aContentValues.put(Sitio.FAVORITO, 0);
        } else {
            aContentValues.put(Sitio.FAVORITO, 1);
        }
        String where = Sitio._ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        return aDatabase.update(TNAME, aContentValues, where, whereArgs);

    }

    public Cursor esFavorito(int id) {
        //Metodo para saber si ese sitio es favorito
        String[] columns = new String[]{Sitio.FAVORITO};
        String whereArgs = Sitio._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        return aDatabase.query(TNAME, columns, whereArgs, selectionArgs, null, null, null);
    }

    public void openDB() {
        aDatabase = getWritableDatabase();
    }

    public void closeDB() {
        aDatabase.close();
    }

    public void commit() {
        aDatabase.setTransactionSuccessful();
    }

    public Cursor getAllDataSitiosZona() {
        //Obtengo todos los sitios de la base de datos
        String[] columns = new String[]{Sitio._ID, Sitio.NOMBRE, Sitio.DIRECCION, Sitio.RUTA, Sitio.IDFOTO};
        Cursor aCursor = aDatabase.query(TNAME, columns, null, null, null, null, null);
        return aCursor;
    }
    public void borrarEstablecimiento(int id){
        //Eliminar establecimiento
        String whereClause = Sitio._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        aDatabase.delete(TNAME,whereClause,whereArgs);
    }
}
