package Opendat.Registro_del_Tiempo.Clases_Genericas;

import android.content.Context;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.util.Log;

import com.civi.R;

/**
 * Clase generica utilizada para almacenar la informacion de los sonidos utilizados en los sistemas MRAT.
 *
 * Modificaciones:
 * Fecha                    Autor                   Descripcion
 * -------------------------------------------------------------------------------------------------
 * 30.11.2016               Jonathan Vasquez        Creacion de Clase
 */

public class Sonidos {
    private static final String TAG = "AppMRAT";

    MediaPlayer mp;
    
    public Sonidos(){

    }

    public void Reproducir_CredNV(Context context){
        try{
            this.mp = MediaPlayer.create(context, R.raw.reg_no_verifi); //credencial no verificada.
            mp.start();
        }catch (Exception ex){
            Log.e(TAG, "Error al reproducir el sonido 'Credencial no verificada'");
        }
    }

    public void Reproducir_IngreCorrec(Context context){
        try{
            this.mp = MediaPlayer.create(context, R.raw.reg_ok); //Ingreso correcto.
            mp.start();
        }catch (Exception ex){
            Log.e(TAG, "Error al reproducir el sonido 'Ingreso correcto'");
        }
    }

    public void Reproducir_IngrePendVerifi(Context context){
        try{
            this.mp = MediaPlayer.create(context, R.raw.reg_ok_no_verifi); //Ingreso pendiente de verificacion.
            mp.start();
        }catch (Exception ex){
            Log.e(TAG, "Error al reproducir el sonido 'Ingreso pendiente de verificacion'");
        }
    }

    public void Reproducir_SelecEvent(Context context){
        try{
            this.mp = MediaPlayer.create(context, R.raw.sel_event); //Seleccione tipo de evento.
            mp.start();
        }catch (Exception ex){
            Log.e(TAG, "Error al reproducir el sonido 'Seleccione tipo de evento'");
        }
    }

    public void Reproducir_IngrePIN(Context context){
        try{
            this.mp = MediaPlayer.create(context, R.raw.reg_pin); //Por favor, ingrese el PIN de 4 digitos.
            mp.start();
        }catch (Exception ex){
            Log.e(TAG, "Error al reproducir el sonido 'Por favor, ingrese el PIN de 4 digitos'");
        }
    }

    public void Reproducir_IngreHuella(Context context){
        try{
            this.mp = MediaPlayer.create(context, R.raw.reg_huella); //Por favor, coloque su dedo en el lector de huellas.
            mp.start();
        }catch (Exception ex){
            Log.e(TAG, "Error al reproducir el sonido 'Por favor, coloque su dedo en el lector de huellas'");
        }
    }

    public void Reproducir_ErrPIN(Context context){
        try{
            this.mp = MediaPlayer.create(context, R.raw.err_pin); //PIN incorrecto, intente nuevamente.
            mp.start();
        }catch (Exception ex){
            Log.e(TAG, "Error al reproducir el sonido 'PIN incorrecto, intente nuevamente'");
        }
    }

    public void Reproducir_ErrHuella(Context context){
        try{
            this.mp = MediaPlayer.create(context, R.raw.err_huella); //Se ha agotado el tiempo de registro, lectura cancelada.
            mp.start();
        }catch (Exception ex){
            Log.e(TAG, "Error al reproducir el sonido 'Se ha agotado el tiempo de registro, lectura cancelada'");
        }
    }

    public void Reproducir_IngreCancel(Context context){
        try{
            this.mp = MediaPlayer.create(context, R.raw.reg_cancel); //Ingreso cancelado.
            mp.start();
        }catch (Exception ex){
            Log.e(TAG, "Error al reproducir el sonido 'Ingreso cancelado'");
        }
    }
}
