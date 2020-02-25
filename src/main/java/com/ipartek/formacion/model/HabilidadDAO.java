package com.ipartek.formacion.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ipartek.formacion.model.pojo.Habilidad;

public class HabilidadDAO implements IDAO<Habilidad> {
	
	private final static Logger LOG = Logger.getLogger(HabilidadDAO.class);
	
	private static HabilidadDAO INSTANCE;
	

	private HabilidadDAO() {
		super();
	}

	public static synchronized HabilidadDAO getInstance() {

		if (INSTANCE == null) {
			INSTANCE = new HabilidadDAO();
		}

		return INSTANCE;
	}

	@Override
	public List<Habilidad> getAll() {
		String sql = "SELECT id_habilidad, nombre_habilidad FROM habilidades ORDER BY id_habilidad ASC LIMIT 500;";
		LOG.debug(sql);
		
		ArrayList<Habilidad> lista = new ArrayList<Habilidad>();
		
		try (Connection con = ConnectionManager.getConnection();
				PreparedStatement pst = con.prepareStatement(sql);) {
			LOG.debug(pst);
			try(ResultSet rs = pst.executeQuery()){
				while (rs.next()) {
					lista.add(mapper(rs));
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

		return lista;
	}

	@Override
	public Habilidad getById(int id) {
		LOG.trace("Recuperar Habilidad por id " + id);
		Habilidad habilidad = new Habilidad();
		
		String sql = "SELECT id_habilidad, nombre_habilidad FROM habilidades WHERE id_habilidad = ? "
					+ "ORDER BY id_habilidad ASC LIMIT 500;";
		
		LOG.debug(sql);
		
		try (Connection con = ConnectionManager.getConnection();
				PreparedStatement pst = con.prepareStatement(sql);) {
			pst.setInt(1, id);
			LOG.debug(pst);
			try(ResultSet rs = pst.executeQuery()){
				while (rs.next()) {
					habilidad = mapper(rs);
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		
		return habilidad;
	}

	@Override
	public Habilidad delete(int id) throws Exception {
		String sql = "DELETE FROM habilidades WHERE id_habilidad = ?;";
		LOG.debug(sql);
		
		Habilidad registro = null;
		
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
	public Habilidad update(int id, Habilidad pojo) throws Exception {
		String sql = "UPDATE habilidades SET nombre_habilidad = ? WHERE id_habilidad = ?;";
		LOG.debug(sql);
		
		try (Connection con = ConnectionManager.getConnection();
				PreparedStatement pst = con.prepareStatement(sql);) {
			pst.setString(1, pojo.getHabilidad());
			pst.setInt(2, id);
			
			LOG.debug(pst);

			int affectedRows = pst.executeUpdate(); // Lanza una excepcion si la Habilidad esta repetido
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

	@Override
	public Habilidad create(Habilidad pojo) throws Exception {
		String sql = "INSERT INTO habilidades (nombre_habilidad) VALUES (?);";
		LOG.debug(sql);
		
		try (Connection con = ConnectionManager.getConnection();
				PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
			pst.setString(1, pojo.getHabilidad());
			
			LOG.debug(pst);
			
			int affectedRows = pst.executeUpdate();
			if (affectedRows == 1) {
				// conseguimos el ID que acabamos de crear
				ResultSet rs = pst.getGeneratedKeys();
				if (rs.next()) {
					pojo.setId(rs.getInt(1));
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		
		return pojo;
	}
	
	private Habilidad mapper(ResultSet rs) throws SQLException {
		Habilidad habilidad = new Habilidad();
		
		habilidad.setId(rs.getInt("id_habilidad"));
		habilidad.setHabilidad(rs.getString("nombre_habilidad"));
		
		return habilidad;
	}

}
