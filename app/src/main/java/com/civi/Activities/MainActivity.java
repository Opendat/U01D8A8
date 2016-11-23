package com.civi.Activities;

import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;


import com.civi.Globals;
import com.civi.R;
import com.civi.U02916C;
import com.civi.jni.PeripheralsJNI;
import com.civi.jni.TypeConvertion;
import com.civintec.CVAPIV01_DESFire;

import java.net.InetAddress;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import Opendat.Registro_del_Tiempo.Clases_Genericas.AntecedentesPersona;
import Opendat.Registro_del_Tiempo.Configuracion;
import Opendat.Registro_del_Tiempo.Database.DataBase_Helper;
import Opendat.Registro_del_Tiempo.Database.DataBase_Manager;
import Opendat.Registro_del_Tiempo.Parametros;

public class MainActivity extends Activity {

    private static final String TAG = "AppMRAT";
    private static final int CODIGO_FIRST_TIME_ACTIVITY = 1; //VARIABLE NESESARIA PARA NAVEGAR A FIRSTTIMEACTIVITY.
    private static final int CODIGO_EVENTS_ACTIVITY = 2;
    private static final int CODIGO_MENU_ADMIN = 3;

    private AlertDialog.Builder alertDialog = null;
    CVAPIV01_DESFire cv = new CVAPIV01_DESFire();

    U02916C theU02916C = new U02916C("http://www.opendat.cl/umbral/ayt/U021E30.asmx");
    Globals global = Globals.getInstance();
    DataBase_Manager manager;
    boolean connection; //variable usada en el hilo de conexion para guardar el estado.

    Timer timer; //usado para la deteccion constante de tarjeta RFID.
    LecturaTarjeta myTask = null; //tarea nesesaria para el autorun de la lectura RFID.(trabaja junto con el timer).

    ImageView faro;


    /**
     * Funcion que inicializa los perifericos e inicializa los datos nesesarios para el funcionamiento del sistema.
     */
    private void load(){
        faro = (ImageView) findViewById(R.id.IV_faroConection);
        //theU02916C.setAlertDialog(alertDialog);
        try {
            //se lee el archivo de configuracion y se guardan los parametros.
            global.setParametrosSistema(Configuracion.ConfiguracionParametros(global.getPathData()+"/config/"));
            new isConected().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, global.getParametrosSistema().get_Base_Datos_IP());

            //Inicializacion de BD local.
            manager = new DataBase_Manager(this);
            global.setDatabase_manager(manager);

            ActualizarLDV();
            inicioRFID();

            //global.setNumThreads(1);
            //theU021E30.ObtenerFechaHora();

            activacionAutoRun();

        }catch (Exception ex){
            Log.e(TAG, "Error en Main:: "+ex.getStackTrace());
        }
    }


    /**
     * Metodo usado para el retorno de los parametros desde la primera configuracion del sistema.
     *
     * @param requestCode Tipo de resultado al retornar dese una activiti hija.
     * @param resultCode Resultado del llamado al activity hija (RESULT_OK) - RESULT_CANCELED: en el caso de cerrar la aplicacion.
     * @param data datos de FirstTimeActivity (Parametros).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode, data);
        if(resultCode == RESULT_OK && requestCode == CODIGO_FIRST_TIME_ACTIVITY){ //Retorno desde actividad de configuracion inicial.
            Parametros p = data.getExtras().getParcelable("RESULTADO");
            global.setParametrosSistema(p);
            load();
        }else if(resultCode == RESULT_OK && requestCode == CODIGO_EVENTS_ACTIVITY){ //Retorno desde actividad de seleccion de eventos.
            //Se cancelo la seleccion. por lo que es nesesario
            Globals.getInstance().LimpiezaDatosDinamicos();//Se limpia las variables globales. (Excepto los parametros del sistema).
            inicioRFID();
            activacionAutoRun();

            new isConected().execute(global.getParametrosSistema().get_Base_Datos_IP());
        }else if(resultCode == RESULT_OK && requestCode == CODIGO_MENU_ADMIN){
            //se regresa a la aplicacion.
            cv.UID="";
            Globals.getInstance().LimpiezaDatosDinamicos();
            inicioRFID();
            activacionAutoRun();
        }else if(resultCode == RESULT_CANCELED && requestCode == CODIGO_MENU_ADMIN){
            //SE CIERRA LA APLICACION.
            finish();
            System.exit(0);
        }

    }

    @Override
    protected void onPause(){
        CVAPIV01_DESFire.AutoRead_Stop();
        if (myTask != null) {
            myTask.cancel();
        }
        super.onPause();
    }

    /**
     * Funcion usada para confirmar la existencia del archivo Config.xml, ademas de verificar la existencia de parametros en el caso de que el sistema provenga
     * desde la interfaz de configuracion de dispositivo.
     * @return boolean: true Configuracion existente; FALSE configuracion no existente
     */
    private boolean VerificarConfigucarion(String ruta){
        boolean conf = false;
        try{
            boolean b = Configuracion.ExistsConfigXML(ruta);
            return b;
        }catch(Exception ex){
            Log.e(TAG, "Error de verificacion de Configuracion:: "+ex.getMessage());
        }
        return conf;
    }


    /**
     * Funcion que activa el periferico de RFID y Finger.
     */
    private void inicioRFID(){
        //activacion de modulo RFID y Finger
        byte [] data = new byte[1];
        int fd = PeripheralsJNI.openRFDev();
        int retval = PeripheralsJNI.rfReadState(fd, data, 1);
        String ret = TypeConvertion.Bytes2HexString(data, 1);
        if (ret.equals("01 ")) {
            //System.out.println("modulos RFID activado");
            Log.i(TAG,"modulos RFID activado");
        }else{
            //System.out.println("modulos RFID no Activados, activando...");
            Log.i(TAG, "modulos RFID no Activados, activando...");
            PeripheralsJNI.rfControl(fd, 1, 1); //se cambia el estado del modulo (Activacion).
            Log.i(TAG,"modulos RFID activado");
        }

        PeripheralsJNI.closeRFDev(fd);
    }

    /**
     * Funcion que activa el sistema de autoDeteccion de tarjeta RFID.
     */
    private void activacionAutoRun() {
        try{
            activacionComunicacion();

            cv.SetOnAutoRead();
            timer = new Timer();//se inicializa el timer para la deteccion a traves del tiempo.
            CVAPIV01_DESFire.AutoRead_Run();
            myTask = new LecturaTarjeta(); //se inicializa la tarea que verifica la deteccion de tarjeta.
            timer.schedule(myTask, 1, 100); // se realiza la deteccion cada 100 ms.
            Log.i(TAG, "Inicio de Lectura..");
        }catch(Exception ex){
            Log.e(TAG,"Error en activar AutoRun:: "+ ex.getMessage());
        }


    }

    /**
     * Funcion usada para activar la comunicacion de datos desde RFID.
     *
     * OpenComm
     */
    private void activacionComunicacion(){
        int nRet = 1;
        String strComName = "com1";

        //se abre puerto de comunicacion.
        nRet = CVAPIV01_DESFire.OpenComm(strComName.getBytes(), 115200, 0);
        if(nRet == 0){
            //System.out.println("Open Port " + strComName + " OK!");
            Log.i(TAG, "Open Port " + strComName + " OK!");
        } else {
            //System.out.println("Open Port " + strComName + " FAIL!");
            Log.i(TAG, "Open Port " + strComName + " FAIL!");
        }

        //se selecciona tipo de tarjeta
        byte blockNum = 0;
        nRet = 0;
        byte CardType = 0; //0--Mifare card  1--Mifare Plus   2--Desfire EV1
        int nDeviceAddr = 0;

        nRet = AutoRead_SelectCard(nDeviceAddr, CardType, blockNum);
        if(nRet == 0){
            //System.out.println("AutoRead_SelectCard successful!");
            Log.i(TAG, "AutoRead_SelectCard successful!");
        } else {
            //System.out.println("AutoRead_SelectCard failed!");
            Log.i(TAG, "AutoRead_SelectCard failed!");
        }

    }




    private void InicioProceso(){
        try{
            Globals.getInstance().setDatabase_manager(manager);//Se guarda la variable del manager.
            startActivityForResult(new Intent(MainActivity.this, EventsActivity.class), CODIGO_EVENTS_ACTIVITY);
        }catch(Exception ex){
            Log.e(TAG, "Error en iniciar la actividad de seleccion de eventos");
        }
    }

    private void ActualizarLDV(){
        try{
            Log.i(TAG, "Actualizando la LDV local");
            U02916C theU02916C = new U02916C(Globals.getInstance().getParametrosSistema().get_Url_WebServices());
            Globals.getInstance().setNumThreads(1);
            theU02916C.Antecedentes_Verificacion_SinFoto(MainActivity.this, "", Globals.getInstance().getParametrosSistema().get_Id_Portico());
        }catch (Exception ex){
            Log.e(TAG, "Error en actualizar la LDV");
        }
    }
    /**
     * Funcion usada para Actualizar la base de datos al finalizar el llamado a busqueda de antecedentes.
     * @param AntsPersonas: Listado de antecedentes
     */
    public void ActualizarLDV_TerminoHilo(List<AntecedentesPersona> AntsPersonas){
        try{
            manager.ActualizarLDV(AntsPersonas);
        }catch (Exception ex){
            Log.e(TAG, "Error en Actualizar la LDV :: Termino Hilo");
        }
    }

    /**
     * funcion que verifica la existencia de la tabla ldv en la bse de datos local.
     */
    public void ExistLDV(){
        try{

        }catch(Exception ex){
            Log.i(TAG,"Error al verificar existencia de LDV en la base de datos local");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        global.setPathData(getApplicationInfo().dataDir);
        /*alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface alertDialog, int which) {
                alertDialog.cancel();
            }
        });*/

        if(!VerificarConfigucarion(global.getPathData()+"/config/config.xml")){
            //se navega a FirstTimeActivity
            startActivityForResult(new Intent(MainActivity.this, FirstTimeActivity.class), CODIGO_FIRST_TIME_ACTIVITY);
        }else{
            load();
        }
    }

    /**
     * Timer que controla la lectura de tarjeta. desde aqui comienza el proceso de marcaje.
     */
    private class LecturaTarjeta extends TimerTask{
        @Override
        public void run() {
            //Message message = new Message();
            //message.what = 1;
            //mHandler.sendMessage(message);
            if(!cv.UID.equals("")){
                //tprueba.setText(cv.UID.toString());
                Log.i(TAG,"deteccion de Tarjeta: " + cv.UID);
                if(cv.UID.equals(global.getParametrosSistema().get_Id_CredencialAdmin())){
                    Log.i(TAG, "Tarjeta Corresponde al administrador");
                    startActivityForResult(new Intent(MainActivity.this,MenuAdminActivity.class), CODIGO_MENU_ADMIN);
                }else{
                    Log.i(TAG, "Se busca propietario de tarjeta");
                    global.setId_RFID(cv.UID); // Se guarda la ID de la credencial.
                    cv.UID = ""; // Se resetea el registro de la credencial en la API.
                    //se verifica coneccion.
                    InicioProceso();
                }
                CVAPIV01_DESFire.AutoRead_Stop();
                this.cancel();
                CloseComm(); // Se cierra comunicacion ...
            }

        }
    }

    /**
     * Clase de Hilo para verificar coneccion, modificando la imagen de faro.
     */
    private class isConected extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String ip = params[0];
            boolean con = false;
            try{
                con = InetAddress.getByName(ip).isReachable(5000);
            }catch (Exception ex){
                Log.e(TAG, "Error en coneccion (back):: "+ex.getMessage());
            }
            return con;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            try{
                if(result){
                    faro.setImageResource(R.drawable.led_green_th);
                    Log.i(TAG, "Cambio a faro Conectado");

                }else{
                    faro.setImageResource(R.drawable.led_red_th);
                    Log.i(TAG, "Cambio a faro Desconectado");
                }
                connection = result;

            }catch(Exception ex){
                Log.e(TAG, "Error en coneccion (post):: "+ ex.getMessage());
            }
        }
    }

    /**
     * Funcion originaria de demo SerialTest
     * @param DeviceAddr
     * @param CardType
     * @param blockNum
     * @return resultado del llamado de API WiegandMode. (Mas informacion ir a CNReader API Reference V3.61.pdf)
     */
    private int AutoRead_SelectCard(int DeviceAddr, byte CardType, byte blockNum) {
        byte data[] = new byte[20];
        int nRet = -1;

        data[0] = 0x00;
        data[1] = blockNum;
        data[2] = 0x26;
        data[3] = 0x12;
        data[4] = 0x01;
        data[5] = 0x0e;		//DATA[5]: output select
        data[6] = 0x00;
        data[7] = CardType;
        data[8] = 0x00;
        data[9] = 0x00;
        data[10] = 0x00;

        nRet = CVAPIV01_DESFire.WiegandMode(DeviceAddr, data);

        return nRet;
    }

    private void CloseComm(){
        int nRet = CVAPIV01_DESFire.CloseComm();
        if(nRet == 0){
            Log.i(TAG, "Cierre puerto comunicacion RFID OK!");
        } else {
            Log.i(TAG, "Cierre puerto comunicacion RFID Fail!");
        }
    }
}
