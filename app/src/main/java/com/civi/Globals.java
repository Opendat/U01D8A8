package com.civi;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import Opendat.Registro_del_Tiempo.Database.DataBase_Manager;
import Opendat.Registro_del_Tiempo.Parametros;

/**
 * Clase con patron singleton utilizada para almacenar tanto variables globales nesesarias para el funcionamiento del sistema
 * como tambien las funciones de sincronizacion usadas por los threads de los llamados a ws.
 *
 * Modificaciones:
 * Fecha                    Autor                   Descripcion
 * -------------------------------------------------------------------------------------------------
 * 13.10.2016               Jonathan Vasquez        Creacion de Clase
 * 20.10.2016               Jonathan Vasquez        Implementacion de funcion de sincronizacion 'decrementThread'
 * 22.10.2016               Jonathan Vasquez        Implementacion de funcion de sincronizacion 'incrementThread'
 * 03.11.2016               Jonathan Vasquez        Integracion de variable de id RFID presentada en dispositivo para marca.
 * 08.11.2016               Jonathan Vasquez        Integracion de variable manager de base de datos local.
 */
public class Globals {
    private static Globals instance;

    private String Dato; //hora servidor.
    private int numThreads; //variable que controla la cantidad de hilos en paralelo en un periodo de tiempo.
    private  Parametros  parametrosSistema = new Parametros(); //variable global de los parametros del sistema.
    private String pathData; //variable global para almacenar la direccion de los datos del sistema en el dispositivo.
    private String id_RFID; //variable que almacena la id de la credencial presentada en el dispositivo.

    private DataBase_Manager dataBase_manager; //variable que almacena la instacia de base de datos utilizada.

    private Globals(){
    }

    public static synchronized Globals getInstance(){
        if(instance == null){
            instance = new Globals();
        }
        return instance;
    }



    public String getDato() {
        return Dato;
    }

    public void setDato(String _dato) {
        this.Dato = _dato;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public Parametros getParametrosSistema() {
        return parametrosSistema;
    }

    public void setParametrosSistema(Parametros parametrosSistema) {
        this.parametrosSistema = parametrosSistema;
    }

    public String getPathData() {
        return pathData;
    }

    public void setPathData(String pathData) {
        this.pathData = pathData;
    }

    public String getId_RFID() {
        return id_RFID;
    }

    public void setId_RFID(String idCredencial) {
        this.id_RFID = idCredencial;
    }

    public DataBase_Manager getDatabase_manager() {
        return dataBase_manager;
    }

    public void setDatabase_manager(DataBase_Manager database) {
        this.dataBase_manager = database;
    }

    /**
     * Funcion utilizada por los hilos en ejecucion.
     * El objetivo es controlar la ejecucion de multiples hilos, para asi conocer el ultimo hilo en realizar su tarea
     * y el cual ejecutara la logica que sigue despues de estas tareas.
     */
    public synchronized void decrementThread(){
        this.numThreads = this.numThreads -1;
        if(this.numThreads < 0)
            this.numThreads = 0;
    }

    /**
     * Funcion utilizada por los hilos en ejecucion.
     * El objetivo es controlar la ejecucion de multiples hilos, para asi conocer el ultimo hilo en realizar su tarea
     * y el cual ejecutara la logica que sigue despues de estas tareas.
     */
    public synchronized  void incrementThread(){
        this.numThreads = this.numThreads +1;

    }

    /**
     * Procedimiento que limpia los atributos, dejandolos al estado inicial del sistema.
     * es decir, solo tiene la informacion de los parametros del sistema.
     */
    public void LimpiezaDatosDinamicos(){
        this.dataBase_manager = null;
        this.Dato = null;
        this.id_RFID = null;
        this.numThreads = 0;
    }

}
