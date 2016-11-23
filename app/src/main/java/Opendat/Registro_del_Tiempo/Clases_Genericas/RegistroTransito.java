package Opendat.Registro_del_Tiempo.Clases_Genericas;

import java.text.DateFormat;
import java.util.Date;

/**
 * Clase generica utilizada para almacenar los datos de los registros del historial de transito en la base de datos local.
 *
 * Modificaciones:
 * Fecha                    Autor                   Descripcion
 * -------------------------------------------------------------------------------------------------
 * 17.11.2016               Jonathan Vasquez        Creacion de Clase
 */

public class RegistroTransito {
    private String _U0272F5;
    private String _U0272F6;
    private String _U0272F7;
    private String _U0272F8;

    public RegistroTransito(){

    }

    public RegistroTransito(String fecha_hora, String id_portico, String id_credencial, String id_persona){
        this._U0272F5 = fecha_hora;
        this._U0272F6 = id_portico;
        this._U0272F7 = id_credencial;
        this._U0272F8 = id_persona;
    }

    public String get_U0272F5() {
        return _U0272F5;
    }

    public void set_U0272F5(String _U0272F5) {
        this._U0272F5 = _U0272F5;
    }

    public String get_U0272F6() {
        return _U0272F6;
    }

    public void set_U0272F6(String _U0272F6) {
        this._U0272F6 = _U0272F6;
    }

    public String get_U0272F7() {
        return _U0272F7;
    }

    public void set_U0272F7(String _U0272F7) {
        this._U0272F7 = _U0272F7;
    }

    public String get_U0272F8() {
        return _U0272F8;
    }

    public void set_U0272F8(String _U0272F8) {
        this._U0272F8 = _U0272F8;
    }
}
