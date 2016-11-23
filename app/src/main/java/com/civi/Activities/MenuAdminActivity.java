package com.civi.Activities;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Button;

import com.civi.Globals;
import com.civi.R;
import com.civi.U02916C;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.InetAddress;
import java.security.acl.NotOwnerException;
import java.util.ArrayList;
import java.util.List;

import Opendat.Registro_del_Tiempo.Clases_Genericas.AntecedentesPersona;
import Opendat.Registro_del_Tiempo.Clases_Genericas.RegistroMarca;
import Opendat.Registro_del_Tiempo.Clases_Genericas.RegistroTransito;
import Opendat.Registro_del_Tiempo.Configuracion;

public class MenuAdminActivity extends AppCompatActivity {
    private static final String TAG = "AppMRAT";

    U02916C theU02916C = new U02916C(Globals.getInstance().getParametrosSistema().get_Url_WebServices());

    ProgressBar PB_barraProceso, PB_barraProceso_finito;
    ImageView ImageConexion, ImageConexionDb;
    Button transferirTransito, transferirMarca, actualizarLDV, actualizarParams, closeApp, openDoor, volver;

    AlertDialog.Builder alertDialog;



    /******************************************Listeners*******************************************/

    private OnClickListener VolverMain = new OnClickListener() {
        @Override
        public void onClick(View v) {

            setResult(RESULT_OK);
            finish();

        }
    };

    private OnClickListener CerrarApp = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setResult(RESULT_CANCELED);
            finish();
        }
    };

    private OnClickListener TransferenciaTransito = new OnClickListener() {
        @Override
        public void onClick(View v) {
            VerificarConexion(transferirTransito);

        }
    };

    private OnClickListener ActualizacionLDV = new OnClickListener() {
        @Override
        public void onClick(View v) {
            VerificarConexion(actualizarLDV);
        }
    };

    private OnClickListener ActualizacionParametros = new OnClickListener() {
        @Override
        public void onClick(View v) {
            VerificarConexion(actualizarParams);
        }
    };

    private OnClickListener TransferenciaMarcajes = new OnClickListener() {
        @Override
        public void onClick(View v) {
            VerificarConexion(transferirMarca);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_admin);

        volver = (Button) findViewById(R.id.BT_Volver_Admin);
        PB_barraProceso = (ProgressBar) findViewById(R.id.PB_Proceso_Admin);
        ImageConexion = (ImageView)findViewById(R.id.IV_Conexion_Ws_Admin);
        ImageConexionDb = (ImageView)findViewById(R.id.IV_Conexion_Db_Admin);
        transferirTransito = (Button)findViewById(R.id.BT_TransTransit);
        transferirMarca = (Button)findViewById(R.id.BT_TransMark);
        actualizarLDV = (Button)findViewById(R.id.BT_UpdateLDV);
        actualizarParams = (Button)findViewById(R.id.BT_UpdateParams);
        closeApp = (Button)findViewById(R.id.BT_CloseApp);
        openDoor = (Button)findViewById(R.id.BT_OpenDoor);

        volver.setOnClickListener(VolverMain);
        closeApp.setOnClickListener(CerrarApp);
        transferirTransito.setOnClickListener(TransferenciaTransito);
        actualizarLDV.setOnClickListener(ActualizacionLDV);
        actualizarParams.setOnClickListener(ActualizacionParametros);
        transferirMarca.setOnClickListener(TransferenciaMarcajes);

        VerificarConexion(null);

    }

    /**
     * Procedimiento usado para verificar la conexion tanto para la base de datos como para el webservice.
     *
     * @param botonLlamado: boton que ejecuta la verificacion. dependiendo del origen del llamado, el sistema se comportara de manera diferente.
     */
    private void VerificarConexion(Button botonLlamado)
    {
        try{
            if(botonLlamado == null){
                Globals.getInstance().setNumThreads(0);
            }else{
                Globals.getInstance().setNumThreads(2);
            }
            new isConnected(botonLlamado).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Globals.getInstance().getParametrosSistema().get_Base_Datos_IP());
            new isConnectedWS(botonLlamado).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }catch (Exception ex){
            Log.e(TAG, "Error al verificar conexion: "+ex.getMessage());
            Notificacion("Error al verificar conexion");
        }
    }

    /**
     * Procedimiento utilizado para el control de la disponibilidad de la botonera.
     * @param estado
     */
    public void EstadoBotonera(boolean estado) {
        volver.setEnabled(estado);
        PB_barraProceso.setEnabled(estado);
        transferirTransito.setEnabled(estado);
        transferirMarca.setEnabled(estado);
        actualizarLDV.setEnabled(estado);
        actualizarParams.setEnabled(estado);
        closeApp.setEnabled(estado);
        openDoor.setEnabled(estado);

    }
    private void Notificacion(String message){
        try{
            alertDialog = new AlertDialog.Builder(this);
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface alertDialog, int which) {
                    alertDialog.cancel();
                }
            });
            alertDialog.setTitle(message);
            alertDialog.show();
            if(PB_barraProceso != null){
                PB_barraProceso.setVisibility(View.INVISIBLE);
            }
        }catch (Exception ex){
            Log.e(TAG, "Error al enviar mensaje (EventsActivity):: "+ex.getMessage());
        }

    }

    private void ProcesoTransferenciaTransito() {
        try{
            List<RegistroTransito> registros = null;

            registros = Globals.getInstance().getDatabase_manager().Registros_Transito();
            if(registros == null){
                Log.e(TAG, "Error al obtener la lista de transito en la BD local");
                Notificacion("Error al obtener la lista de marcas de transito en la BD local");
            }else{
                ProgressBar pb = (ProgressBar)findViewById(R.id.PB_Proceso_Admin_2); // por porblemas de renderizado utilizando el PB_Progress_admin, se creo otro NO indeterminado.
                theU02916C.InsertTransitoOffline(this, pb, registros);
            }

        }catch (Exception ex){
            Log.e(TAG, "Error en proceso de transferencia de transito:: "+ex.getMessage());
        }

    }

    private void ProcesoActualizacionLDV(){
        try{
            PB_barraProceso_finito = (ProgressBar)findViewById(R.id.PB_Proceso_Admin_2);
            PB_barraProceso_finito.setIndeterminate(false);
            PB_barraProceso_finito.setVisibility(View.VISIBLE);
            PB_barraProceso_finito.setMax(100);
            PB_barraProceso_finito.setProgress(50);

            Globals.getInstance().setNumThreads(1);
            theU02916C.Antecedentes_Verificacion_SinFoto(this, "", Globals.getInstance().getParametrosSistema().get_Id_Portico());
            //theU02916C.Antecedentes_Verificacion(this,"", Globals.getInstance().getParametrosSistema().get_Id_Portico());
        }catch (Exception ex){
            Log.e(TAG, "Error en Proceso de Actualizacion de LDV:: "+ex.getMessage());
        }
    }
    /**
     * Procedimiento utilizado para proceder con la logica luego de la obtencion de antecedentes de la persona.
     * @param listadoLDV
     */
    public void ProcesoActualizacionLDV_TerminoHilo(List<AntecedentesPersona> listadoLDV){
        try{
            boolean result = Globals.getInstance().getDatabase_manager().ActualizarLDV(listadoLDV);

            PB_barraProceso_finito.setProgress(100);
            PB_barraProceso_finito.setVisibility(View.INVISIBLE);
            if(result){
                Notificacion("Actualizacion realizada exitosamente");
            }else{
                Notificacion("No fue posible la actualizacion");
            }
        }catch (Exception ex){
            Log.e(TAG, "Error en proceso de actualizacion de LDV (Termino Hilo):: " +ex.getMessage());
        }
    }

    private void ProcesoActualizacionParametros(){
        try{
            PB_barraProceso_finito = (ProgressBar)findViewById(R.id.PB_Proceso_Admin_2);
            PB_barraProceso_finito.setIndeterminate(false);
            PB_barraProceso_finito.setVisibility(View.VISIBLE);
            PB_barraProceso_finito.setMax(5);
            PB_barraProceso_finito.setProgress(0);


            Globals.getInstance().setNumThreads(4);

            //obtengo disponibilidad de chapa electrica.
            theU02916C.Disponibilidad_Puerta_Magnetica(this, PB_barraProceso_finito,Globals.getInstance().getParametrosSistema().get_Id_Portico());

            //obtengo la disposicion del portico.
            theU02916C.Verificar_Portico(this, PB_barraProceso_finito, Globals.getInstance().getParametrosSistema().get_Id_Portico());

            //obtengo la id de credencial del administrador del sistema.
            theU02916C.Credencial_Admin(this, PB_barraProceso_finito);

            //obtengo la localizacion geografica del portico.
            theU02916C.Localizacion_Geografica(this, PB_barraProceso_finito, Globals.getInstance().getParametrosSistema().get_Id_Portico());


        }catch (Exception ex){
            Log.e(TAG, "Error en procesar la actualizacion de parametros");
        }
    }
    /**
     * Procedimiento usado para la continuacion de la logica del proceso de actualizacion de parametros luego de terminados los hilos.
     */
    public void ProcesoActualizacionParametros_TerminoHilo(){
        try{
            //Se reconstruye el archivo de configuracion.
            Configuracion.ReescrituraConfig(Globals.getInstance().getPathData()+"/config",
                                            Globals.getInstance().getParametrosSistema().get_Url_WebServices(),
                                            Globals.getInstance().getParametrosSistema().get_Base_Datos_IP(),
                                            Globals.getInstance().getParametrosSistema().get_Id_Portico(),
                                            Globals.getInstance().getParametrosSistema().get_Manipulacion_Puerta(),
                                            Globals.getInstance().getParametrosSistema().get_Disposicion(),
                                            Globals.getInstance().getParametrosSistema().get_Id_CredencialAdmin(),
                                            Globals.getInstance().getParametrosSistema().get_Eventos(),
                                            Globals.getInstance().getParametrosSistema().get_Localizacion_Geografica());
            PB_barraProceso_finito.setVisibility(View.INVISIBLE);
            Notificacion("Parametros actualizados exitosamente");
        }catch (Exception ex){
            Log.e(TAG, "Error en procesar la actualizacion de parametros (termino hilo):: "+ex.getMessage());
        }
    }

    /**
     * Procedimiento utilizado para procesar la transferencia de marcas de asistencia desde la BD local a la BD del servidor.
     */
    private void ProcesoTransferenciaMarcajes(){
        try{
            List<RegistroMarca> registros = null;
            Globals globals = Globals.getInstance();
            registros = globals.getDatabase_manager().Registros_Marcas_De_Asistencia();
            if(registros == null){
                Log.e(TAG, "Error al obtener la lista de marcas en la BD local");
                Notificacion("Error al obtener la lista de marcas en la BD local");
            }else{
                ProgressBar pb = (ProgressBar)findViewById(R.id.PB_Proceso_Admin_2);
                theU02916C.InsertMarcaOffline(this, pb, registros);
            }

        }catch (Exception ex){
            Log.e(TAG, "Error en proceso de transferencia de marcas de asistencia:: "+ex.getMessage());
        }
    }

    private class isConnected extends AsyncTask<String, Void, Boolean> {
        Button llamadoPor=null;//variable que almacena el boton que lamó a la ejecucion de este hilo.


        public isConnected(Button llamado){
            llamadoPor=llamado;
        }

        @Override
        protected void onPreExecute(){
            EstadoBotonera(false);
            PB_barraProceso.setIndeterminate(true);
            PB_barraProceso.setVisibility(View.VISIBLE);

        }

        @Override
        protected Boolean doInBackground(String... params) {
            String ip = params[0];//IP base de datos.
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
                Globals.getInstance().decrementThread();
                if(Globals.getInstance().getNumThreads() == 0){
                    Log.i(TAG, "Ultimo hilo es isConnected");
                    //ultimo hilo en finalizar.
                    if(result && llamadoPor == null){
                        ImageConexionDb.setImageResource(R.drawable.ic_db_online);
                        EstadoBotonera(true); //habilito los botones para su uso.
                        Log.i(TAG, "Cambio a faro DB Conectado");
                    }else if(!result && llamadoPor == null){
                        ImageConexionDb.setImageResource(R.drawable.ic_db_offline);
                        EstadoBotonera(true); //habilito los botones para su uso.
                        Log.i(TAG, "Cambio a faro DB Desconectado");
                    }else if(result && llamadoPor == transferirTransito){
                        ImageConexionDb.setImageResource(R.drawable.ic_db_online);
                        ProcesoTransferenciaTransito();
                        EstadoBotonera(true);// Despues de procesar la transferencia.
                    }else if(!result && llamadoPor == transferirTransito){
                        ImageConexionDb.setImageResource(R.drawable.ic_db_offline);
                        //no hay conexion para realizar la tarea.
                        Notificacion("No es posible realizar la transferencia de archivos.\nNo hay conexion");
                        EstadoBotonera(true);
                    }else if(result && llamadoPor == actualizarLDV){
                        ImageConexionDb.setImageResource(R.drawable.ic_db_online);
                        ProcesoActualizacionLDV();
                    }else if (!result && llamadoPor == actualizarLDV){
                        ImageConexionDb.setImageResource(R.drawable.ic_db_offline);
                        Notificacion("No es posible realizar la transferencia de archivos.\nNo hay conexion.");
                        EstadoBotonera(true);
                    }else if(result && llamadoPor == actualizarParams){
                        ImageConexionDb.setImageResource(R.drawable.ic_db_online);
                        ProcesoActualizacionParametros();
                        EstadoBotonera(true);
                    }else if(!result && llamadoPor == actualizarParams){
                        ImageConexionDb.setImageResource(R.drawable.ic_db_offline);
                        Notificacion("No es posible realizar la actualizacion de parametros.\nNo hay conexion");
                        EstadoBotonera(true);
                    }else if(result && llamadoPor == transferirMarca){
                        ImageConexionDb.setImageResource(R.drawable.ic_db_online);
                        ProcesoTransferenciaMarcajes();
                        EstadoBotonera(true);
                    }else if(!result && llamadoPor == transferirMarca){
                        ImageConexionDb.setImageResource(R.drawable.ic_db_offline);
                        Notificacion("No es posible realizar la transferencia de marcas de asistencia.\nNo hay conexion");
                        EstadoBotonera(true);
                    }
                    PB_barraProceso.setIndeterminate(false);
                    PB_barraProceso.setVisibility(View.INVISIBLE);
                }else if(result){
                    ImageConexionDb.setImageResource(R.drawable.ic_db_online);
                }else{
                    ImageConexionDb.setImageResource(R.drawable.ic_db_offline);
                }


            }catch(Exception ex){
                Log.e(TAG, "Error en coneccion (post):: "+ ex.getMessage());
            }
        }
    }
    private class isConnectedWS extends AsyncTask<String, Void, Boolean>{
        String NAMESPACE = "http://tempuri.org/";
        String nombreFuncion = "U02916D";
        String URL = Globals.getInstance().getParametrosSistema().get_Url_WebServices();

        Button llamadoPor = null;

        public isConnectedWS(Button llamado){
            llamadoPor = llamado;
        }

        @Override
        protected void onPreExecute(){
            EstadoBotonera(false);
            PB_barraProceso.setIndeterminate(true);
            PB_barraProceso.setVisibility(View.VISIBLE);
        }

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
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                envio = false;
            }
            return envio;

        }

        @Override
        protected void onPostExecute(Boolean result){
            try{

                Globals.getInstance().decrementThread();
                if(Globals.getInstance().getNumThreads() == 0){
                    Log.i(TAG, "Ultimo hilo es isConnectedWS");
                    if(result && llamadoPor == null){
                        ImageConexion.setImageResource(R.drawable.ic_ws_online);
                        EstadoBotonera(true); //habilito los botones para su uso.
                        Log.i(TAG, "Cambio a faro WS Conectado");
                    }else if(!result && llamadoPor == null){
                        ImageConexion.setImageResource(R.drawable.ic_ws_offline);
                        EstadoBotonera(true); //habilito los botones para su uso.
                        Log.i(TAG, "Cambio a faro WS Desconectado");
                    }else if(result && llamadoPor == transferirTransito){
                        ImageConexion.setImageResource(R.drawable.ic_ws_online);
                        ProcesoTransferenciaTransito();
                        EstadoBotonera(true);// Despues de procesar la transferencia.
                    }else if(!result && llamadoPor == transferirTransito){
                        ImageConexion.setImageResource(R.drawable.ic_ws_offline);
                        //no hay conexion para realizar la tarea.
                        Notificacion("No es posible realizar la transferencia de archivos.\nNo hay conexion");
                        EstadoBotonera(true);
                    }else if(result && llamadoPor == actualizarLDV){
                        ImageConexion.setImageResource(R.drawable.ic_ws_online);
                        ProcesoActualizacionLDV();
                        EstadoBotonera(true);// Despues de procesar la actualizacion.
                    }else if(!result && llamadoPor == actualizarLDV){
                        ImageConexion.setImageResource(R.drawable.ic_ws_offline);
                        //no hay conexion para realizar la tarea.
                        Notificacion("No es posible realizar la transferencia de archivos.\nNo hay conexion");
                        EstadoBotonera(true);
                    }else if(result && llamadoPor == actualizarParams){
                        ImageConexion.setImageResource(R.drawable.ic_ws_online);
                        ProcesoActualizacionParametros();
                        EstadoBotonera(true);
                    }else if(!result && llamadoPor == actualizarParams){
                        ImageConexion.setImageResource(R.drawable.ic_ws_offline);
                        Notificacion("No es posible realizar la actualizacion de parametros.\nNo hay conexion");
                        EstadoBotonera(true);
                    }else if(result && llamadoPor == transferirMarca){
                        ImageConexion.setImageResource(R.drawable.ic_ws_online);
                        ProcesoTransferenciaMarcajes();
                        EstadoBotonera(true);
                    }else if(!result && llamadoPor == transferirMarca){
                        ImageConexion.setImageResource(R.drawable.ic_ws_offline);
                        Notificacion("No es posible realizar la transferencia de marcas de asistencia.\nNo hay conexion");
                        EstadoBotonera(true);
                    }
                    PB_barraProceso.setIndeterminate(false);
                    PB_barraProceso.setVisibility(View.INVISIBLE);
                }else if(result){ //si no fue el ultimo hilo, se verifica estado de coneccion.
                    ImageConexion.setImageResource(R.drawable.ic_ws_online);
                }else {
                    ImageConexion.setImageResource(R.drawable.ic_ws_offline);
                }

            }catch (Exception ex){
                Log.e(TAG, "Error en postExecute : hilo isConnectedWS ->"+ex.getMessage());
                Notificacion("Error de proceso: "+ex.getStackTrace());
                EstadoBotonera(true);
            }

        }

    }


}
