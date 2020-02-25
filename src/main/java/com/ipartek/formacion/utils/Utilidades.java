package com.ipartek.formacion.utils;

public class Utilidades {
	/**
	 * Obtenemos el Id del PathInfo o  Uri.
	 * @param pathInfo Parte de la Uri donde debemos buscar un numero.
	 * @return Numero Id.
	 * @throws Exception Si el PathInfo esta mal formado.
	 * 
	 * Ejemplos:
	 * <ol>
	 * 		<li> / pathInfo Valido </li>
	 * 		<li> /2 pathInfo Valido </li>
	 * 		<li> /2/ pathInfo Valido </li>
	 * 		<li> /2/2/ url pathInfo esta mal Formado </li>
	 * 		<li> /2/otracosa/34/ pathInfo esta mal Formado </li>
	 * </ol>
	 */
	public static int obtenerId(String pathInfo) throws Exception {
		int numeroID = -1;
		if (pathInfo != null) {
			try {
				String[] partes = pathInfo.split("/");
				if (partes.length == 2) {
					numeroID = Integer.parseInt(partes[1]);
				} else if (partes.length > 2){
					throw new Exception("La URL esta mal formada" + pathInfo);
				}
			} catch (NumberFormatException nfe) {
				throw new Exception("URL mal formada porque no es numerico " + pathInfo);
			}
		}	
		return numeroID;
	}
}
