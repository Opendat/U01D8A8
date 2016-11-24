package com.civi;

import android.app.Activity;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.civi.Activities.EventsActivity;
import com.civi.Activities.FirstTimeActivity;
import com.civi.Activities.MainActivity;
import com.civi.Activities.MenuAdminActivity;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.MarshalHashtable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import Opendat.Registro_del_Tiempo.Clases_Genericas.AntecedentesPersona;
import Opendat.Registro_del_Tiempo.Clases_Genericas.Portico;
import Opendat.Registro_del_Tiempo.Clases_Genericas.EventoPuertaMagnetica;
import Opendat.Registro_del_Tiempo.Clases_Genericas.RegistroMarca;
import Opendat.Registro_del_Tiempo.Clases_Genericas.RegistroTransito;

/**
 * Clase que representa el webservice a utilizar.
 *
 * Modificaciones:
 * Fecha                    Autor                   Descripcion
 * -------------------------------------------------------------------------------------------------
 * 12.10.2016               Jonathan Vasquez        Creacion de Clase
 * 25.10.2016               Jonathan Vasquez        Creacion de objeto en diccionario y modificacion de nombre de ws a U02916C (Anteriormente U021E30).
 * 02.11.2016               Jonathan Vasquez        Encriptación de identificadores de las funciones de ws, generacion de Ants_Verificacion.
 */

public class U02916C {
    private static final String TAG = "AppMRAT";
    private AlertDialog.Builder alertDialog = null;
    private ProgressDialog progressDialog = null;
    private ProgressBar miBarra = null;
    //variable que almacena el URL del Webservice desde los parametros del sistema.
    private String URL;
    private AsyncTask.Status estadoCall;
    private String NAMESPACE = "http://tempuri.org/";
    Globals global = Globals.getInstance();
    Activity origen; //variable usada en 'CallFechaHora_verificacionWS' cuyo objetivo es diferenciar la logica dependiendo del llamado.
    /**
     * Constructores.
     */
    public U02916C(){}
    public U02916C(String pstrURL) {
        this.URL = pstrURL;
        estadoCall = null;
    }

    public void configAlertDialog(final Activity activity){
        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface alertDialog, int which) {
                if(activity instanceof EventsActivity){
                    //se retorna a MainActivity;
                    activity.setResult(Activity.RESULT_OK);
                    activity.finish();
                }else if(activity instanceof MainActivity){
                    //cancelo.
                    alertDialog.cancel();
                }else if(activity instanceof FirstTimeActivity){

                    alertDialog.cancel();
                }else if(activity instanceof MenuAdminActivity){
                    alertDialog.cancel();

                }
            }
        });
    }

    //getter and setters

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public AsyncTask.Status getEstadoCall() {
        return estadoCall;
    }

    public AlertDialog.Builder getAlertDialog() {
        return alertDialog;
    }

    public void setAlertDialog(AlertDialog.Builder alertDialog) {
        this.alertDialog = alertDialog;
    }

    public ProgressBar getMiBarra() {
        return miBarra;
    }

    public void setMiBarra(ProgressBar miBarra) {
        this.miBarra = miBarra;
    }




    /**
     * Funcion usaba tanto para obtener la hora del servidor como para la verificacion de la existencia del webservice.
     */
    public void verificarWS(Activity llamado, String pstrId_Portico){
        this.origen = llamado;

        configAlertDialog(llamado);

        //no hay parametros, no va PropertyInfo.
        Log.i(TAG, "Ejecutando hilo llamado a ws...");
        new CallFechaHora_verificacionWS().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"U02916D",llamado.getLocalClassName(), pstrId_Portico);
        //call.execute("ObtenerFechaHora");
    }

    public void Buscar_Portico(Activity llamado, String pstrId_Portico){
        Log.i(TAG, "Llamado 'Buscar_Portico' realizado");
        this.origen = llamado;
        //new CallWSDato().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"Buscar_Portico", pstrId_Portico);
        new CallWSBuscar_Portico().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pstrId_Portico);
    }

    public void Disponibilidad_Puerta_Magnetica(Activity llamado, ProgressBar pb, String pstrId_Portico){
        Log.i(TAG, "Llamado 'Disponibilidad chapa' realizada");
        this.origen = llamado;
        configAlertDialog(llamado);

        if(origen instanceof MenuAdminActivity){
            miBarra = pb;
        }
        //new CallWSDato().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"Get_DisponibilidadChapaElectrica", pstrId_Portico);
        new CallWSDisponibilidad_Puerta_Magnetica().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pstrId_Portico);
    }

    public void Verificar_Portico(Activity llamado, ProgressBar pb, String pstrId_Portico){
        Log.i(TAG, "Llamado 'Verificar Disponibilidad Portico' realizada");
        this.origen = llamado;
        configAlertDialog(llamado);

        if(origen instanceof MenuAdminActivity){
            miBarra = pb;
        }
        new CallWSVerificar_Portico().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pstrId_Portico);

    }

    public void Credencial_Admin(Activity llamado, ProgressBar pb){
        Log.i(TAG, "Llamado 'Credencial_Admin' realizada");
        this.origen = llamado;
        configAlertDialog(llamado);

        if(origen instanceof MenuAdminActivity){
            miBarra = pb;
        }
        new CallWSCredencial_Admin().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void Localizacion_Geografica(Activity llamado, ProgressBar pb, String pstrId_Portico){
        Log.i(TAG, "Llamado 'Localizacion_Geografica' realizada");
        this.origen = llamado;
        configAlertDialog(llamado);

        if(origen instanceof MenuAdminActivity){
            miBarra = pb;
        }
        new CallWSLocalizacion_Geografica().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pstrId_Portico);
    }

    public void Eventos_Con_Puerta_Magnetica(Activity llamado, String pstrId_Portico){
        this.origen = llamado;

        new CallWSEventos_Puerta_Magnetica().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pstrId_Portico);

    }

    public void Antecedentes_Verificacion(Activity llamado, String pstrId_Credencial, String pstrPortico){
        this.origen = llamado;
        configAlertDialog(llamado);
        new CallWSAnts_VerificacionNoFotoHuella().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pstrId_Credencial, pstrPortico);
    }

    public void Antecedentes_Verificacion_SinFoto(Activity llamado, String pstrId_Credencial, String pstrPortico){//usado para actualizar ldv.
        this.origen = llamado;

        configAlertDialog(llamado);
        /*alertDialog = new AlertDialog.Builder(origen);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface alertDialog, int which) {
                alertDialog.cancel();
            }
        });*/

        new CallWSAnts_VerificacionNoFoto().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pstrId_Credencial, pstrPortico);
    }

    public void Antecedente_Fotografia(Activity llamado, String pstrId_Credencial, String pstrPortico){
        this.origen = llamado;
        new CallWSAnts_Veri_Foto().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pstrId_Credencial, pstrPortico);
    }
    public void Antecedente_Huella(Activity llamado, String pstrId_Credencial, String pstrPortico){
        this.origen = llamado;


    }

    public void Verificar_Reingreso(Activity llamado, String pstrId_Credencial, String pstrEvento, String pstrPortico){
        this.origen = llamado;
        new CallWSVerificar_Reingreso().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pstrId_Credencial, pstrEvento, pstrPortico);
    }

    public void InsertMarca(Activity llamado,String mensajeMarca,
                            String pstrId_Credencial, String pstrEvento, String pstrId_Portico,
                            int pintId_Verificacion, String pstrId_Persona, String pstrTipo_Credencial, String pstrNivel_Autenti,
                            String pstrTipo_Transaccion, String pstrEstado_Transaccion, String pstrEstado_Transferencia,
                            String pstrLocalizacion_Geo, String pstrOrigen_Marca){

        this.origen = llamado;

        configAlertDialog(llamado);
        progressDialog = new ProgressDialog(origen);
        progressDialog.setTitle("Registrando Marcaje...");
        progressDialog.setMessage(mensajeMarca);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);

        if(pintId_Verificacion == -1){ //equivalente a que no tiene verificacion.
            new CallWSInsertar_Marca(progressDialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    pstrId_Credencial, pstrEvento, pstrId_Portico, null, pstrId_Persona, pstrTipo_Credencial, pstrNivel_Autenti,
                    pstrTipo_Transaccion, pstrEstado_Transaccion, pstrEstado_Transferencia, pstrLocalizacion_Geo, pstrOrigen_Marca);
        }else{
            new CallWSInsertar_Marca(progressDialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    pstrId_Credencial, pstrEvento, pstrId_Portico, String.valueOf(pintId_Verificacion), pstrId_Persona, pstrTipo_Credencial,
                    pstrNivel_Autenti, pstrTipo_Transaccion, pstrEstado_Transaccion, pstrEstado_Transferencia,
                    pstrLocalizacion_Geo, pstrOrigen_Marca);
        }
    }

    public void InsertTransito(Activity llamado, String pstrId_Portico, String pstrId_Credencial
                                , String pstrId_Persona){
        this.origen = llamado;

        configAlertDialog(llamado);
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        Date datenow = new Date(); //solo para desplegar la fecha y hora del dispositivo.

        progressDialog = new ProgressDialog(origen);

        progressDialog.setTitle("Registrando Transito...");
        progressDialog.setMessage("Hora de transito: "+dateFormat.format(datenow));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);

        new CallWSInsertar_Marca_Transito(progressDialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                pstrId_Portico, pstrId_Credencial, pstrId_Persona);
    }


    public void InsertTransitoOffline(Activity llamado, ProgressBar pb,  List<RegistroTransito> registrosTransito){
        this.origen = llamado;
        configAlertDialog(llamado);
        this.miBarra = pb;

        new CallWSInsertar_Marca_Transito_Offline(registrosTransito).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void InsertMarcaOffline(Activity llamado, ProgressBar pb, List<RegistroMarca> registrosMarca){
        this.origen = llamado;
        configAlertDialog(llamado);
        this.miBarra = pb;

        new CallWSInsertar_Marca_Offline(registrosMarca).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //threads
    private class CallFechaHora_verificacionWS extends AsyncTask<String, Void, String[]> {
        String idPortico;
        @Override
        protected String [] doInBackground(String... params){
            Log.i(TAG, "CallFechaHora_verificacionWS en backGround...: "+NAMESPACE+params[0]+ "URL: "+URL);
            estadoCall = this.getStatus();//obtengo el estado.
            String [] result = new String [3];

            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
            headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).
            SoapObject request = new SoapObject(NAMESPACE, params[0]);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);

            try{
                transporte.call(NAMESPACE + params[0], envelope, headers);
                SoapPrimitive resSoap = (SoapPrimitive)envelope.getResponse();
                result[0] = resSoap.toString(); //resultado del llamado (Fecha)
                result[1] = params[0]; //nombre de la funcion.
                result[2] = params[1]; //nombre de la clase en donde fue llamado. (FirstTimeActivity, MainActivity).
                idPortico = params[2]; //id de portico

            }catch (SoapFault sf){

                String faultString = "FAULT: Code: " + sf.faultcode + "\nString: " +
                        sf.faultstring;
                Log.d(TAG , "fault : " + faultString);

            }
            catch (IOException e) {//entra aqui cuando no logra realizar la coneccion.
                //e.printStackTrace();
                Log.e(TAG, "Excepcion de IOE (ws): " + e.getMessage());
                result = null;
                cancel(true);

            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                Log.e(TAG, "Excepcion de XmlPullParser");
                result = null;
                cancel(true);
            }
            Log.i(TAG, "Saliendo de background...");
            return result;
        }

        @Override
        protected void onCancelled(String [] result){
            if(miBarra != null){
                miBarra.setVisibility(View.INVISIBLE);
            }
            Log.i(TAG, "Entra a funcion onCancelled de verificacion ws");
            Notificacion("No es posible conectarse a WS");
            if(origen instanceof FirstTimeActivity){
                ((FirstTimeActivity) origen).ConfiguracionSistema_CorteSecuenciaHilo();
            }
        }

        @Override
        protected void onPostExecute(String [] resultado){
            Log.i(TAG, "Entra a funcion PostExecute de verificacion ws");
            estadoCall = this.getStatus();

            Log.i(TAG, "Resultado final: "+resultado[0]);
            global.setDato(resultado[0]);
            //se verifica la existencia del portico.
            new CallWSBuscar_Portico().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, idPortico);
            //new CallWSDato().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "Get_LocalizacionGeografica", idPortico);

            Log.i(TAG, "resto el hilo de verificacion de ws");
            global.decrementThread();
            if (global.getNumThreads() == 0){
                Log.i(TAG, "ultimo hilo: Verificacion WS");
                //miBarra.setVisibility(View.INVISIBLE);
                if(resultado[2].equals("Activities.FirstTimeActivity")){
                    //origen.GeneracionConfig();
                    ((FirstTimeActivity)origen).configuracionSistema();
                }
            }

        }
    }

    private class CallWSVerificar_Portico extends AsyncTask<String, Void, List<Portico>>{
        List<Portico> listaPortico;

        private String nombreFuncion = "U029170";
        @Override
        protected List<Portico> doInBackground(String... params) {
            Log.i(TAG, "comienzo de ejecucion en background de hilo 'Verificar_portico'");
            String result;

            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
            headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).

            SoapObject request = new SoapObject(NAMESPACE, nombreFuncion);
            //parametros.
            request.addProperty("pstrCod_Portico", params[0]); //sensible a nombre de parametros.

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);

            try {
                transporte.call(NAMESPACE + nombreFuncion, envelope, headers);
                SoapObject resSoap = (SoapObject) envelope.getResponse();

                //se interpreta la lista.
                listaPortico = new ArrayList<>();
                for(int i = 0; i < resSoap.getPropertyCount(); i++){
                    SoapObject ic = (SoapObject) resSoap.getProperty(i);
                    Portico p = new Portico();
                    p.set_ubicacion(ic.getProperty(0).toString());
                    p.set_descripcion_ubicacion(ic.getProperty(1).toString());
                    p.set_site(ic.getProperty(2).toString());
                    p.set_cod_centro_trabajo(ic.getProperty(3).toString());
                    p.set_descripcion_centro_trabajo(ic.getProperty(4).toString());
                    p.set_tipo_dependencia(ic.getProperty(5).toString());
                    p.set_exclusividad_cdt(ic.getProperty(6).toString());
                    p.set_empresa_exclusiva(ic.getProperty(7).toString());
                    p.set_cod_portico(ic.getProperty(8).toString());
                    p.set_descripcion_portico(ic.getProperty(9).toString());
                    p.set_tipo_portico(ic.getProperty(10).toString());
                    p.set_coordenadas(ic.getProperty(11).toString());
                    p.set_tipo_disposicion(ic.getProperty(12).toString());
                    p.set_clase_credencializacion(ic.getProperty(13).toString());
                    p.set_tipo_dispositivo(ic.getProperty(14).toString());
                    p.set_identificacion_dispositivo(ic.getProperty(15).toString());
                    p.set_direccion_ip(ic.getProperty(16).toString());
                    p.set_mascara_ip(ic.getProperty(17).toString());
                    p.set_get_way(ic.getProperty(18).toString());
                    listaPortico.add(p);
                }

            } catch (IOException e) {//no encuentra respuesta (No existe o no Existe la direccion a webservice).
                //e.printStackTrace();
                listaPortico = null;
                cancel(true);
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                listaPortico = null;
                cancel(true);
            }
            return listaPortico;

        }

        @Override
        protected void onCancelled(List<Portico> result) {
            if(miBarra != null){
                miBarra.setVisibility(View.INVISIBLE);
            }
            Log.i(TAG, "Se ha entrado a funcion onCancelled de verificar_portico");
            Notificacion("No ha sido posible obtener la informacion sobre el portico");
            if(origen instanceof FirstTimeActivity){
                ((FirstTimeActivity) origen).ConfiguracionSistema_CorteSecuenciaHilo();
            }
        }

        @Override
        protected void onPostExecute(List<Portico> respuesta){
            Log.i(TAG, "Ingreso a funcion onPOst de verificar_portico");

            //asigno a parametro de sistema al tipo de disposisicion del dispositivo.
            if(respuesta.size() == 0){
                Notificacion("No ha sido posible obtener la informacion sobre el portico");
                if(origen instanceof FirstTimeActivity){
                    ((FirstTimeActivity) origen).ConfiguracionSistema_CorteSecuenciaHilo();
                }
            }else{
                global.getParametrosSistema().set_Disposicion(respuesta.get(0).get_tipo_disposicion());
                global.decrementThread();
                if(global.getNumThreads() == 0){
                    Log.i(TAG, "Ultimo hilo: verificar_portico");

                    if(origen instanceof FirstTimeActivity){//se que sera para configuracion.
                        Log.i(TAG, "retorno a FirstTimeActivity");
                        if(miBarra != null){
                            miBarra.setVisibility(View.INVISIBLE);
                        }
                        //envio al siguiente bloque logico.
                        //((FirstTimeActivity)origen).continuacion();
                        ((FirstTimeActivity)origen).generateXML();
                    }else if(origen instanceof MainActivity){//sin uso, solo como informacion de retorno
                        if(miBarra != null){
                            miBarra.setVisibility(View.INVISIBLE);
                        }
                        Log.i(TAG, "retorno a MainActivity");
                    }else if(origen instanceof MenuAdminActivity){//lamado desde interfaz de administracion.
                        Log.i(TAG, "Retorno a MenuAdminActivity");
                        if(miBarra.getProgress() < miBarra.getMax()){
                            miBarra.setProgress(miBarra.getProgress() +1);
                        }
                        ((MenuAdminActivity) origen).ProcesoActualizacionParametros_TerminoHilo();
                    }
                }
            }


        }
    }

    private class CallWSBuscar_Portico extends AsyncTask<String, Void, String>{
        private String nombreFuncion = "U02916E";
        @Override
        protected String  doInBackground(String... params){
            Log.i(TAG, "Entro a hilo Buscar_Portico");
            String result;

            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
            headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).

            SoapObject request = new SoapObject(NAMESPACE, nombreFuncion);
            //parametros.
            request.addProperty("idPortico", params[0]);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);

            try {
                transporte.call(NAMESPACE + nombreFuncion, envelope, headers);
                SoapPrimitive resSoap = (SoapPrimitive) envelope.getResponse();
                result = resSoap.toString();

            } catch (IOException e) {//no encuentra respuesta (No existe o no Existe la direccion a webservice).
                //e.printStackTrace();
                result = null;
                cancel(true);
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                result = null;
                cancel(true);
            }
            return result;
        }

        @Override
        protected void onCancelled(String result){
            Log.i(TAG, "Entra a funcion onCancelled de Buscar_portico");
            Notificacion("No es posible obtener la informacion del portico ingresado");
            if(origen instanceof FirstTimeActivity){
                ((FirstTimeActivity) origen).ConfiguracionSistema_CorteSecuenciaHilo();
            }
        }

        @Override
        protected void onPostExecute(String resultado){
            Log.i(TAG, "Entro a funcion onPost de funcion Buscar_Portico, resultado: "+resultado);
            global.decrementThread();
            if(global.getNumThreads() == 0){
                Log.i(TAG, "Ultimo hilo: Buscar_portico");
                if(miBarra != null){
                    miBarra.setVisibility(View.INVISIBLE);
                }
                if(origen instanceof FirstTimeActivity){//se que sera para configuracion.
                    Log.i(TAG, "retorno a FirstTimeActivity");
                    ((FirstTimeActivity)origen).configuracionSistema();
                }else if(origen instanceof MainActivity){
                    Log.i(TAG, "retorno a MainActivity");
                }
            }
        }
    }

    private class CallWSDisponibilidad_Puerta_Magnetica extends AsyncTask<String, Void, String> {
        private String nombreFuncion = "U02916F";
        private String idPortico;

        @Override
        protected String doInBackground(String... params){
            Log.i(TAG, "Entro al hilo de disponibilidad de puerta magnetica");
            String result;

            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
            headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).

            SoapObject request = new SoapObject(NAMESPACE, nombreFuncion);
            //parametros.
            //request.addProperty("IdPortico", params[1]);
            request.addProperty("idPortico", params[0]);
            idPortico = params[0];

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);

            try {
                transporte.call(NAMESPACE + nombreFuncion, envelope, headers);
                SoapPrimitive resSoap = (SoapPrimitive) envelope.getResponse();
                result = resSoap.toString();

            } catch (IOException e) {//no encuentra respuesta (No existe o no Existe la direccion a webservice).
                //e.printStackTrace();
                result = null;
                cancel(true);
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                result = null;
                cancel(true);
            }
            return result;


        }

        @Override
        protected void onCancelled(String result){
            if(miBarra != null){
                miBarra.setVisibility(View.INVISIBLE);
            }
            Log.i(TAG, "Entra a funcion onCancelled de Get_DisponibilidadChapaElectrica");
            Notificacion("No es posible obtener la informacion de la disponibilidad de puerta magnetica");
            if(origen instanceof FirstTimeActivity){
                ((FirstTimeActivity)origen).ConfiguracionSistema_CorteSecuenciaHilo();
            }
        }

        @Override
        protected void onPostExecute(String resultado){
            Log.i(TAG, "Entro a funcion onPost de funcion Get_DisponibilidadChapaElectrica, resultado: "+resultado);
            //asigno a parametros del sistema
            global.getParametrosSistema().set_Manipulacion_Puerta(resultado);

            if(global.getParametrosSistema().get_Manipulacion_Puerta().equals("Z0B9C4B")){//SI
                //se incrementa por la utilizacion de un nuevo hilo
                global.incrementThread();
                //obtengo los eventos asignados a la manipulacion de puerta magnetica.
                new CallWSEventos_Puerta_Magnetica().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, idPortico);
            }

            global.decrementThread();
            if(global.getNumThreads() == 0){
                Log.i(TAG, "Ultimo hilo: Disponibilidad_Puerta");
                if(origen instanceof FirstTimeActivity){//se que sera para configuracion.
                    Log.i(TAG, "retorno a FirstTimeActivity");
                    if(miBarra != null){
                        miBarra.setVisibility(View.INVISIBLE);
                    }
                    //envio a siguiente bloque logico.
                    ((FirstTimeActivity)origen).generateXML();
                }else if(origen instanceof MainActivity){//sin uso, solo como informacion de retorno.
                    Log.i(TAG, "retorno a MainActivity");
                    if(miBarra != null){
                        miBarra.setVisibility(View.INVISIBLE);
                    }
                }else if(origen instanceof MenuAdminActivity){//lamado desde la interfaz de administracion.
                    Log.i(TAG, "Retorno a MenuAdminActivity");
                    if(miBarra.getProgress() < miBarra.getMax()){
                        miBarra.setProgress(miBarra.getProgress() +1);
                    }
                    ((MenuAdminActivity) origen).ProcesoActualizacionParametros_TerminoHilo();
                }
            }
        }
    }

    private class CallWSCredencial_Admin extends AsyncTask<Void, Void, String>{
        private String nombreFuncion = "U029171";
        @Override
        protected String doInBackground(Void... params) {
            Log.i(TAG, "Entro al hilo de Credencial_Admin");
            String result;

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

            } catch (IOException e) {//no encuentra respuesta (No existe o no Existe la direccion a webservice).
                //e.printStackTrace();
                result = null;
                cancel(true);
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                result = null;
                cancel(true);
            }
            return result;

        }

        @Override
        protected void onCancelled(String result){
            if(miBarra != null){
                miBarra.setVisibility(View.INVISIBLE);
            }
            Log.i(TAG, "Entra a funcion onCancelled de Credencial_Admin");
            Notificacion("No es posible obtener la informacion del codigo de la credencial asignada al administrador");
            if(origen instanceof FirstTimeActivity){
                ((FirstTimeActivity)origen).ConfiguracionSistema_CorteSecuenciaHilo();
            }
        }

        @Override
        protected void onPostExecute(String resultado){
            Log.i(TAG, "Entro a funcion onPost de funcion Credencial_Admin, resultado: "+resultado);

            //asigno a parametro del sistema.
            global.getParametrosSistema().set_Id_CredencialAdmin(resultado);

            global.decrementThread();
            if(global.getNumThreads() == 0){
                Log.i(TAG, "Ultimo Hilo: Credencial_Admin");

                if(origen instanceof FirstTimeActivity){//se que sera para configuracion.
                    Log.i(TAG, "retorno a FirstTimeActivity");
                    if(miBarra != null){
                        miBarra.setVisibility(View.INVISIBLE);
                    }
                    //envio a siguiente bloque logico.
                    //((FirstTimeActivity)origen).continuacion();
                    ((FirstTimeActivity)origen).generateXML();
                }else if(origen instanceof MainActivity){//sin uso, solo como informacion de retorno.
                    Log.i(TAG, "retorno a MainActivity");
                    if(miBarra != null){
                        miBarra.setVisibility(View.INVISIBLE);
                    }
                }else if(origen instanceof MenuAdminActivity){
                    Log.i(TAG, "Retorno a MenuAdminActivity");

                    if(miBarra.getProgress() < miBarra.getMax()){
                        miBarra.setProgress(miBarra.getProgress() +1);
                    }
                    ((MenuAdminActivity) origen).ProcesoActualizacionParametros_TerminoHilo();
                }
            }
        }
    }

    private class CallWSLocalizacion_Geografica extends AsyncTask<String, Void, String> {
        private String nombreFuncion = "U029172";
        @Override
        protected String doInBackground(String... params){
            Log.i(TAG, "Entro al hilo de localizacion geografica");
            String result;

            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
            headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).

            SoapObject request = new SoapObject(NAMESPACE, nombreFuncion);
            //parametros.
            request.addProperty("idPortico", params[0]);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);

            try {
                transporte.call(NAMESPACE + nombreFuncion, envelope, headers);
                SoapPrimitive resSoap = (SoapPrimitive) envelope.getResponse();
                result = resSoap.toString();

            } catch (IOException e) {//no encuentra respuesta (No existe o no Existe la direccion a webservice).
                //e.printStackTrace();
                result = null;
                cancel(true);
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                result = null;
                cancel(true);
            }
            return result;


        }

        @Override
        protected void onCancelled(String result){
            if(miBarra != null){
                miBarra.setVisibility(View.INVISIBLE);
            }
            Log.i(TAG, "Entra a funcion onCancelled de Localizacion_Geografica");
            Notificacion("No es posible obtener la informacion de la localizacion geografica del dispositivo ingresado. Imposible la configuracion");
            if(origen instanceof FirstTimeActivity ){
                ((FirstTimeActivity) origen).ConfiguracionSistema_CorteSecuenciaHilo();
            }
        }

        @Override
        protected void onPostExecute(String resultado){
            Log.i(TAG, "Entro a funcion onPost de funcion Localizacion_geografica, resultado: "+resultado);

            //asigno como parametro del sistema
            global.getParametrosSistema().set_Localizacion_Geografica(resultado);

            global.decrementThread();
            if(global.getNumThreads() == 0){
                Log.i(TAG, "Ultimo hilo: Localizacion_geografica");
                if(origen instanceof FirstTimeActivity){//se que sera para configuracion.
                    Log.i(TAG, "retorno a FirstTimeActivity");
                    if(miBarra != null){
                        miBarra.setVisibility(View.INVISIBLE);
                    }
                    //envio a siguiente bloque logico.
                    //((FirstTimeActivity)origen).continuacion();
                    ((FirstTimeActivity)origen).generateXML();
                }else if(origen instanceof MainActivity){
                    Log.i(TAG, "retorno a MainActivity");
                    if(miBarra != null){
                        miBarra.setVisibility(View.INVISIBLE);
                    }
                }else if(origen instanceof MenuAdminActivity){
                    Log.i(TAG, "Retorno a MenuAdminActivity");
                    if(miBarra.getProgress() < miBarra.getMax()){
                        miBarra.setProgress(miBarra.getProgress() + 1);
                    }
                    ((MenuAdminActivity) origen).ProcesoActualizacionParametros_TerminoHilo();
                }
            }
        }
    }

    private class CallWSEventos_Puerta_Magnetica extends AsyncTask<String, Void, List<EventoPuertaMagnetica>>{
        private String nombreFuncion = "U029173";
        private String relacion = global.getParametrosSistema().get_Id_Relacion();//nesesario que este aqui.
        List<EventoPuertaMagnetica> listaEventos;

        @Override
        protected List<EventoPuertaMagnetica> doInBackground(String... params) {
            Log.i(TAG, "comienzo de ejecucion en background de hilo 'Eventos_Puerta_Magnetica'");
            String result;

            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
            headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).

            SoapObject request = new SoapObject(NAMESPACE, nombreFuncion);
            //parametros.
            request.addProperty("rel", relacion); //sensible a nombre de parametros.
            request.addProperty("cod_Portico", params[0]);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);

            try {
                transporte.call(NAMESPACE + nombreFuncion, envelope, headers);
                SoapObject resSoap = (SoapObject) envelope.getResponse();

                //se interpreta la lista.
                listaEventos = new ArrayList<>();
                for(int i = 0; i < resSoap.getPropertyCount(); i++){
                    SoapObject ic = (SoapObject) resSoap.getProperty(i);
                    EventoPuertaMagnetica evento = new EventoPuertaMagnetica();
                    //evento.set_U01B3F3(ic.getProperty(2).toString());
                    evento.set_U01B3F3(ic.getProperty(0).toString());
                    //evento.set_DESTINO(ic.getProperty(6).toString());
                    evento.set_DESTINO(ic.getProperty(1).toString());
                    listaEventos.add(evento);
                }

                return listaEventos;

            } catch (IOException e) {//no encuentra respuesta (No existe o no Existe la direccion a webservice).
                //e.printStackTrace();
                listaEventos = null;
                cancel(true);
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                listaEventos = null;
                cancel(true);
            }
            return listaEventos;
        }

        @Override
        protected void onCancelled(List<EventoPuertaMagnetica> result) {
            if(miBarra != null){
                miBarra.setVisibility(View.INVISIBLE);
            }
            Log.i(TAG, "Se ha entrado a funcion onCancelled de Eventos_Puerta_Magnetica");
            Notificacion("No ha sido posible obtener la informacion sobre los eventos asignados a la manipulacion de puerta magnetica en este dispositivo");

        }

        @Override
        protected void onPostExecute(List<EventoPuertaMagnetica> respuesta){
            Log.i(TAG, "Ingreso a funcion onPOst de Eventos_Puerta_Magnetica");

            //asigno a parametros del sistema
            global.getParametrosSistema().set_Eventos(respuesta);

            global.decrementThread();
            if(global.getNumThreads() == 0){
                Log.i(TAG, "Ultimo hilo: Eventos_Puerta_Magnetica");

                if(origen instanceof FirstTimeActivity){//se que sera para configuracion.
                    Log.i(TAG, "retorno a FirstTimeActivity");
                    if(miBarra != null){
                        miBarra.setVisibility(View.INVISIBLE);
                    }
                    //envio al siguiente bloque logico.
                    //((FirstTimeActivity)origen).continuacion();
                    ((FirstTimeActivity)origen).generateXML();
                }else if(origen instanceof MainActivity){//sin usu, solo como informacion hacia main activity.
                    Log.i(TAG, "retorno a MainActivity");
                    if(miBarra != null){
                        miBarra.setVisibility(View.INVISIBLE);
                    }
                }else if(origen instanceof MenuAdminActivity){
                    Log.i(TAG, "Retorno a MenuAdminActivity");
                    if(miBarra.getProgress() < miBarra.getMax()){
                        miBarra.setProgress(miBarra.getProgress() +1);
                    }
                    ((MenuAdminActivity) origen).ProcesoActualizacionParametros_TerminoHilo();
                }
            }
        }
    }

    private class CallWSAnts_VerificacionNoFoto extends AsyncTask<String, Void, List<AntecedentesPersona>> {
        byte [] arrayVacio = new byte[64];
        String nombreFuncion = "U029174";
        List<AntecedentesPersona> listaAntecedentes = null;

        @Override
        protected void onPreExecute() {
            for(int i=0; i<arrayVacio.length; i++){
                arrayVacio[i] = 0;
            }
        }


        @Override
        protected List<AntecedentesPersona> doInBackground(String... params) {
            Log.i(TAG, "Entro al hilo de Antecedentes de verifiacion");

            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
            headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).

            SoapObject request = new SoapObject(NAMESPACE, nombreFuncion);
            request.addProperty("pstrId_Credencial", params[0]);
            request.addProperty("pstrPortico", params[1]);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);
            try{
                transporte.call(NAMESPACE + nombreFuncion, envelope, headers);
                SoapObject resSoap = (SoapObject) envelope.getResponse();

                listaAntecedentes = new ArrayList<>();
                for(int i= 0; i<resSoap.getPropertyCount(); i++){
                    SoapObject ic = (SoapObject) resSoap.getProperty(i);
                    AntecedentesPersona ap = new AntecedentesPersona();
                    ap.set_U0217F1(ic.getProperty(0).toString());
                    ap.set_U0217F2(ic.getProperty(1).toString());
                    ap.set_U0217F3(ic.getProperty(2).toString());
                    ap.set_U0217F4(Integer.parseInt(ic.getProperty(3).toString()));
                    ap.set_U0217F5(ic.getProperty(4).toString());
                    ap.set_U0217F6(ic.getProperty(5).toString());
                    ap.set_U0217F7(Arrays.equals(Base64.decode(ic.getProperty(6).toString().getBytes(),Base64.DEFAULT), arrayVacio) ? null : Base64.decode(ic.getProperty(6).toString().getBytes(),Base64.DEFAULT));
                    ap.set_U0217F8(ic.getProperty(7).toString().equals("anyType{}") ? null : ic.getProperty(7).toString());
                    ap.set_U0217F9(ic.getProperty(8).toString());

                    listaAntecedentes.add(ap);
                }

            }catch (IOException e) {//no encuentra respuesta (No existe o no Existe la direccion a webservice).
                //e.printStackTrace();
                cancel(true);
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                cancel(true);
            }
            return listaAntecedentes;
        }


        @Override
        protected void onCancelled(List<AntecedentesPersona> result){
            Log.e(TAG, "Se ha cancelado llamado a Ants_Verificacion");
            //Notificacion("No ha sido posible obtener los datos de la persona");
        }

        @Override
        protected void onPostExecute(List<AntecedentesPersona> resultado) {
            Log.i(TAG, "Ingreso a POST de llamado Ants_Verificacion");

            global.decrementThread();

            if(global.getNumThreads() == 0){
                Log.i(TAG, "Ultimo hilo: Ants_Verificacion_NOHuellaFoto");
                if(miBarra != null){
                    miBarra.setVisibility(View.INVISIBLE);
                }
                if(origen instanceof MainActivity){//para actualizar la LDV.
                    //envio a siguiente bloque logico.
                    ((MainActivity)origen).ActualizarLDV_TerminoHilo(resultado);

                }else if(origen instanceof MenuAdminActivity){// Actualizacion LDV desde interfaz de adminitracion.
                    Log.i(TAG, "Retorno a MenuAdminActivity");
                    ((MenuAdminActivity) origen).ProcesoActualizacionLDV_TerminoHilo(resultado);
                }
            }
        }

    }

    private class CallWSAnts_VerificacionNoFotoHuella extends AsyncTask<String, Void, List<AntecedentesPersona>> {
        List<AntecedentesPersona> listaAntecedentes;
        String nombreFuncion="U029175";

        @Override
        protected List<AntecedentesPersona> doInBackground(String... params){
            Log.i(TAG, "Entro al hilo de Antecedentes de verifiacion");

            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
            headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).

            SoapObject request = new SoapObject(NAMESPACE, nombreFuncion);
            request.addProperty("pstrId_Credencial", params[0]);
            request.addProperty("pstrPortico", params[1]);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);
            try{
                transporte.call(NAMESPACE + nombreFuncion, envelope, headers);
                SoapObject resSoap = (SoapObject) envelope.getResponse();

                listaAntecedentes = new ArrayList<>();
                for(int i= 0; i<resSoap.getPropertyCount(); i++){
                    SoapObject ic = (SoapObject) resSoap.getProperty(i);
                    AntecedentesPersona ap = new AntecedentesPersona();
                    ap.set_U0217F1(ic.getProperty(0).toString());
                    ap.set_U0217F2(ic.getProperty(1).toString());
                    ap.set_U0217F3(ic.getProperty(2).toString());
                    ap.set_U0217F4(Integer.parseInt(ic.getProperty(3).toString()));
                    ap.set_U0217F5(ic.getProperty(4).toString());
                    ap.set_U0217F6(ic.getProperty(5).toString());
                    ap.set_U0217F8(ic.getProperty(6).toString().equals("anyType{}") ? null : ic.getProperty(6).toString());
                    ap.set_U0217F9(ic.getProperty(7).toString());

                    listaAntecedentes.add(ap);
                }

            }catch (IOException e) {//no encuentra respuesta (No existe o no Existe la direccion a webservice).
                //e.printStackTrace();
                cancel(true);
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                cancel(true);
            }
            return listaAntecedentes;
        }

        @Override
        protected void onCancelled(List<AntecedentesPersona> result){
            Log.e(TAG, "Se ha cancelado llamado a Ants_Verificacion");

            if(origen instanceof EventsActivity){
                Notificacion("No ha sido posible obtener los datos de la persona");
            }else if(origen instanceof MenuAdminActivity){
                Notificacion("No ha sido posible obtener los antecedentes de la persona");
            }


        }

        @Override
        protected void onPostExecute(List<AntecedentesPersona> resultado) {
            Log.i(TAG, "Ingreso a POST de llamado Ants_Verificacion");

            global.decrementThread();

            if(global.getNumThreads() == 0){
                Log.i(TAG, "Ultimo hilo: Ants_Verificacion_NOHuellaFoto");
                if(miBarra != null){
                    miBarra.setVisibility(View.INVISIBLE);
                }
                if(origen instanceof EventsActivity){//se que sera para despliegue de datos de la persona.
                    Log.i(TAG, "retorno a EventsActivity");

                    //envio a siguiente bloque logico.
                    ((EventsActivity)origen).DespliegueDatos(resultado);

                }
            }
        }

    }

    private class CallWSAnts_Veri_Foto extends AsyncTask<String, Void, byte[]> {
        byte[] data = null;
        String nombreFuncion="U029176";

        @Override
        protected byte[] doInBackground(String... params){
            Log.i(TAG, "Entro al hilo de Antecedentes de verifiacion - Fotografia");

            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
            headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).

            SoapObject request = new SoapObject(NAMESPACE, nombreFuncion);
            request.addProperty("pstrId_Credencial", params[0]);
            request.addProperty("pstrPortico", params[1]);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);
            try{
                transporte.call(NAMESPACE + nombreFuncion, envelope, headers);
                SoapPrimitive resSoap = (SoapPrimitive) envelope.getResponse();

                if(resSoap != null){
                    //data = (resSoap.toString().getBytes());
                    data = Base64.decode(resSoap.getValue().toString().getBytes(),Base64.DEFAULT);

                    //dd = new String(data,"UTF-8");
                }else{ // En el caso de que no encontrara informacion... (crede no verificada, persona sin foto asignada).
                    data=null;
                    cancel(true);
                }

            }catch (IOException e) {//no encuentra respuesta (No existe o no Existe la direccion a webservice).
                //e.printStackTrace();
                data = null;
                cancel(true);
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                data = null;
                cancel(true);
            }
            return data;
        }

        @Override
        protected void onCancelled(byte[] result){
            Log.e(TAG, "Se ha cancelado llamado a Ants_Verificacion - Fotografia");
            //Notificacion("No ha sido posible obtener los datos de la persona");
            if(origen instanceof EventsActivity){
                ((EventsActivity)origen).AsignacionFotografia(result);
            }
        }

        @Override
        protected void onPostExecute(byte[] resultado) {
            Log.i(TAG, "Ingreso a POST de llamado Ants_Verificacion - Fotografia");

            global.decrementThread();

            if(global.getNumThreads() == 0){
                Log.i(TAG, "Ultimo hilo: Ants_Verificacion - Fotografia");
                if(miBarra != null){
                    miBarra.setVisibility(View.INVISIBLE);
                }
                if(origen instanceof EventsActivity){//se que sera para despliegue de datos de la persona.
                    Log.i(TAG, "retorno a EventsActivity");

                    //Se asigna a foto a la persona.
                    ((EventsActivity)origen).AsignacionFotografia(resultado);

                }
            }
        }

    }

    private class CallWSVerificar_Reingreso extends AsyncTask<String, Void, Boolean> {
        boolean result = false;
        String nombreFuncion = "U029178";

        @Override
        protected Boolean doInBackground(String... params) {
            Log.i(TAG, "Entro al hilo de Verificacion de reingreso");

            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
            headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).

            SoapObject request = new SoapObject(NAMESPACE, nombreFuncion);
            request.addProperty("_idCard", params[0]);
            request.addProperty("_typeEvent", params[1]);
            request.addProperty("_portico", params[2]);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);
            try{
                transporte.call(NAMESPACE + nombreFuncion, envelope, headers);
                SoapPrimitive resSoap = (SoapPrimitive) envelope.getResponse();

                if(resSoap != null){
                    result = Boolean.parseBoolean(resSoap.toString());

                }else{ // En el caso de que no encontrara informacion... (crede no verificada, persona sin foto asignada).
                    cancel(true);
                }

            }catch (IOException e) {//no encuentra respuesta (No existe o no Existe la direccion a webservice).
                //e.printStackTrace();
                cancel(true);
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                cancel(true);
            }
            return result;
        }

        @Override
        protected void onCancelled(Boolean resultado){
            Log.i(TAG, "Se ha cancelado el hilo de verificacion de reingreso");
            //Notificacion("No es posible verificar el reingreso de la marca"); no importa si no puede.
            ((EventsActivity)origen).VerificacionReingreso_TerminoHilo(false);
        }

        @Override
        protected void onPostExecute(Boolean resultado){
            Log.i(TAG, "Ingreso a POST de llamado Verificacion_reingreso");

            global.decrementThread();

            if(global.getNumThreads() == 0){
                Log.i(TAG, "Ultimo hilo: Verificacion_reingreso");
                if(miBarra != null){
                    miBarra.setVisibility(View.INVISIBLE);
                }
                if(origen instanceof EventsActivity){//se que sera para el reingreso antes del marcaje.
                    Log.i(TAG, "retorno a EventsActivity");

                    //Se asigna a foto a la persona.
                    ((EventsActivity)origen).VerificacionReingreso_TerminoHilo(resultado);

                }
            }
        }
    }

    private class CallWSInsertar_Marca extends AsyncTask<String, Integer, Boolean>{
        String nombreFuncion = "U029179";
        ProgressDialog progressDialog;


        //constructor para asignacion de progressDialog.
        public CallWSInsertar_Marca(ProgressDialog pd){
            this.progressDialog = pd;
        }

        @Override
        protected void onPreExecute(){
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Log.i(TAG, "Entro al hilo de Antecedentes de verifiacion");

            Boolean resultMarca = false;
            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
            headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).

            SoapObject request = new SoapObject(NAMESPACE, nombreFuncion);
            request.addProperty("_idCard", params[0]);
            request.addProperty("_typeEvent", params[1]);
            request.addProperty("_portico", params[2]);
            request.addProperty("_idVerificacion", params[3]);
            request.addProperty("_idPersona", params[4]);
            request.addProperty("_TypeCard", params[5]);
            request.addProperty("_levelAuth", params[6]);
            request.addProperty("_typeTransaccion", params[7]);
            request.addProperty("_stateTransaccion", params[8]);
            request.addProperty("_stateTransfer", params[9]);
            request.addProperty("_loc_geo", params[10]);
            request.addProperty("_originMark", params[11]);
            publishProgress(25);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);
            publishProgress(25);
            try{
                transporte.call(NAMESPACE + nombreFuncion, envelope, headers);
                publishProgress(45);
                SoapPrimitive resSoap = (SoapPrimitive) envelope.getResponse();
                resultMarca = Boolean.parseBoolean(resSoap.toString());
                if(!resultMarca){
                    cancel(true);
                }
                publishProgress(5);
            }catch (IOException e) {//no encuentra respuesta (No existe o no Existe la direccion a webservice).
                //e.printStackTrace();
                cancel(true);
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                cancel(true);
            }

            return resultMarca;
        }

        @Override
        protected void onProgressUpdate(Integer... porcentaje){
            progressDialog.setProgress(progressDialog.getProgress() + porcentaje[0]);
        }

        @Override
        protected void onPostExecute(Boolean result){
            Log.i(TAG, "Se ha realizado la marca a la BD");
            try{
                Thread.sleep(1000);
            }catch (Exception ex){
                Log.e(TAG, "Error en POstExecute de hilo de Ingreso de Marca");
            }
            progressDialog.dismiss();
            origen.setResult(Activity.RESULT_OK);
            origen.finish();

        }
        @Override
        protected void onCancelled(Boolean res){
            Log.e(TAG, "Se ha cancelado llamado a Ingresar_Marca");
            Notificacion("No ha sido posible obtener los datos de la persona");

        }
    }

    private class CallWSInsertar_Marca_Transito extends AsyncTask<String, Integer, Boolean> {
        String nombreFuncion = "U02917A";
        ProgressDialog progressDialog;

        //constructor para asignacion de progressDialog.
        public CallWSInsertar_Marca_Transito(ProgressDialog pd){
            this.progressDialog = pd;
        }

        @Override
        protected void onPreExecute(){
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Log.i(TAG, "Entro al hilo de Ingreso de Marcas de transito");

            Boolean resultMarca = false;
            List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
            headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).

            SoapObject request = new SoapObject(NAMESPACE, nombreFuncion);
            request.addProperty("_IdPortico", params[0]);
            request.addProperty("_IdCredencial", params[1]);
            request.addProperty("_IdPersona", params[2]);
            publishProgress(25);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL);
            publishProgress(25);
            try{
                transporte.call(NAMESPACE + nombreFuncion, envelope, headers);
                publishProgress(45);
                SoapPrimitive resSoap = (SoapPrimitive) envelope.getResponse();
                resultMarca = Boolean.parseBoolean(resSoap.toString());
                if(!resultMarca){
                    cancel(true);
                }
            }catch (IOException e) {//no encuentra respuesta (No existe o no Existe la direccion a webservice).
                //e.printStackTrace();
                cancel(true);
            } catch (XmlPullParserException e) {
                //e.printStackTrace();
                cancel(true);
            }
            publishProgress(5);
            return resultMarca;
        }

        @Override
        protected void onPostExecute(Boolean result){
            Log.i(TAG, "Se ha realizado la marca de transito a la BD");
            try{
                //sonido de marca realizada.
                Thread.sleep(5000);
            }catch (Exception ex){
                Log.e(TAG, "Error en POstExecute de hilo de Ingreso de Marca");
            }
            progressDialog.dismiss();
            origen.setResult(Activity.RESULT_OK);
            origen.finish();
        }

        @Override
        protected void onCancelled(Boolean res){
            Log.e(TAG, "Se ha cancelado llamado a Ingresar_Marca dde transito");
            Notificacion("No ha sido posible obtener los datos de la persona");

        }

    }

    private class CallWSInsertar_Marca_Transito_Offline extends AsyncTask<Void, Integer, Integer>{
        List<RegistroTransito> registrosTransito = null;
        String nombreFuncion = "U02917B";


        public CallWSInsertar_Marca_Transito_Offline(List<RegistroTransito> list){
            registrosTransito = list;
        }

        @Override
        protected void onPreExecute(){
            miBarra.setIndeterminate(false);
            miBarra.setVisibility(View.VISIBLE);
            miBarra.setMax(registrosTransito.size());
            miBarra.setProgress(0);

        }
        @Override
        protected Integer doInBackground(Void... params) {
            int resultDelete =-1;
            try{
                List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
                headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).
                for(int i=0; i<registrosTransito.size(); i++){
                    Boolean resultMarca = false;

                    SoapObject request = new SoapObject(NAMESPACE, nombreFuncion);
                    request.addProperty("_fechaHora", registrosTransito.get(i).get_U0272F5());
                    request.addProperty("_IdPortico", registrosTransito.get(i).get_U0272F6());
                    request.addProperty("_IdCredencial", registrosTransito.get(i).get_U0272F7());
                    request.addProperty("_IdPersona", registrosTransito.get(i).get_U0272F8());

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE transporte = new HttpTransportSE(URL);

                    transporte.call(NAMESPACE + nombreFuncion, envelope, headers);
                    SoapPrimitive resSoap = (SoapPrimitive) envelope.getResponse();
                    resultMarca = Boolean.parseBoolean(resSoap.toString());
                    if(!resultMarca){
                        cancel(true);
                        return -1;
                    }else{
                        //elimino el registro de la BD local.
                        resultDelete = Globals.getInstance().getDatabase_manager().EliminarTransito(registrosTransito.get(i).get_U0272F5(),
                                                                                        registrosTransito.get(i).get_U0272F6());
                        if(resultDelete < 0){
                            //no fue posible la eliminacion.
                           cancel(true);
                            return -1;
                        }
                        publishProgress(1);
                    }
                }
            }catch (Exception ex){
                cancel(true);
            }

            return resultDelete;
        }

        @Override
        protected void onPostExecute(Integer result){
            try{
                //Termino de transferencia.
                miBarra.setVisibility(View.INVISIBLE);
                Notificacion("Registros de historial de transito transferidos exitosamente");
                if(origen instanceof MenuAdminActivity){
                    ((MenuAdminActivity) origen).EstadoBotonera(true);
                }
            }catch (Exception ex){
                Log.e(TAG, "Error en PostExecute:: hilo de Transito offline");
            }
        }

        @Override
        protected void onCancelled(Integer result){
            Notificacion("Error al transferir los datos de transito");
            if(origen instanceof MenuAdminActivity){
                ((MenuAdminActivity) origen).EstadoBotonera(true);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress){
            miBarra.setProgress(miBarra.getProgress()+progress[0]);
        }
    }

    private class CallWSInsertar_Marca_Offline extends AsyncTask<Void, Integer, Integer>{
        List<RegistroMarca> registrosMarca = null;
        String nombreFuncion = "U02917C";

        public CallWSInsertar_Marca_Offline(List<RegistroMarca> listado){
            registrosMarca = listado;
        }

        @Override
        protected void onPreExecute(){
            miBarra.setIndeterminate(false);
            miBarra.setVisibility(View.VISIBLE);
            miBarra.setMax(registrosMarca.size());
            miBarra.setProgress(0);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int resultDelete =-1;
            try{
                List<HeaderProperty> headers = new ArrayList<HeaderProperty>();//lista que guarda la autenticacion.
                headers.add(new HeaderProperty("Authorization", "Basic dW1icmFsOjEyMzQ=")); //dW1icmFsOjEyMzQ= es encriptacion 64 de umbral:1234. (64 encode).
                for(int i=0; i<registrosMarca.size(); i++){
                    Boolean resultMarca = false;

                    SoapObject request = new SoapObject(NAMESPACE, nombreFuncion);
                    request.addProperty("_idCard", registrosMarca.get(i).get_U021DCD());
                    request.addProperty("_timePorticox", registrosMarca.get(i).get_U021DCE());
                    request.addProperty("_typeEvent", registrosMarca.get(i).get_U021DCF());
                    request.addProperty("_portico", registrosMarca.get(i).get_U021DD0());
                    request.addProperty("_idVerificacion", registrosMarca.get(i).get_U021DD1() == -1 ? null : registrosMarca.get(i).get_U021DD1());
                    request.addProperty("_idPersona", registrosMarca.get(i).get_U021DD2());
                    request.addProperty("_TypeCard", registrosMarca.get(i).get_U021DD3());
                    request.addProperty("_levelAuth", registrosMarca.get(i).get_U021DD4());
                    request.addProperty("_typeTransaccion", "Z0B99D0"); //Z0B99D0: Batch (fuera de linea)
                    request.addProperty("_stateTransaccion", registrosMarca.get(i).get_U021DD6());
                    request.addProperty("_stateTransfer", "Z0B99DC"); //Z0B99DC: Enviada
                    request.addProperty("_loc_geo", registrosMarca.get(i).get_U021DD8());
                    request.addProperty("_originMark", registrosMarca.get(i).get_U021DD9());

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE transporte = new HttpTransportSE(URL);

                    transporte.call(NAMESPACE + nombreFuncion, envelope, headers);
                    SoapPrimitive resSoap = (SoapPrimitive) envelope.getResponse();
                    resultMarca = Boolean.parseBoolean(resSoap.toString());
                    if(!resultMarca){
                        cancel(true);
                        return -1;
                    }else{
                        //elimino el registro de la BD local.
                        resultDelete = Globals.getInstance().getDatabase_manager().EliminarMarca(registrosMarca.get(i).get_U021DCD(),
                                                                                                 registrosMarca.get(i).get_U021DCE(),
                                                                                                 registrosMarca.get(i).get_U021DCF(),
                                                                                                 registrosMarca.get(i).get_U021DD0());
                        if(resultDelete < 0){
                            //no fue posible la eliminacion.
                            cancel(true);
                            return -1;
                        }
                        publishProgress(1);
                    }
                }
            }catch (Exception ex){
                cancel(true);
                return -1;
            }

            return resultDelete;
        }

        @Override
        protected void onProgressUpdate(Integer... status){
            miBarra.setProgress(miBarra.getProgress() + status[0]);
        }

        @Override
        protected void onCancelled(Integer result){
            Notificacion("Error al transferir los datos de marcaje");
            if(origen instanceof MenuAdminActivity){
                ((MenuAdminActivity) origen).EstadoBotonera(true);
            }
        }

        @Override
        protected void onPostExecute(Integer result){
            try{
                //Termino de transferencia.
                miBarra.setVisibility(View.INVISIBLE);
                Notificacion("Registros de marcas de asistencia transferidos exitosamente");
                if(origen instanceof MenuAdminActivity){
                    ((MenuAdminActivity) origen).EstadoBotonera(true);
                }
            }catch (Exception ex){
                Log.e(TAG, "Error en PostExecute:: hilo de ingreso de marcas offline");
            }
        }
    }

    private void Notificacion(String message){
        try{
            alertDialog.setTitle(message);
            alertDialog.show();
            if(miBarra != null){
                miBarra.setVisibility(View.INVISIBLE);
            }
        }catch (Exception ex){
            Log.e(TAG, "Error al enviar mensaje (U02916C -> Notification):: "+ex.getMessage());
        }

    }
}
