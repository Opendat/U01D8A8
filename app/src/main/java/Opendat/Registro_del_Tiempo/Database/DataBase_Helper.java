package Opendat.Registro_del_Tiempo.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Clase que representa la 'Logica' del la base de datos local.
 * Desde esta clase se controlara la contruccion y modificacion de esta base de datos.
 *
 * Modificaciones:
 * Fecha                    Autor                   Descripcion
 * -------------------------------------------------------------------------------------------------
 * 12.10.2016               Jonathan Vasquez        Creacion de Clase
 * 07.11.2016               Jonathan Vasquez        codificacion de clase (contructores y funciones Overrrided)
 *
 */
public class DataBase_Helper extends SQLiteOpenHelper {

    private static final String TAG = "AppMRAT";

    private static final String BD_NAME = "DataBaseU0293C4.sqlite";
    private static final int BD_SCHEME_VERSION = 1;

    //constructor.
    public DataBase_Helper(Context context) {
        super(context, BD_NAME, null, BD_SCHEME_VERSION);
    }

    //contrcutor por defecto.
    public DataBase_Helper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL(DataBase_Manager.CREATE_TABLE_MARCAJE);
            db.execSQL(DataBase_Manager.CREATE_TABLE_LDV);
            db.execSQL(DataBase_Manager.CREATE_TABLE_TRANSITO);
        }catch (Exception ex){
            Log.e(TAG, "Error en la contruccion de tablas en la base de datos.");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }






    /*public void ingresarMarca(SQLiteDatabase db, String U021DCD, String U021DCE, String U021DCF, String U021DD0, int U021DD1, String U021DD2, String U021DD3, String U021DD4, String U021DD6, String U021DD8, String U021DD9){
        try{
            db.execSQL(DataBase_Manager.InsertInU021DCC(U021DCD, U021DCE, U021DCF, U021DD0, U021DD1, U021DD2, U021DD3, U021DD4, U021DD6, U021DD8, U021DD9));
        }catch (Exception ex){
            Log.e(TAG, "Error al ingresar la marca: 'ingresarMarca'");
        }
    }

    public void ingresarMarca(SQLiteDatabase db, String U021DCD, String U021DCE, String U021DCF, String U021DD0, String U021DD2, String U021DD3, String U021DD4, String U021DD6, String U021DD8, String U021DD9){
        try{
            db.execSQL(DataBase_Manager.InsertInU021DCC(U021DCD, U021DCE, U021DCF, U021DD0, U021DD2, U021DD3, U021DD4, U021DD6, U021DD8, U021DD9));
        }catch (Exception ex){
            Log.e(TAG, "Error al ingresar la marca: 'ingresarMarca 2'");
        }
    }

    public void ingresarTransito(SQLiteDatabase db, String U0272F5, String U0272F6, String U0272F7, String U0272F8){
        try{
            db.execSQL(DataBase_Manager.InsertInU0272F4(U0272F5, U0272F6, U0272F7, U0272F8));
        }catch (Exception ex) {
            Log.e(TAG, "Error al ingresar el registro de transito en la BD local.");
        }
    }

    public void actualizarLDV(SQLiteDatabase db){
        try{

        }catch (Exception ex){
            Log.e(TAG, "Error al actualizar registros del LDV en l base de datos local");
        }
    }
*/

}
