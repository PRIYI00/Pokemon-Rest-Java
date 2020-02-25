package com.ipartek.formacion.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.ipartek.formacion.model.pojo.Habilidad;
import com.ipartek.formacion.model.pojo.Pokemon;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class PokemonDAO implements IDAO<Pokemon>{
	
	private static PokemonDAO INSTANCE;
	private final static Logger LOG = Logger.getLogger(PokemonDAO.class);
	
	private PokemonDAO() {
		super();
	}
	
	public static synchronized PokemonDAO getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PokemonDAO();
		}
		return INSTANCE;
	}

	@Override
	public List<Pokemon> getAll() {
		String sql = "SELECT "
				+ "p.id_pokemon, p.pokemon, p.imagen, h.id_habilidad, h.nombre_habilidad "
				+ "FROM pokemons p LEFT JOIN po_ha ph ON p.id_pokemon = ph.id_pokemon "
				+ "LEFT JOIN habilidades h ON ph.id_habilidad = h.id_habilidad "
				+ "ORDER BY p.id_pokemon ASC LIMIT 500;";
		
		LOG.debug(sql);

		HashMap<Integer, Pokemon> hm = new HashMap<Integer, Pokemon>();
		
		try (Connection con = ConnectionManager.getConnection();
				PreparedStatement pst = con.prepareStatement(sql);
				ResultSet rs = pst.executeQuery()) {
			while(rs.next()) {
				int idPokemon = rs.getInt("id_pokemon");
				Pokemon pokemon = hm.get(idPokemon);
				if (pokemon == null) {
					pokemon = new Pokemon();
					pokemon.setId(idPokemon);
					pokemon.setNombre(rs.getString("pokemon"));
					pokemon.setImagen(rs.getString("imagen"));
				}
				Habilidad habilidad = new Habilidad();
				habilidad.setId(rs.getInt("id_habilidad"));
				habilidad.setHabilidad(rs.getString("nombre_habilidad"));
				
				pokemon.getHabilidades().add(habilidad);
				
				hm.put(idPokemon, pokemon);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return new ArrayList<Pokemon>(hm.values());
	}

	@Override
	public Pokemon getById(int id) {
		LOG.trace("Recuperar Pokemon por id " + id);
		Pokemon pokemon = new Pokemon();
		HashMap<Integer, Pokemon> hm = new HashMap<Integer, Pokemon>();
		
		String sql = "SELECT "
				+ "p.id_pokemon, p.pokemon, p.imagen, h.id_habilidad, h.nombre_habilidad "
				+ "FROM pokemons p, habilidades h, po_ha "
				+ "WHERE p.id_pokemon = po_ha.id_pokemon AND "
				+ "po_ha.id_habilidad = h.id_habilidad AND p.id_pokemon = ? "
				+ "ORDER BY p.id_pokemon ASC LIMIT 500;";
		
		LOG.debug(sql);
		
		try (Connection con = ConnectionManager.getConnection();
				PreparedStatement pst = con.prepareStatement(sql);) {
			pst.setInt(1, id);
			LOG.debug(pst);
			try(ResultSet rs = pst.executeQuery()){
				while (rs.next()) {
					pokemon = mapper(rs, hm);
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		
		return pokemon;
	}
	
	public ArrayList<Pokemon> getByNombre(String cadena) {
		String sql = "SELECT p.id_pokemon, p.pokemon, p.imagen, h.id_habilidad, h.nombre_habilidad "
				+ "FROM pokemons p, habilidades h, po_ha "
				+ "WHERE p.id_pokemon = po_ha.id_pokemon AND po_ha.id_habilidad = h.id_habilidad AND "
				+ "p.pokemon LIKE ? ORDER BY p.id_pokemon ASC LIMIT 500;";
		LOG.debug(sql);
		Pokemon pokemon = new Pokemon();
		HashMap<Integer, Pokemon> hm = new HashMap<Integer, Pokemon>();
		ArrayList<Pokemon> pokemons = new ArrayList<Pokemon>();
		
		try (Connection con = ConnectionManager.getConnection();
				PreparedStatement pst = con.prepareStatement(sql);) {
			pst.setString(1, cadena);
			LOG.debug(pst);
			try(ResultSet rs = pst.executeQuery()){
				while (rs.next()) {
					pokemon = mapper(rs, hm);
					pokemons.add(pokemon);
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		
		return pokemons;
	}

	@Override
	public Pokemon delete(int id) throws Exception {
		String sql = "DELETE FROM pokemons WHERE id_pokemon = ?;";
		LOG.debug(sql);
		
		Pokemon registro = null;
		
		try (Connection con = ConnectionManager.getConnection();
				PreparedStatement pst = con.prepareStatement(sql);) {
			pst.setInt(1, id);
			
			LOG.debug(pst);
			
			registro = this.getById(id);
			int affectedRows = pst.executeUpdate(); // Eliminar
			if (affectedRows != 1) {
				registro = null;
				throw new Exception("No se puede eliminar " + registro);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		
		return registro;
	}

	@Override
	public Pokemon update(int id, Pokemon pojo) throws Exception {
		String sql = "UPDATE pokemons SET pokemon = ? WHERE id_pokemon = ?;";
		LOG.debug(sql);
		
		try (Connection con = ConnectionManager.getConnection();
				PreparedStatement pst = con.prepareStatement(sql);) {
			pst.setString(1, pojo.getNombre());
			pst.setInt(2, id);
			
			LOG.debug(pst);

			int affectedRows = pst.executeUpdate(); // lanza una excepcion si nombre repetido
			if (affectedRows == 1) {
				pojo.setId(id);
			} else {
				throw new Exception("No se encontro registro para id =" + id);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		
		return pojo;
	}
	
	public Pokemon updateConHabilidades(int id, Pokemon pojo) throws SQLException {
		Pokemon resul = null;
		Pokemon pokemonOriginal = this.getById(id);
		
		String sqlUpdatePokemon = "UPDATE pokemons SET pokemon = ?, imagen = ? WHERE id_pokemon = ?;";
		String sqlInsertHabilidad = "INSERT INTO po_ha (id_pokemon,id_habilidad) VALUES (?,?);";
		String sqlDeleteHabilidad = "DELETE FROM po_ha WHERE id_pokemon = ? AND id_habilidad = ?;";
		
		LOG.debug("Modificar pokemon " + id + ". Datos a modificar -> " + pojo);
		
		Connection con = null;
		try {
			con = ConnectionManager.getConnection();
			con.setAutoCommit(false);
			PreparedStatement pstUpdatePokemon = con.prepareStatement(sqlUpdatePokemon);
			
			pstUpdatePokemon.setString(1, pojo.getNombre());
			pstUpdatePokemon.setString(2, pojo.getImagen());
			pstUpdatePokemon.setInt(3, id);
			
			LOG.debug(pstUpdatePokemon);
			
			int affectedRows = pstUpdatePokemon.executeUpdate();
			if (affectedRows == 1) {
				PreparedStatement psInsert = con.prepareStatement(sqlInsertHabilidad, Statement.RETURN_GENERATED_KEYS);
				PreparedStatement psDelete = con.prepareStatement(sqlDeleteHabilidad);
				
				ArrayList<Habilidad> habilidadesViejas = (ArrayList<Habilidad>) pokemonOriginal.getHabilidades();
				ArrayList<Habilidad> habilidadesNuevas = (ArrayList<Habilidad>) pojo.getHabilidades();
				ArrayList<Habilidad> habilidadesQuitar = new ArrayList<Habilidad>();
				ArrayList<Habilidad> habilidadesInsertar = new ArrayList<Habilidad>();
				
				for (Habilidad habilidadVieja : habilidadesViejas) {
					boolean encontrado = false;
					
					for (Habilidad habilidadNueva : habilidadesNuevas) {
						if(habilidadVieja.getId() == habilidadNueva.getId()) {
							encontrado = true;
						}
					}
			
					if(encontrado == false) {
						habilidadesQuitar.add(habilidadVieja);				
					}
				}
				
				for (Habilidad habilidadNueva : habilidadesNuevas) {
					boolean encontrado = false;
					
					for (Habilidad habilidadVieja : habilidadesViejas) {
						if(habilidadVieja.getId() == habilidadNueva.getId()) {
							encontrado = true;
						}
					}
					
					if(encontrado == false) {
						habilidadesInsertar.add(habilidadNueva);				
					}
				}
				
				for(Habilidad habilidadInsertar : habilidadesInsertar) {
					psInsert.setInt(1, pokemonOriginal.getId());
					psInsert.setInt(2, habilidadInsertar.getId());
					psInsert.addBatch();
				}
				
				for(Habilidad habilidadEliminar : habilidadesQuitar) {
					psDelete.setInt(1, pokemonOriginal.getId());
					psDelete.setInt(2, habilidadEliminar.getId());
					psDelete.addBatch();
				}
				
				psDelete.executeBatch();
				psInsert.executeBatch();
				
				//SI TODO FUNCIONA BIEN			
				con.commit();
			} else {
				throw new Exception("No se encontro registro para id =" + id);
			}
		} catch (MySQLIntegrityConstraintViolationException e) {
			con.rollback();
			LOG.error(e.getMessage());
			throw new MySQLIntegrityConstraintViolationException("Nombre Duplicado");
		} catch (Exception e) {
			con.rollback();
			LOG.error(e.getMessage());
		} finally {
			if (con != null) {
				con.close();
			}
		}
		
		return resul;
	}

	@Override
	public Pokemon create(Pokemon pojo) throws Exception {
		String sql = "INSERT INTO pokemons (pokemon, imagen) VALUES (?,?);";
		String sqlHabilidades = "INSERT INTO po_ha (id_pokemon, id_habilidad) VALUES (?,?);";
		
		LOG.debug(sql);
		LOG.debug(sqlHabilidades);
		
		Connection con = null;
		try {
			con = ConnectionManager.getConnection();
			con.setAutoCommit(false);
			PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			
			pst.setString(1, pojo.getNombre());
			pst.setString(2, pojo.getImagen());
			
			LOG.debug(pst);
			
			int affectedRows = pst.executeUpdate();
			if (affectedRows == 1) {
				// Conseguimos el ID que acabamos de crear
				ResultSet rs = pst.getGeneratedKeys();
				if (rs.next()) {
					pojo.setId(rs.getInt(1));
					
					ArrayList<Habilidad> habilidades = (ArrayList<Habilidad>) pojo.getHabilidades();
					for (Habilidad habilidad : habilidades) {
						
						LOG.debug(habilidad);
						
						PreparedStatement pstHabilidad = con.prepareStatement(sqlHabilidades);
						
						pstHabilidad.setInt(1, pojo.getId());
						pstHabilidad.setInt(2, habilidad.getId());
						
						LOG.debug(pstHabilidad);
						
						int affectedRowsHabilidad = pstHabilidad.executeUpdate();
						if (affectedRowsHabilidad == 1) {
							LOG.debug("Se ha creado con Exito");
						} else {
							throw new Exception("No se ha creado el Registro");
						}
						pstHabilidad.close();
					} // For
				}
			} // If affectedRows == 1
			
			// Si todo Funciona
			con.commit();
		} catch (Exception e) {
			con.rollback();
			LOG.error(e.getMessage());
			throw new Exception(e.getMessage());
		} finally {
			if (con != null) {
				con.close();
			}
		}
		return pojo;
	}
	
	private Pokemon mapper(ResultSet rs, HashMap<Integer,Pokemon> hm) throws SQLException {
		int idPokemon = rs.getInt("id_pokemon");
		Pokemon pokemon = hm.get(idPokemon);
		if (pokemon == null) {
			pokemon = new Pokemon();
			pokemon.setId(idPokemon);
			pokemon.setNombre(rs.getString("pokemon"));
			pokemon.setImagen(rs.getString("imagen"));
		}
		Habilidad habilidad = new Habilidad();
		habilidad.setId(rs.getInt("id_habilidad"));
		habilidad.setHabilidad(rs.getString("nombre_habilidad"));
		
		pokemon.getHabilidades().add(habilidad);
		
		hm.put(idPokemon, pokemon);
		
		return pokemon;
	}

}
