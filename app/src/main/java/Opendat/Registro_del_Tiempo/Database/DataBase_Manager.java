package Opendat.Registro_del_Tiempo.Database;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.civi.Globals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import Opendat.Registro_del_Tiempo.Clases_Genericas.AntecedentesPersona;
import Opendat.Registro_del_Tiempo.Clases_Genericas.RegistroMarca;
import Opendat.Registro_del_Tiempo.Clases_Genericas.RegistroTransito;

/**
 * Clase que representa la 'Persistencia' del la base de datos local.
 * Desde esta clase se controlara la contruccion y modificacion de esta base de datos.
 *
 * Modificaciones:
 * Fecha                    Autor                   Descripcion
 * -------------------------------------------------------------------------------------------------
 * 12.10.2016               Jonathan Vasquez        Creacion de Clase
 * 07.11.2016               Jonathan Vasquez        Generacion de variables estaticas de nombre de tablas y CRUD (Create-Read-Update-Delete).
 * 09.11.2016               Jonathan Vasquez        Generacion de Funcion de verificacion de reingreso
 * 10.11.2016               Jonathan Vasquez        Implemetacion de Ingreso de marca online y offline
 * 22.11.2016               Jonathan Vasquez        Implementacion de llamados de eliminaciones y registros de marcas de asistencia.
 */
public class DataBase_Manager {

    private DataBase_Helper helper;
    private SQLiteDatabase db;

    private static final String NAME_TABLE_MARCAJE = "U021DCC";
    private static final String NAME_TABLE_LDV = "U0217F0";
    private static final String NAME_TABLE_TRANSITO = "U0272F4";

    public static final String CREATE_TABLE_LDV = "CREATE TABLE " + NAME_TABLE_LDV + " ("
            +" U0217F1 varchar2(20),"
            +" U0217F2 varchar2(20),"
            +" U0217F3 varchar2(20),"
            +" U0217F4 number,"
            +" U0217F5 varchar2(20),"
            +" U0217F6 varchar2(20),"
            +" U0217F7 BLOB,"
            +" U0217F8 varchar2(700),"
            +" U0217F9 varchar2(90));";
    public static final String CREATE_TABLE_MARCAJE = "CREATE TABLE " + NAME_TABLE_MARCAJE + " ("
            +" U021DCD varchar2(20) NOT NULL,"
            +" U021DCE datetime NOT NULL,"
            +" U021DCF varchar2(20) NOT NULL,"
            +" U021DD0 varchar2(20) NOT NULL,"
            +" U021DD1 number,"
            +" U021DD2 varchar2(20),"
            +" U021DD3 varchar2(20),"
            +" U021DD4 varchar2(20),"
            +" U021DD6 varchar2(20) NOT NULL,"
            +" U021DD8 varchar2(120),"
            +" U021DD9 varchar2(20),"
            +" CONSTRAINT U021DCC_pk PRIMARY KEY (U021DCD, U021DCE, U021DCF, U021DD0));";
    public static final String CREATE_TABLE_TRANSITO = "CREATE TABLE " + NAME_TABLE_TRANSITO + " ("
            +" U0272F5 datetime NOT NULL,"
            +" U0272F6 varchar(20) NOT NULL,"
            +" U0272F7 varchar(20) NOT NULL,"
            +" U0272F8 varchar(20),"
            +" CONSTRAINT PK_U0272F4 PRIMARY KEY (U0272F5, U0272F6));";

    public long IngresarMarca(String U021DCD, String U021DCE, String U021DCF, String U021DD0, int U021DD1, String U021DD2, String U021DD3, String U021DD4, String U021DD6, String U021DD8, String U021DD9) {
        this.OpenWriteableDB();

        ContentValues params = new ContentValues();
        params.put("U021DCD",U021DCD);
        params.put("U021DCE",U021DCE);
        params.put("U021DCF",U021DCF);
        params.put("U021DD0",U021DD0);
        params.put("U021DD1",U021DD1);
        params.put("U021DD2",U021DD2);
        params.put("U021DD3",U021DD3);
        params.put("U021DD4",U021DD4);
        params.put("U021DD6",U021DD6);
        params.put("U021DD8",U021DD8);
        params.put("U021DD9",U021DD9);

        long rowid = db.insert(NAME_TABLE_MARCAJE, null, params);
        this.closeDB();

        return rowid;

    }

    public long IngresarMarca(String U021DCD, String U021DCE, String U021DCF, String U021DD0, String U021DD2, String U021DD3, String U021DD4, String U021DD6, String U021DD8, String U021DD9){
        this.OpenWriteableDB();

        ContentValues params = new ContentValues();
        params.put("U021DCD",U021DCD);
        params.put("U021DCE",U021DCE);
        params.put("U021DCF",U021DCF);
        params.put("U021DD0",U021DD0);
        params.put("U021DD1",-1);//debiera ser vacio.
        params.put("U021DD2",U021DD2);
        params.put("U021DD3",U021DD3);
        params.put("U021DD4",U021DD4);
        params.put("U021DD6",U021DD6);
        params.put("U021DD8",U021DD8);
        params.put("U021DD9",U021DD9);

        long rowid = db.insert(NAME_TABLE_MARCAJE, null, params);

        this.closeDB();

        return rowid;
    }

    public long IngresarTransito(String U0272F5, String U0272F6, String U0272F7, String U0272F8){
        this.OpenWriteableDB();

        ContentValues params = new ContentValues();
        params.put("U0272F5",U0272F5);
        params.put("U0272F6",U0272F6);
        params.put("U0272F7",U0272F7);
        params.put("U0272F8",U0272F8);

        long rowid = db.insert(NAME_TABLE_TRANSITO, null, params);
        this.closeDB();

        return rowid;
    }

    /**
     * Procedimiento que limpia la LDV y reingresa los datos de la lista.
     * @param listadoLDV
     * @return TRUE: Actualizacion exitosa, FALSE: Error en actualizacion.
     */
    public boolean ActualizarLDV(List<AntecedentesPersona> listadoLDV){
        this.OpenWriteableDB();
        long result;

        result = db.delete(NAME_TABLE_LDV, null, null); //LIMPIA TABLA. (NO LA ELIMINA).
        if(result < 0){
            return false;
        }
        for(AntecedentesPersona ap : listadoLDV){
            ContentValues params = new ContentValues();
            params.put("U0217F1", ap.get_U0217F1());
            params.put("U0217F2", ap.get_U0217F2());
            params.put("U0217F3", ap.get_U0217F3());
            params.put("U0217F4", ap.get_U0217F4());
            params.put("U0217F5", ap.get_U0217F5());
            params.put("U0217F6", ap.get_U0217F6());
            params.put("U0217F7", ap.get_U0217F7());
            params.put("U0217F8", ap.get_U0217F8());
            params.put("U0217F9", ap.get_U0217F9());

             result = db.insert(NAME_TABLE_LDV, null, params);
            if(result < 0){
                return false;
            }
        }

        this.closeDB();

        return true;
    }



    /**
     * Funcion utilizada para la busqueda de antecedentes de una persona.
     * Funcion utilizada para la busqueda en la BD local.
     * @param idTarjeta: UID de la tarjeta RFID presentada en el dispostivo.
     * @return Antecedentes (Sin Fotografia).
     */
    public AntecedentesPersona BuscarAntecedente(String idTarjeta){
        AntecedentesPersona pers = new AntecedentesPersona();
        this.OpenReadableDB();
        String strWhere = "U0217F1 = ?";
        String [] whereArgs = {idTarjeta};

        Cursor c = db.query(NAME_TABLE_LDV, null, strWhere, whereArgs, null, null, null);

        if(c != null && c.getCount() > 0){
            c.moveToFirst();
            pers.set_U0217F1(c.getString(0));
            pers.set_U0217F2(c.getString(1));
            pers.set_U0217F3(c.getString(2));
            pers.set_U0217F4(c.getInt(3));
            pers.set_U0217F5(c.getString(4));
            pers.set_U0217F6(c.getString(5));
            pers.set_U0217F7(c.getBlob(6));
            pers.set_U0217F8(c.getString(7));
            pers.set_U0217F9(c.getString(8));

            c.close();
        }else{
            pers = null;
        }

        this.closeDB();
        return pers;
    }

    /**
     * Funcion usado para Verificar si existe una marca de la persona realizada anteriormente.
     * @param pstrId_Credencial
     * @param pstrEvento
     * @param pstrId_Portico
     * @return
     */
    public boolean Verificar_Reingreso(String pstrId_Credencial, String pstrEvento, String pstrId_Portico){
        this.OpenReadableDB();

        String query = "SELECT U021DCD, strftime(U021DCE) AS U021DCE, U021DCF FROM U021DCC WHERE U021DCE = (SELECT MAX(U021DCE) FROM U021DCC WHERE U021DD0 = '" + pstrId_Portico + "')";
        Cursor c = db.rawQuery(query, null);

        try{
            if(c != null){
                if(c.getCount() == 0) {
                    this.closeDB();
                    return false;
                }
                c.moveToFirst();
                if(pstrId_Credencial.equals(c.getString(0)) && pstrEvento.equals(c.getString(2))){
                    this.closeDB();
                    return true;
                }else {
                    this.closeDB();
                    return false;
                }
            }else{
                this.closeDB();
                return false;
            }
        }catch (Exception ex){
            this.closeDB();
            return false;
        }
    }

    /**
     * Funcion utilizada para obtener los registros de las marcas de asistencia dentro de la base de datos local.
     * @return listado de marcas o null.
     */
    public List<RegistroMarca> Registros_Marcas_De_Asistencia(){
        List<RegistroMarca> registros = null;
        try{
            this.OpenReadableDB();

            Cursor c = db.query(NAME_TABLE_MARCAJE, null, null, null, null, null, null);

            registros = new ArrayList<>();
            while(c.moveToNext()){
                String id_cred = c.getString(0);
                String fecha_hora = c.getString(1);
                String tipo_evento = c.getString(2);
                String id_portico = c.getString(3);
                int id_veri = c.getInt(4);
                String id_persona = c.getString(5);
                String tipo_cred = c.getString(6);
                String nivel_au = c.getString(7);
                String estado_trans = c.getString(8);
                String geo_referencia = c.getString(9);
                String origen_marca = c.getString(10);

                RegistroMarca rm = new RegistroMarca(id_cred, fecha_hora, tipo_evento, id_portico, id_veri, id_persona, tipo_cred, nivel_au, estado_trans, geo_referencia, origen_marca);
                registros.add(rm);
            }

        }catch (Exception ex){
            registros = null;
        }
        this.closeDB();
        return registros;
    }

    /**
     * Funcion que solicita todos los registros de transito en la base de datos local.
     * @return listado de transito.
     */
    public List<RegistroTransito> Registros_Transito(){
        List<RegistroTransito> registros = null;
        try{
            this.OpenReadableDB();

            Cursor c = db.query(NAME_TABLE_TRANSITO, null, null, null, null, null, null);

            registros = new ArrayList<>();
            while(c.moveToNext()){
                String fechaHora = c.getString(0);
                String idPortico = c.getString(1);
                String idCrede = c.getString(2);
                String idPersona = c.getString(3);
                RegistroTransito reg = new RegistroTransito(fechaHora, idPortico, idCrede, idPersona);

                registros.add(reg);
            }

        }catch (Exception ex){
           registros = null;

        }
        this.closeDB();
        return registros;
    }

    /**
     * Funcion que elimina un registro especifico de la tabla de historial de transito U0272F4.
     * @param fechaHora
     * @param idPortico
     * @return
     */
    public int EliminarTransito(String fechaHora, String idPortico){
        int result = -1;
        try{
            this.OpenWriteableDB();
            String selection = "U0272F5 BETWEEN ? AND ? AND U0272F6 = ?";
            String [] selectionArgs = {fechaHora,fechaHora,idPortico};

            result = db.delete(NAME_TABLE_TRANSITO, selection, selectionArgs);
        }catch (Exception ex){

        }
        this.closeDB();
        return result;
    }

    public int EliminarMarca(String idCredencial, String fechaHora, String tipoEvento, String idPortico){
        int result = -1;
        try{
            this.OpenWriteableDB();
            String selection = "U021DCE BETWEEN ? AND ? AND U021DCD = ? AND U021DCF = ? AND U021DD0 = ?";
            String [] selectionArgs = {fechaHora, fechaHora, idCredencial, tipoEvento, idPortico};

            result = db.delete(NAME_TABLE_MARCAJE, selection, selectionArgs);
        }catch (Exception ex){

        }
        this.closeDB();
        return result;
    }

    //constructor.
    public DataBase_Manager(Context context) {
        helper = new DataBase_Helper(context);
    }

    private void OpenReadableDB(){
        db = helper.getReadableDatabase();
    }
    private void OpenWriteableDB(){

        //Obtencion de BD, si la base de datos no existe getWritableDatabase crea la base de datos y la devuelve en modo
        //escritura. Si ya existe, solamente la devuelve.
        db = helper.getWritableDatabase();
    }
    private void closeDB(){
        if(db != null){
            db.close();
        }
    }
}
