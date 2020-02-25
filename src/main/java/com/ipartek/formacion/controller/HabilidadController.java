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
import com.ipartek.formacion.model.HabilidadDAO;
import com.ipartek.formacion.model.pojo.Habilidad;
import com.ipartek.formacion.utils.ResponseMensaje;
import com.ipartek.formacion.utils.Utilidades;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

/**
 * Servlet implementation class HabilidadController
 */
@WebServlet("/api/habilidad/*")
public class HabilidadController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = Logger.getLogger(HabilidadController.class);
	private static HabilidadDAO daoHabilidad;
	private int idHabilidad;
	private String nombreHabilidad;
	private int statusCode;
	private Object responseBody;
	private String pathInfo;
	private ValidatorFactory factory;
	private Validator validator;

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		daoHabilidad = HabilidadDAO.getInstance();
		factory  = Validation.buildDefaultValidatorFactory();
		validator  = factory.getValidator();
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		daoHabilidad = null;
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
		
		responseBody = null;
		pathInfo = request.getPathInfo();
		
		try { 
			idHabilidad = Utilidades.obtenerId(pathInfo);
			super.service(request, response);  
		} catch (Exception e) {
			statusCode =  HttpServletResponse.SC_BAD_REQUEST;
			responseBody = e.getMessage();				
		} finally {	
			response.setStatus(statusCode);
			if (responseBody != null) {
				// response body
				PrintWriter out = response.getWriter();		               
				String jsonResponseBody = new Gson().toJson(responseBody); 
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
		
		nombreHabilidad = request.getParameter("nombre");
		
		LOG.debug("PathInfo = " + pathInfo );
		LOG.debug("Parametro Nombre = " + nombreHabilidad );
		
		if (idHabilidad != -1) {				
			detalle(idHabilidad);		
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
			Habilidad habilidad = gson.fromJson(reader, Habilidad.class);
			LOG.debug("Json convertido a Objeto: " + habilidad);
			
			// Validar Objeto con los Validators
			Set<ConstraintViolation<Habilidad>>  validacionesErrores = validator.validate(habilidad);
			if (validacionesErrores.isEmpty()) {
				Habilidad habilidadGuardar = null;
				// Modificar Habilidad
				if (idHabilidad != -1) {
					habilidadGuardar = daoHabilidad.update(idHabilidad, habilidad);
					statusCode =  HttpServletResponse.SC_OK;
				} else {
				// Crear Habilidad
					habilidadGuardar = daoHabilidad.create(habilidad);
					statusCode =  HttpServletResponse.SC_CREATED;
				}
				
				responseBody = habilidadGuardar;
			} else {
				statusCode =  HttpServletResponse.SC_BAD_REQUEST;				
				ResponseMensaje responseMensaje = new ResponseMensaje("Los Valores no son Correctos");
				ArrayList<String> errores = new ArrayList<String>();
				for (ConstraintViolation<Habilidad> error : validacionesErrores) {					 
					errores.add(error.getPropertyPath() + " " + error.getMessage());
				}				
				responseMensaje.setErrores(errores);				
				responseBody = responseMensaje;
			}
		} catch (MySQLIntegrityConstraintViolationException e) {	
			statusCode =  HttpServletResponse.SC_CONFLICT;
			responseBody = new ResponseMensaje("Nombre de la Habilidad esta repetido.");
		} catch (Exception e) {
			statusCode =  HttpServletResponse.SC_BAD_REQUEST;
			responseBody = new ResponseMensaje(e.getMessage());	
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
		LOG.debug("Delete Habilidad");
		
		try {
			Habilidad habilidadEliminar = daoHabilidad.delete(idHabilidad);
			statusCode =  HttpServletResponse.SC_OK;
			responseBody = habilidadEliminar;
		} catch (Exception e) {
			statusCode =  HttpServletResponse.SC_NOT_FOUND;
			responseBody = new ResponseMensaje(e.getMessage());		
		}
	}
	
	private void listar() {
		ArrayList<Habilidad> habilidades = (ArrayList<Habilidad>) daoHabilidad.getAll();
		responseBody = habilidades;
		if (habilidades.isEmpty()) {
			statusCode = HttpServletResponse.SC_NO_CONTENT;
		} else {
			statusCode = HttpServletResponse.SC_OK;
		}
	}
	
	private void detalle(int id) {
		responseBody = daoHabilidad.getById(id);
		if (responseBody != null) {
			statusCode = HttpServletResponse.SC_OK;
		} else {
			responseBody = null;
			statusCode = HttpServletResponse.SC_NOT_FOUND;
		}
	}

}
