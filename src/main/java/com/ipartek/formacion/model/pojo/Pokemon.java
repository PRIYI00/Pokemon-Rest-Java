package com.ipartek.formacion.model.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

public class Pokemon {
	
	private int id;
	
	@NotNull(message="Este campo no puede estar vacio.")
	@NotBlank(message="Este campo no puede estar vacio.")
	@Size(min = 2, max = 50, message="El valor de este campo tiene que estar entre 2 y 50 caracteres.")
	private String nombre;
	
	@NotNull(message="Este campo no puede estar vacio.")
	@NotBlank(message="Este campo no puede estar vacio.")
	@Size(min = 2, max = 250, message="El valor de este campo tiene que estar entre 2 y 250 caracteres.")
	private String imagen;
	
	private List<Habilidad> habilidades;
	
	public Pokemon() {
		this.id = 0;
		this.nombre = "";
		this.imagen = "";
		this.habilidades = new ArrayList<Habilidad>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getImagen() {
		return imagen;
	}

	public void setImagen(String imagen) {
		this.imagen = imagen;
	}

	public List<Habilidad> getHabilidades() {
		return habilidades;
	}

	public void setHabilidades(List<Habilidad> habilidades) {
		this.habilidades = habilidades;
	}

	@Override
	public String toString() {
		return "Pokemon [id=" + id + ", nombre=" + nombre + ", imagen=" + imagen + ", habilidades=" + habilidades + "]";
	}

}
