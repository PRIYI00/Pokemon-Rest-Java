package com.ipartek.formacion.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.ipartek.formacion.model.PokemonDAO;
import com.ipartek.formacion.model.pojo.Pokemon;
import com.ipartek.formacion.utils.ResponseMensaje;
import com.ipartek.formacion.utils.Utilidades;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;


/**
 * Servlet implementation class PokemonController
 */
@WebServlet("/api/pokemon/*")
public class PokemonController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = Logger.getLogger(PokemonController.class);
	private static PokemonDAO daoPokemon;
	private int idPokemon;
	private String nombre;
	private int statusCode;
	private Object reponseBody;
	private String pathInfo;
	private ValidatorFactory factory;
	private Validator validator;

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		daoPokemon = PokemonDAO.getInstance();
		factory  = Validation.buildDefaultValidatorFactory();
		validator  = factory.getValidator();
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		daoPokemon = null;
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOG.debug(request.getMethod() + " " + request.getRequestURL());
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
		response.addHeader("Access-Control-Allow-Headers", "Content-Type");
		
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		
		reponseBody = null;
		pathInfo = request.getPathInfo();
		
		try { 
			idPokemon = Utilidades.obtenerId(pathInfo);
			super.service(request, response);  
		} catch (Exception e) {
			statusCode =  HttpServletResponse.SC_BAD_REQUEST;
			reponseBody = e.getMessage();				
		} finally {	
			response.setStatus(statusCode);
			if (reponseBody != null) {
				// response body
				PrintWriter out = response.getWriter();		               // out se encarga de poder escribir datos en el body
				String jsonResponseBody = new Gson().toJson(reponseBody);  // conversion de Java a Json
				out.print(jsonResponseBody.toString()); 	
				out.flush();  
			}	
		}	
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOG.trace("doGet");
		
		nombre = request.getParameter("nombre"); //TODO busqueda por Nombre Pokemon
		
		LOG.debug("PathInfo = " + pathInfo );
		LOG.debug("Parametro Nombre = " + nombre );
		
		if (idPokemon != -1) {				
			detalle(idPokemon);		
		} else {			
			listar();
		}	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// Convertir json del request body a Objeto
			BufferedReader reader = request.getReader();               
			Gson gson = new Gson();
			Pokemon pokemon = gson.fromJson(reader, Pokemon.class);
			LOG.debug("Json convertido a Objeto: " + pokemon);
			
			// Validar objeto
			Set<ConstraintViolation<Pokemon>>  validacionesErrores = validator.validate(pokemon);		
			if (validacionesErrores.isEmpty()) {
				Pokemon pokemonGuardar = null;
				// Modificar Pokemon
				if (idPokemon != -1) {
					pokemonGuardar = daoPokemon.updateConHabilidades(idPokemon, pokemon);
					statusCode =  HttpServletResponse.SC_OK;
				} else {
					pokemonGuardar = daoPokemon.create(pokemon);
					statusCode =  HttpServletResponse.SC_CREATED;
				}
				
				reponseBody = pokemonGuardar;
			} else {
				statusCode =  HttpServletResponse.SC_BAD_REQUEST;				
				ResponseMensaje responseMensaje = new ResponseMensaje("valores no correctos");
				ArrayList<String> errores = new ArrayList<String>();
				for (ConstraintViolation<Pokemon> error : validacionesErrores) {					 
					errores.add(error.getPropertyPath() + " " + error.getMessage());
				}				
				responseMensaje.setErrores(errores);				
				reponseBody = responseMensaje;
			}	
		} catch (MySQLIntegrityConstraintViolationException e) {	
			statusCode =  HttpServletResponse.SC_CONFLICT;
			reponseBody = new ResponseMensaje("Nombre del Pokemon esta repetido.");
		} catch (Exception e) {
			statusCode =  HttpServletResponse.SC_BAD_REQUEST;
			reponseBody = new ResponseMensaje(e.getMessage());		
		}
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("Delete Pokemon");
		
		try {	
			Pokemon pEliminado = daoPokemon.delete(idPokemon);
			statusCode =  HttpServletResponse.SC_OK;
			reponseBody = pEliminado;
		} catch (Exception e) {
			statusCode =  HttpServletResponse.SC_NOT_FOUND;
			reponseBody = new ResponseMensaje(e.getMessage());		
		}
	}
	
	private void listar() {
		ArrayList<Pokemon> pokemons = (ArrayList<Pokemon>) daoPokemon.getAll();
		reponseBody = pokemons;
		if (pokemons.isEmpty()) {					
			statusCode = HttpServletResponse.SC_NO_CONTENT;
		} else {
			statusCode = HttpServletResponse.SC_OK;
		}
	}
	
	private void detalle(int id) {
		reponseBody = daoPokemon.getById(id);
		if (reponseBody != null) {
			statusCode = HttpServletResponse.SC_OK;
		} else {
			reponseBody = null;
			statusCode = HttpServletResponse.SC_NOT_FOUND;
		}
	}
	
	/*private void busquedaNombre(String cadena) {
		ArrayList<Pokemon> pokemons = (ArrayList<Pokemon>) daoPokemon.getByNombre(cadena);
		reponseBody = pokemons;
		if (pokemons.isEmpty()) {					
			statusCode = HttpServletResponse.SC_NO_CONTENT;
		} else {
			statusCode = HttpServletResponse.SC_OK;
		}
	}*/

}
