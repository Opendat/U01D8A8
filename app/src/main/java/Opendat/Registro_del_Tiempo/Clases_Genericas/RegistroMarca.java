package Opendat.Registro_del_Tiempo.Clases_Genericas;

/**
 * Clase generica utilizada para obtener los datos de los registros de las marcas de asistencia en la base de datos local.
 *
 * Nota: No se considera los atributos U021DD5 (tipo_transaccion) ni U021DD7 (estado_tranferencia).
 *
 * Modificaciones:
 * Fecha                    Autor                   Descripcion
 * -------------------------------------------------------------------------------------------------
 * 22.11.2016               Jonathan Vasquez        Creacion de Clase
 */

public class RegistroMarca {
    private String _U021DCD; // Id Credencial
    private String _U021DCE; // Fecha hora
    private String _U021DCF; // Tipo de Evento
    private String _U021DD0; // Id Portico
    private int _U021DD1;    // Id Verificacion
    private String _U021DD2; // Id Persona
    private String _U021DD3; // Tipo Credencial
    private String _U021DD4; // Nivel Autenticacion
    private String _U021DD6; // Estado Transaccion
    private String _U021DD8; // Georeferencia
    private String _U021DD9; //Origen de la marca

    public RegistroMarca(){
    }
    public RegistroMarca(String u021dcd, String u021dce, String u021dcf, String u021dd0, int u021dd1, String u021dd2,
                         String u021dd3, String u021dd4, String u021dd6, String u021dd8, String u021dd9){
        this.set_U021DCD(u021dcd);
        this.set_U021DCE(u021dce);
        this.set_U021DCF(u021dcf);
        this.set_U021DD0(u021dd0);
        this.set_U021DD1(u021dd1);
        this.set_U021DD2(u021dd2);
        this.set_U021DD3(u021dd3);
        this.set_U021DD4(u021dd4);
        this.set_U021DD6(u021dd6);
        this.set_U021DD8(u021dd8);
        this.set_U021DD9(u021dd9);
    }

    public String get_U021DCD() {
        return _U021DCD;
    }

    public void set_U021DCD(String _U021DCD) {
        this._U021DCD = _U021DCD;
    }

    public String get_U021DCE() {
        return _U021DCE;
    }

    public void set_U021DCE(String _U021DCE) {
        this._U021DCE = _U021DCE;
    }

    public String get_U021DCF() {
        return _U021DCF;
    }

    public void set_U021DCF(String _U021DCF) {
        this._U021DCF = _U021DCF;
    }

    public String get_U021DD0() {
        return _U021DD0;
    }

    public void set_U021DD0(String _U021DD0) {
        this._U021DD0 = _U021DD0;
    }

    public int get_U021DD1() {
        return _U021DD1;
    }

    public void set_U021DD1(int _U021DD1) {
        this._U021DD1 = _U021DD1;
    }

    public String get_U021DD2() {
        return _U021DD2;
    }

    public void set_U021DD2(String _U021DD2) {
        this._U021DD2 = _U021DD2;
    }

    public String get_U021DD3() {
        return _U021DD3;
    }

    public void set_U021DD3(String _U021DD3) {
        this._U021DD3 = _U021DD3;
    }

    public String get_U021DD4() {
        return _U021DD4;
    }

    public void set_U021DD4(String _U021DD4) {
        this._U021DD4 = _U021DD4;
    }

    public String get_U021DD6() {
        return _U021DD6;
    }

    public void set_U021DD6(String _U021DD6) {
        this._U021DD6 = _U021DD6;
    }

    public String get_U021DD8() {
        return _U021DD8;
    }

    public void set_U021DD8(String _U021DD8) {
        this._U021DD8 = _U021DD8;
    }

    public String get_U021DD9() {
        return _U021DD9;
    }

    public void set_U021DD9(String _U021DD9) {
        this._U021DD9 = _U021DD9;
    }
}
