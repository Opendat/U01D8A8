package com.civi.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.civi.Globals;
import com.civi.R;
import com.civi.U02916C;
import com.civi.jni.PeripheralsJNI;
import com.civi.jni.TypeConvertion;
import com.civintec.CVAPIV01_DESFire;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import Opendat.Registro_del_Tiempo.Clases_Genericas.AntecedentesPersona;
import Opendat.Registro_del_Tiempo.Parametros;

public class EventsActivity extends AppCompatActivity {
    private static final String TAG = "AppMRAT";
    boolean estadoConexion = true; //Variable para controlar el estado de la conexion, usado al verificar la conexion.
    U02916C theU02916C;
    List<AntecedentesPersona> persona; //es una lista porque puede haber mas de una regla asignada a la credencial.

    String EventoSeleccionado = null; //variable que almacena el eventoSeleccionado por la persona.

    ImageView faroConnect;
    ProgressBar barraProceso;
    TextView nombrePersona;
    Button inicioJornada, inicioPausa, terminoPausa, terminoJornada, transito, cancel;
    AlertDialog.Builder alertDialog; // variable nesesaria para el despliegue de mensajes.
    AlertDialog.Builder confirmDialog; // variable nesesaria para desplegar mensaje de confirmacion de reingreso.


    ProgressDialog progressDialogCargando;
    private int progressStatus = 0; //variable  que controla el porcetaje de avance del ingreso de marca.

    private void ShowConfirmDialog(){

    }



    /**
     * Metodo utilizado para realizar el llamado a la obtenidos de antecedentes de la persona dependiendo del estado
     * de la conexion del dispositivo.
     * TRUE: El llamado es hacia el webservice.
     * FALSE: El llamado es hacia la base de datos local.
     */
    private void ObtenerAntecedentes(){


        if(this.estadoConexion){
            //estado Online. Se llama al ws.
            //no nesesito saber cual termina primero, por lo que asignno 0 para que los 2 entren a sus condicionantes
            // de termino de hilo.
            Globals.getInstance().setNumThreads(0);
            theU02916C.setMiBarra(barraProceso);
            theU02916C.Antecedentes_Verificacion(EventsActivity.this
                    , Globals.getInstance().getId_RFID()
                    , Globals.getInstance().getParametrosSistema().get_Id_Portico());
            theU02916C.Antecedente_Fotografia(EventsActivity.this
                    , Globals.getInstance().getId_RFID()
                    , Globals.getInstance().getParametrosSistema().get_Id_Portico());


        }else{

            //Estado Offline. Se llama a la BD local.
            AntecedentesPersona pers = Globals.getInstance().getDatabase_manager().BuscarAntecedente(Globals.getInstance().getId_RFID());

            if(pers == null){
                //No fue encontrado los antecedentes de la persona.
                Log.e(TAG, "Id de tarjeta no se encuentra en la LDV. ");
                DespliegueDatos(null);
            }else{
                //se ha encontrado persona.
                List<AntecedentesPersona> l = new ArrayList<>();
                l.add(pers);
                DespliegueDatos(l); //se despliegua los datos de la persona.
            }
            barraProceso.setVisibility(View.INVISIBLE);
            EstadoBotones(true);
        }
    }

    /**
     * Metodo que asigna la fotografia a la persona. Esta funcion es llamada al terminar el hilo Ants_Veri_foto
     * @param fotografia
     */
    public void AsignacionFotografia(byte[] fotografia){
        ImageView panelImagen = (ImageView)findViewById(R.id.IV_Foto);
        EstadoBotones(true);
        try{
            if(fotografia != null){
                Bitmap bm = BitmapFactory.decodeByteArray(fotografia, 0, fotografia.length);
                panelImagen.setImageBitmap(bm);
            }else{
                //se carga la imagen default.
                panelImagen.setImageResource(R.drawable.persona_default);
            }
        }catch(Exception ex){
            Log.e(TAG, "Imposible generar la imagen de la persona");
        }
    }

    /**
     * Funcion utilizada para desplegar los datos de la persona en la interfaz. La funcion es llamada al terminar el llamado de Ants_Verificacion.
     * @param antecedentes: La lista de antecedentes de la persona
     */
    public void DespliegueDatos(List<AntecedentesPersona> antecedentes){
        EstadoBotones(true);
        try{
            if(antecedentes != null && antecedentes.size() != 0 ){
                nombrePersona.setText("Bienvenido: "+antecedentes.get(0).get_U0217F9());
                persona = antecedentes;
            }else{
                nombrePersona.setText("Bienvenido/a");
                persona = null;
            }
        }catch(Exception ex){
            Log.e(TAG, "Error al desplegar nombre de la persona en la seleccion de eventos");
        }
    }

    /**
     * Metodo que configura la botonera dependiendo de la disposicion del portico.
     */
    private void HabilitarBotones(){
        String typeDispo = Globals.getInstance().getParametrosSistema().get_Disposicion();

        switch (typeDispo){
            case "Z0B9948": //Solo inicio de jornada.
                inicioJornada.setVisibility(View.VISIBLE);
                inicioPausa.setVisibility(View.INVISIBLE);
                terminoPausa.setVisibility(View.INVISIBLE);
                terminoJornada.setVisibility(View.INVISIBLE);
                break;
            case "Z0B9949": //Solo termino de jornada
                inicioJornada.setVisibility(View.INVISIBLE);
                inicioPausa.setVisibility(View.INVISIBLE);
                terminoPausa.setVisibility(View.INVISIBLE);
                terminoJornada.setVisibility(View.VISIBLE);
                break;
            case "Z0B994A": //Inicio y termino de jornada
                inicioJornada.setVisibility(View.VISIBLE);
                inicioPausa.setVisibility(View.INVISIBLE);
                terminoPausa.setVisibility(View.INVISIBLE);
                terminoJornada.setVisibility(View.VISIBLE);
                break;
            case "Z0B994B": //Sólo inicio colación
                inicioJornada.setVisibility(View.INVISIBLE);
                inicioPausa.setVisibility(View.VISIBLE);
                terminoPausa.setVisibility(View.INVISIBLE);
                terminoJornada.setVisibility(View.INVISIBLE);
                break;
            case "Z0B994C": //Sólo término colación
                inicioJornada.setVisibility(View.INVISIBLE);
                inicioPausa.setVisibility(View.INVISIBLE);
                terminoPausa.setVisibility(View.VISIBLE);
                terminoJornada.setVisibility(View.INVISIBLE);
                break;
            case "Z0B994D": //Inicio y Término colación
                inicioJornada.setVisibility(View.INVISIBLE);
                inicioPausa.setVisibility(View.VISIBLE);
                terminoPausa.setVisibility(View.VISIBLE);
                terminoJornada.setVisibility(View.INVISIBLE);
                break;
            case "Z0B994E": //IJ, IC, TC, TJ
                inicioJornada.setVisibility(View.VISIBLE);
                inicioPausa.setVisibility(View.VISIBLE);
                terminoPausa.setVisibility(View.VISIBLE);
                terminoJornada.setVisibility(View.VISIBLE);
                break;

        }
    }

    private void EstadoBotones(boolean estado){
        Log.i(TAG, "Cambio de estado de botones a "+estado);
        inicioJornada.setEnabled(estado);
        inicioPausa.setEnabled(estado);
        terminoPausa.setEnabled(estado);
        terminoJornada.setEnabled(estado);
        transito.setEnabled(estado);
        cancel.setEnabled(estado);
    }

    /**
     * Metodo usado para habilitar o desabilitar el boton de transito dependiendo del parametro de manipulacion de chapa magnetica.
     */
    private void HabilitarTransito(){
        if(Globals.getInstance().getParametrosSistema().get_Manipulacion_Puerta().equals("Z0B9C4C")
                || Globals.getInstance().getParametrosSistema().get_Manipulacion_Puerta() == null){
            transito.setVisibility(View.INVISIBLE);
        }else{
            transito.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Funcion usado para obtener el codigo del evento seleccionado.
     * @param pressKey: representa una id que identifica el tipo de boton presionado:
     *                112: "Z0B99D3";//Inicio Jornada (F1)
     *                113: "Z0B99D5";//Inicio Colación (F2)
     *                114: "Z0B99D6";//Termino Colación (F3)
     *                115: "Z0B99D4";//Termino Jornada(F4)
     * @return Codigo del evento seleccionado
     */
    private String GetEventoSeleccionado(int pressKey){
        try{
            String evento = null;
            String disposicion = Globals.getInstance().getParametrosSistema().get_Disposicion();

            switch (disposicion){
                //Sólo inicio jornada
                case "Z0B9948":
                    switch (pressKey){
                        //Inicio Jornada (F1)
                        case 112:
                            evento = "Z0B99D3";
                            break;
                    }
                    break;

                //Sólo término jornada
                case "Z0B9949":
                    switch (pressKey){
                        //Termino Jornada(F4)
                        case 115:
                            evento = "Z0B99D4";
                            break;
                    }
                    break;

                //Inicio y Término jornada
                case "Z0B994A":
                    switch (pressKey){
                        //Inicio Jornada (F1)
                        case 112:
                            evento = "Z0B99D3";
                            break;

                        //Termino Jornada(F4)
                        case 115:
                            evento = "Z0B99D4";
                            break;
                    }
                    break;

                //Sólo inicio colación
                case "Z0B994B":
                    switch (pressKey){
                        //Inicio Colación (F2)
                        case 113:
                            evento = "Z0B99D5";
                            break;
                    }
                    break;

                //Sólo término colación
                case "Z0B994C":
                    switch (pressKey){
                        //Termino Colación (F3)
                        case 114:
                            evento = "Z0B99D6";
                            break;
                    }
                    break;

                //Inicio y Término colación
                case "Z0B994D":
                    switch (pressKey){
                        //Inicio Colación (F2)
                        case 113:
                            evento = "Z0B99D5";
                            break;

                        //Termino Colación (F3)
                        case 114:
                            evento = "Z0B99D6";
                            break;
                    }
                    break;

                //IJ, IC, TC, TJ
                case "Z0B994E":
                    switch (pressKey){
                        //Inicio Jornada (F1)
                        case 112:
                            evento = "Z0B99D3";
                            break;

                        //Inicio Colación (F2)
                        case 113:
                            evento = "Z0B99D5";
                            break;

                        //Termino Colación (F3)
                        case 114:
                            evento = "Z0B99D6";
                            break;

                        //Termino Jornada(F4)
                        case 115:
                            evento = "Z0B99D4";
                            break;
                    }
                    break;

            }
            return evento;
        }catch (Exception ex){
            Log.e(TAG, "Error en la obtencion de eventos");
        }
        return null;
    }

    /*****************************Funciones de Botonera *******************************************/
    /**********************************************************************************************/
    private OnClickListener CancelarSeleccion = new OnClickListener() {
        @Override
        public void onClick(View v) {
            try{
                setResult(RESULT_OK);
                Globals.getInstance().setId_RFID(""); //limpio el dato de la tarjeta que se presentó para marca.
                finish();
            }catch (Exception ex){
                Log.e(TAG, "Error en Listener de Cancelar Seleccion de Eventos.");
            }
        }
    };
    private OnClickListener IniJor_Click = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String evento = GetEventoSeleccionado(112);
            EventoSeleccionado = evento;
            VerificarAutentificacion();
        }
    };
    private OnClickListener IniPau_Click = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String evento = GetEventoSeleccionado(113);
            EventoSeleccionado = evento;
            VerificarAutentificacion();
        }
    };
    private OnClickListener TermPau_Click = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String evento = GetEventoSeleccionado(114);
            EventoSeleccionado = evento;
            VerificarAutentificacion();
        }
    };
    private OnClickListener TermJor_Click = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String evento = GetEventoSeleccionado(115);
            EventoSeleccionado = evento;
            VerificarAutentificacion();
        }
    };

    private OnClickListener Transit_Click = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ProcesoMarcaTransito();
        }
    };
    /**********************************************************************************************/
    /**********************************************************************************************/

    private void VerificarAutentificacion(){
        try{
            if(persona != null){//Existe los Antecedentes de la persona
                String nivelAu = persona.get(0).get_U0217F5();
                switch(nivelAu){
                    //Sólo credencial.
                    case "Z0B997F":
                        VerificarReingreso();
                        break;

                    //Credencial + Pin
                    case "Z0B9980":
                        break;

                    //Credencial + Huella dactilar
                    case "Z0B9981":
                        break;
                }
            }else{//No existe los antecedentes de la persona.
                VerificarReingreso();
            }
        }catch (Exception ex){
            Log.e(TAG, "Error en proceso de autentificacion");
        }
    }

    /**
     * Procedimiento usado para verificar reingreso de marca
     */
    private void VerificarReingreso(){
        try{
            if(estadoConexion){
                //Estado conectado.
                theU02916C.Verificar_Reingreso(EventsActivity.this,
                        Globals.getInstance().getId_RFID(),
                        EventoSeleccionado,
                        Globals.getInstance().getParametrosSistema().get_Id_Portico());
            }else{
                //Estado desconectado.
                boolean resultVerificacion = Globals.getInstance().getDatabase_manager().Verificar_Reingreso(Globals.getInstance().getId_RFID(),
                        EventoSeleccionado,
                        Globals.getInstance().getParametrosSistema().get_Id_Portico());

                VerificacionReingreso_TerminoHilo(resultVerificacion);

            }

        }catch (Exception ex){
            Log.e(TAG, "Error en la verificacion de Reingreso");
        }
    }

    /**
     * Procedimiento usado al terminar llamado a WS para verificacion de reingreso.
     * @param resultVerificacion
     */
    public void VerificacionReingreso_TerminoHilo(boolean resultVerificacion){
        try{
            if(resultVerificacion != true){//la persona no ha marcado antes. Se procede al proceso de marca.
                IngresarMarca();
            }else{
                //la persona ya ha marcado. Se despliegua ventana de confirmacion.
                NotificacionConfirm();
            }
        }catch (Exception ex){
            Log.e(TAG,"Error en proceso de verificacion de reingreso (Al terminar hilo)");
        }
    }

    /**
     * Procedimiento usado para iniciar el proceso de marca de transito
     */
    public void ProcesoMarcaTransito(){
        try{
            String id_persona = null;
            if(persona != null){
                id_persona = persona.get(0).get_U0217F3();
            }

            if(estadoConexion){
                theU02916C.InsertTransito(this, Globals.getInstance().getParametrosSistema().get_Id_Portico(),
                        Globals.getInstance().getId_RFID(),
                        id_persona);
            }else{
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
                Date datenow = new Date();
                new IngresarTransitoOffline().execute(dateFormat.format(datenow), //hora
                        dateTimeFormat.format(datenow), //fecha y hora
                        Globals.getInstance().getParametrosSistema().get_Id_Portico(), //id portico
                        Globals.getInstance().getId_RFID(), //id credencial presentada
                        id_persona); // id de persona
            }
        }catch (Exception ex){
            Log.e(TAG, "Error el iniciar proceso de marca de transito::" + ex.getStackTrace());
            Notificacion("No fue posible registrar la marca de transito");
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        confirmDialog = new AlertDialog.Builder(EventsActivity.this);

        faroConnect = (ImageView)findViewById(R.id.IV_FaroEvent);
        barraProceso = (ProgressBar) findViewById(R.id.PB_ProcesoEvent);
        nombrePersona = (TextView)findViewById(R.id.TV_NombrePersonaEvent);
        inicioJornada = (Button)findViewById(R.id.BT_InicioJornada);
        inicioPausa = (Button)findViewById(R.id.BT_InicioPausa);
        terminoPausa = (Button)findViewById(R.id.BT_TerminoPausa);
        terminoJornada = (Button)findViewById(R.id.BT_TerminoJornada);
        transito = (Button)findViewById(R.id.BT_Transito);
        cancel = (Button)findViewById(R.id.BT_Cancelar);

        EstadoBotones(false);

        theU02916C = new U02916C(Globals.getInstance().getParametrosSistema().get_Url_WebServices());
        //theU02916C.setAlertDialog(alertDialog);

        cancel.setOnClickListener(CancelarSeleccion);
        inicioJornada.setOnClickListener(IniJor_Click);
        inicioPausa.setOnClickListener(IniPau_Click);
        terminoPausa.setOnClickListener(TermPau_Click);
        terminoJornada.setOnClickListener(TermJor_Click);
        transito.setOnClickListener(Transit_Click);

        //Se configura la botonera.
        HabilitarBotones();
        HabilitarTransito();

        //verifico el estado de conexion del dispositivo.
        new isConnected().execute(Globals.getInstance().getParametrosSistema().get_Base_Datos_IP());
    }


    private void Notificacion(String message){
        try{
            alertDialog = new AlertDialog.Builder(EventsActivity.this);
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface alertDialog, int which) {
                    //alertDialog.cancel();
                    setResult(RESULT_OK);
                    finish();
                }
            });
            alertDialog.setTitle(message);
            alertDialog.show();
            if(barraProceso != null){
                barraProceso.setVisibility(View.INVISIBLE);
            }
        }catch (Exception ex){
            Log.e(TAG, "Error al enviar mensaje (EventsActivity):: "+ex.getMessage());
        }

    }

    private void NotificacionConfirm(){
        try{
            confirmDialog = new AlertDialog.Builder(EventsActivity.this);
            confirmDialog.setPositiveButton("SI", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //se procede a realizar la marca.
                    IngresarMarca();
                }
            });
            confirmDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //se cancela la seleccion de evento.
                    setResult(RESULT_OK);
                    finish();
                }
            });
            confirmDialog.setTitle("Confirmacion de reingreso");
            confirmDialog.setMessage("Ya ha realizado registro,¿Desea volver a registrarse?");
            confirmDialog.show();
        }catch (Exception ex){
            Log.e(TAG, "Error al desplegar Notificación de confirmación (EventsActivity):: "+ex.getMessage());
        }

    }

    private void ShowProgressDialog(String titulo){
        progressDialogCargando = new ProgressDialog(this);
        //progressDialogCargando.setTitle("Registrando Marca");
        progressDialogCargando.setTitle(titulo);
        progressDialogCargando.setMessage("Preparando registro");
        progressDialogCargando.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialogCargando.setCancelable(false);
        progressDialogCargando.setMax(100);
        progressDialogCargando.show();
    }

    /**************************************Threads*************************************************/
    /**
     * Conjunto de hilos utilizado para la verificacion de conexion del servidor de webservice y base de datos.
     */
    private class isConnected extends AsyncTask<String, Void, Boolean> {

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
                    //verifico la existencia de webservice.
                    new isWSConnected().execute();

                }else{
                    faroConnect.setImageResource(R.drawable.led_red_th);
                    estadoConexion = false;
                    Log.i(TAG, "Cambio a faro de evento Desconectado");
                    ObtenerAntecedentes();

                }
            }catch(Exception ex){
                Log.e(TAG, "Error en coneccion (postExe -> isConnected):: "+ ex.getMessage());
            }
        }
    }
    private class isWSConnected extends AsyncTask<String, Void, Boolean> {
        String NAMESPACE = "http://tempuri.org/";
        String nombreFuncion = "U02916D";
        String URL = Globals.getInstance().getParametrosSistema().get_Url_WebServices();

        @Override
        protected Boolean doInBackground(String... params) {
            String result = null;
            boolean envio = false;
            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
            headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).

            SoapObject request = new SoapObject(NAMESPACE, nombreFuncion);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);

            try {
                transporte.call(NAMESPACE + nombreFuncion, envelope, headers);
                SoapPrimitive resSoap = (SoapPrimitive) envelope.getResponse();
                result = resSoap.toString();
                if(!result.equals(null)){
                    envio = true;
                }else {
                    envio = false;
                }

            } catch (IOException e) {//no encuentra respuesta (No existe o no Existe la direccion a webservice).
                //e.printStackTrace();
                envio = false;
                cancel(true);
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                envio = false;
                cancel(true);
            }
            return envio;

        }

        @Override
        protected void onCancelled(Boolean result) {
            Log.i(TAG, "Cancel -> isWSConnected");
            faroConnect.setImageResource(R.drawable.led_red_th);
            estadoConexion = false; //se cambia a estado desconectado.
            ObtenerAntecedentes(); // se envia a funcion de estadoConexion.
        }

        @Override
        protected void onPostExecute(Boolean result){
            try{
                if(result){
                    faroConnect.setImageResource(R.drawable.led_green_th);
                    Log.i(TAG, "Cambio a faro de evento Conectado");
                    estadoConexion = true;
                }else{
                    faroConnect.setImageResource(R.drawable.led_red_th);
                    Log.i(TAG, "Cambio a faro de evento Desconectado");
                    estadoConexion = false;
                }
                ObtenerAntecedentes();
            }catch(Exception ex){
                Log.e(TAG, "Error en coneccion (postExe -> isWSConnected):: "+ ex.getMessage());
            }
        }
    }
    private class IngresarMarcaOffLine extends AsyncTask<String, String, Long>{
        long r = -1;
        String dateMark;

        @Override
        protected void onPreExecute(){
            ShowProgressDialog("Registrando Marca...");
            progressDialogCargando.setProgress(10);
        }

        @Override
        protected Long doInBackground(String... params) {
            dateMark = params[1];

            try{
                //Credencial no Existe en LDV.
                if(persona == null){
                    publishProgress("5","Registro realizado a las " + dateMark + ".\nPersona no identificada, registro no verificado...transacción aceptada.");
                    r = Globals.getInstance().getDatabase_manager().IngresarMarca(params[0], //Globals.getInstance().getId_RFID()
                            params[2], //now
                            params[3], //EventoSeleccionado
                            params[4], //Globals.getInstance().getParametrosSistema().get_Id_Portico()
                            params[5], //id_persona
                            params[6], //tipo_credencial
                            params[7], //nivel_autenticacion
                            params[8], //"Z0B99CB"
                            params[9], //Globals.getInstance().getParametrosSistema().get_Localizacion_Geografica()
                            params[10]); //"Z0B9E19"
                    publishProgress("70","Registro realizado a las " + dateMark + ".\nPersona no identificada, registro no verificado...transacción aceptada.");

                }else{
                    //Credencial única en el pórtico.
                    if(persona.size() == 1){
                        publishProgress("5", "Registro realizado a las " + dateMark + ".\nRegistro verificado...transacción aceptada.");
                        r = Globals.getInstance().getDatabase_manager().IngresarMarca(params[0], //Globals.getInstance().getId_RFID()
                                params[2], //now
                                params[3], //EventoSeleccionado
                                params[4], //Globals.getInstance().getParametrosSistema().get_Id_Portico()
                                Integer.parseInt(params[5]), //id_verificacion
                                params[6], //id_persona
                                params[7], //tipo_credencial
                                params[8], //nivel_autenticacion
                                params[9], //"Z0B99CA"
                                params[10], //Globals.getInstance().getParametrosSistema().get_Localizacion_Geografica()
                                params[11]); //"Z0B9E19"
                        publishProgress("70", "Registro realizado a las " + dateMark + ".\nRegistro verificado...transacción aceptada.");
                    }else{
                        //Credencial duplicada en el pórtico.
                        publishProgress("5", "Registro realizado a las " + dateMark + ".\nRegla de verificación duplicada, registro no verificado...transacción aceptada.");
                        r = Globals.getInstance().getDatabase_manager().IngresarMarca(params[0], //Globals.getInstance().getId_RFID()
                                params[2], //now
                                params[3], //EventoSeleccionado
                                params[4], //Globals.getInstance().getParametrosSistema().get_Id_Portico()
                                Integer.parseInt(params[5]), //id_verificacion
                                params[6], //id_persona
                                params[7], //tipo_credencial
                                params[8], //nivel_autenticacion
                                params[9], //"Z0B99CC"
                                params[10], //Globals.getInstance().getParametrosSistema().get_Localizacion_Geografica()
                                params[11]); //"Z0B9E19"
                        publishProgress("70", "Registro realizado a las " + dateMark + ".\nRegla de verificación duplicada, registro no verificado...transacción aceptada.");
                    }
                }
            }catch (Exception ex){
                Log.e(TAG, "Error en Background:: hilo IngresarMarcaOffline");
            }
            return r;
        }

        @Override
        protected void onProgressUpdate(String... datos){

            progressDialogCargando.setProgress(progressDialogCargando.getProgress() + Integer.parseInt(datos[0]));
            progressDialogCargando.setMessage(datos[1]);
        }

        @Override
        protected void onPostExecute(Long result){
            try{
                if(result == -1){
                    Log.e(TAG, "No fue posible ingresar el registro ");
                    Notificacion("No fue posible ingresar el registro");
                }else{
                    progressDialogCargando.setProgress(progressDialogCargando.getProgress() + 15);
                    Thread.sleep(1000);
                    progressDialogCargando.dismiss();
                }
                setResult(RESULT_OK);
                finish();
            }catch (Exception ex){
                Log.e(TAG, "Error en PostExecute: hilo Ingreso marka offline");
            }

        }
    }
    private class IngresarTransitoOffline extends  AsyncTask<String, String, Long> {
        String dateMark;

        @Override
        protected void onPreExecute(){
            ShowProgressDialog("Registrando Transito...");
            progressDialogCargando.setProgress(10);
        }

        @Override
        protected Long doInBackground(String... params) {
            dateMark = params[0]; //HORA.
            long result=-1;

            try{
                publishProgress("40", "Registro realizado a las " + dateMark);
                result = Globals.getInstance().getDatabase_manager().IngresarTransito(params[1], //Hora_Fecha
                        params[2], //Id_Portico
                        params[3], //Id_Credencial
                        params[4]);//Id_Persona
                publishProgress("50", "Registro realizado a las " + dateMark);
            }catch(Exception ex){
                Log.e(TAG, "Problemas al ingresar el transito a la base de datos local"+ ex.getMessage());

                cancel(true);
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(String... progressInfo){
            progressDialogCargando.setProgress(progressDialogCargando.getProgress() + Integer.parseInt(progressInfo[0]));
            progressDialogCargando.setMessage(progressInfo[1]);
        }

        @Override
        protected void onPostExecute(Long result){
            try{
                if(result == -1){
                    Log.e(TAG, "No fue posible ingresar el registro de transito a la BD.");
                    Notificacion("No fue posible ingresar el registro de transito a la BD");
                }else{
                    progressDialogCargando.setProgress(30);
                    progressDialogCargando.setMessage("Registro realizado a las "+ dateMark);
                    Thread.sleep(5000);
                    progressDialogCargando.dismiss();
                }
            }catch (Exception ex){
                Log.e(TAG, "Problemas al finalizar hilo de ingreso de transito");
            }
            setResult(RESULT_OK);
            finish();
        }

        @Override
        protected void onCancelled(Long result){
            Notificacion("No fue posible el ingreso del transito a la BD local.");
            setResult(RESULT_OK);
            finish();
        }
    }

    /**********************************************************************************************/

    /**
     * Procedimiento utilizado para registrar la marca en la base de datos que correspoenda a su estado de coneccion.
     * Este contempla:
     * id_credencial, fecha_hora, tipo_evento, portico, id_verificacion, id_persona,
     * tipo_credencial, nivel_autenticacion, tipo_transaccion, estado_transaccion, estado_transferencia
     * tipo_evento puede ser - Z0B99D3 (IJ) - Z0B99D4 (TJ) - Z0B99D5 (IC) - Z0B99D6 (TC)
     * nivel_autenticacion puede ser - Z0B997F (Credencial) - Z0B9980 (Credencial + Pin) - Z0B9981 (Credencial + Huella Dactilar)
     * tipo_transaccion puede ser - Z0B99CF (En Linea) - Z0B99D0 (Batch)
     * estado_transaccion puede ser - Z0B99CA (Verificada) - Z0B99CB (No Verificada) - Z0B99CC (Regla Duplicada)
     * estado_transferencia puede ser - Z0B99DB (Encolada) - Z0B99DC (Se envio)
     *
     **/
    private void IngresarMarca(){
        try{
            int id_verificacion = -1;
            String id_persona = null;
            String tipo_credencial = null;
            String nivel_autenticacion = null;

            //Si existe la persona en la LDV, se obtienen los datos desde el datatable.
            if(persona != null){
                id_verificacion = persona.get(0).get_U0217F4();
                id_persona = persona.get(0).get_U0217F3();
                tipo_credencial = persona.get(0).get_U0217F2();
                nivel_autenticacion = persona.get(0).get_U0217F5();
            }

            //si el dispositivo manipula puerta, verificar.
            if(Globals.getInstance().getParametrosSistema().get_Manipulacion_Puerta().equals("Z0B9C4B")){
                verificarAperturaPuerta();
            }

            if(estadoConexion){
                //conectado
                SimpleDateFormat dateFormat2 = new SimpleDateFormat("hh:mm:ss");
                Date datenow = new Date();
                //Credencial no Existe en LDV.
                if(persona == null){
                    theU02916C.InsertMarca(EventsActivity.this,
                            "Registro realizado a las " + dateFormat2.format(datenow) + ".\nPersona no identificada, registro no verificado...transacción aceptada.",
                            Globals.getInstance().getId_RFID(),
                            EventoSeleccionado,
                            Globals.getInstance().getParametrosSistema().get_Id_Portico(),
                            id_verificacion,
                            id_persona,
                            tipo_credencial,
                            nivel_autenticacion,
                            "Z0B99CF",
                            "Z0B99CB",
                            "Z0B99DC",
                            Globals.getInstance().getParametrosSistema().get_Localizacion_Geografica(),
                            "Z0B9E19");

                    //depliego mensaje ..

                }else{//credencial existe en LDV.
                    //Credencial única en el pórtico.
                    if(persona.size() == 1){
                        theU02916C.InsertMarca(EventsActivity.this,
                                "Registro realizado a las " + dateFormat2.format(datenow) + ".\nRegistro verificado...transacción aceptada.",
                                Globals.getInstance().getId_RFID(),
                                EventoSeleccionado,
                                Globals.getInstance().getParametrosSistema().get_Id_Portico(),
                                id_verificacion,
                                id_persona,
                                tipo_credencial,
                                nivel_autenticacion,
                                "Z0B99CF",
                                "Z0B99CA",
                                "Z0B99DC",
                                Globals.getInstance().getParametrosSistema().get_Localizacion_Geografica(),
                                "Z0B9E19");
                        //depliego mensaje ..

                    }else{ //Credencial duplicada en el pórtico.
                        theU02916C.InsertMarca(EventsActivity.this,
                                "Registro realizado a las " + dateFormat2.format(datenow) + ".\nRegla de verificación duplicada, registro no verificado...transacción aceptada.",
                                Globals.getInstance().getId_RFID(),
                                EventoSeleccionado,
                                Globals.getInstance().getParametrosSistema().get_Id_Portico(),
                                id_verificacion,
                                id_persona,
                                tipo_credencial,
                                nivel_autenticacion,
                                "Z0B99CF",
                                "Z0B99CC",
                                "Z0B99DC",
                                Globals.getInstance().getParametrosSistema().get_Localizacion_Geografica(),
                                "Z0B9E19");
                        //depliego mensaje ..
                    }

                }
            }else{ //sin coneccion.
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
                SimpleDateFormat dateFormat2 = new SimpleDateFormat("hh:mm:ss");
                Date datenow = new Date();
                String now = dateFormat.format(datenow);
                String dateDeploy = dateFormat2.format(datenow);
                Log.w(TAG, "Hora actual: " + now);

                //Credencial no Existe en LDV.
                if(persona == null){

                    new IngresarMarcaOffLine().execute(Globals.getInstance().getId_RFID(),
                            dateDeploy,
                            now,
                            EventoSeleccionado,
                            Globals.getInstance().getParametrosSistema().get_Id_Portico(),
                            id_persona,
                            tipo_credencial,
                            nivel_autenticacion,
                            "Z0B99CB",
                            Globals.getInstance().getParametrosSistema().get_Localizacion_Geografica(),
                            "Z0B9E19");

                }else{
                    //Credencial única en el pórtico.
                    if(persona.size() == 1){

                        new IngresarMarcaOffLine().execute(Globals.getInstance().getId_RFID(),
                                dateDeploy,
                                now,
                                EventoSeleccionado,
                                Globals.getInstance().getParametrosSistema().get_Id_Portico(),
                                String.valueOf(id_verificacion),
                                id_persona,
                                tipo_credencial,
                                nivel_autenticacion,
                                "Z0B99CA",
                                Globals.getInstance().getParametrosSistema().get_Localizacion_Geografica(),
                                "Z0B9E19");

                    }else{//Credencial duplicada en el pórtico.

                        new IngresarMarcaOffLine().execute(Globals.getInstance().getId_RFID(),
                                dateDeploy,
                                now,
                                EventoSeleccionado,
                                Globals.getInstance().getParametrosSistema().get_Id_Portico(),
                                String.valueOf(id_verificacion),
                                id_persona,
                                tipo_credencial,
                                nivel_autenticacion,
                                "Z0B99CC",
                                Globals.getInstance().getParametrosSistema().get_Localizacion_Geografica(),
                                "Z0B9E19");
                    }
                }
                /*progressDialogCargando.setProgress(5);
                progressDialogCargando.setMessage("Marca Realizada a las "+ dateFormat.format(datenow));
                Thread.sleep(1000);
                progressDialogCargando.dismiss();*/
            }
        }catch (Exception ex){
            Log.e(TAG, "Error al intentar registrar la marca de asistencia");
        }
    }

    private void verificarAperturaPuerta(){
        Log.i(TAG, "Entro a verificar apuertura de puerta");
    }



}
