package com.ipartek.formacion.utils;

import java.util.ArrayList;

public class ResponseMensaje {

	private String texto;
	private ArrayList<String> errores;
	
	public ResponseMensaje() {
		super();
		this.texto = "";
		this.errores = new ArrayList<String>();
	}
	
	public ResponseMensaje(String mensaje) {
		this();
		this.texto = mensaje;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public ArrayList<String> getErrores() {
		return errores;
	}

	public void setErrores(ArrayList<String> errores) {
		this.errores = errores;
	}

	@Override
	public String toString() {
		return "ResponseMensaje [texto=" + texto + ", errores=" + errores + "]";
	}
	
}
