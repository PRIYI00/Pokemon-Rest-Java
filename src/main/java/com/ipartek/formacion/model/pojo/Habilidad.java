package com.ipartek.formacion.model.pojo;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

public class Habilidad {

	private int id;
	
	@NotNull(message="Este campo no puede estar vacio.")
	@NotBlank(message="Este campo no puede estar vacio.")
	@Size(min = 2, max = 50, message="El valor de este campo tiene que estar entre 2 y 50 caracteres.")
	private String habilidad;
	
	public Habilidad() {
		this.id = 0;
		this.habilidad = "";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getHabilidad() {
		return habilidad;
	}

	public void setHabilidad(String habilidad) {
		this.habilidad = habilidad;
	}

	@Override
	public String toString() {
		return "Habilidad [id=" + id + ", habilidad=" + habilidad + "]";
	}
	
}
