package kz.zvezdochet.export.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kz.zvezdochet.bean.Element;
import kz.zvezdochet.bean.Planet;
import kz.zvezdochet.bean.Sign;
import kz.zvezdochet.core.service.DataAccessException;
import kz.zvezdochet.core.tool.Connector;

/**
 * Класс, обеспечивающий взаимодействие с БД
 * @author Nataly Didenko
 */
public class ExportService { 

	public List<Object> getPlanetInSignText(Planet planet, Sign sign) throws DataAccessException {
/*
select categories.Name, categories.Code, 
planetsigns.Text, textgender.Male, textgender.Female, textgender.child
from planetsigns 
inner join categories on planetsigns.TypeID = categories.id
left join textgender on planetsigns.GenderID = textgender.id
where planetsigns.SignID = 6
and categories.ObjectID = 23
order by categories.Priority
 */
        List<Object> list = new ArrayList<Object>();
        PreparedStatement ps = null;
        ResultSet rs = null;
		String sql;
		try {
			sql =	
				"select categories.name, categories.code, " +
					"planetsigns.text, textgender.male, textgender.female " +
				"from planetsigns " +
					"inner join categories on planetsigns.typeid = categories.id) " +
					"left join textgender on planetsigns.genderid = textgender.id) " +
				"where planetsigns.signid = '" + sign.getId() + "' " +
					"and categories.objectid = '" + planet.getId() + "' " +
				"order by categories.priority";
			ps = Connector.getInstance().getConnection().prepareStatement(sql);
			rs = ps.executeQuery();
			int columns = rs.getMetaData().getColumnCount();
			while (rs.next()) { 
		        List<Object> sublist = new ArrayList<Object>();
				for (int i = 0; i < columns; i++)  
			        sublist.add(rs.getString(i + 1));
				list.add(sublist);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { 
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException e) { 
				e.printStackTrace(); 
			}
		}
		return list;
	}

	//TODO продумать метод
	public Element getElementByGroup(String[] codes) throws DataAccessException {
        Element element = new Element();
        PreparedStatement ps = null;
        ResultSet rs = null;
		String sql;
		try {
			sql =	
				"select Elements.*, TextGender.Male, TextGender.Female " +
	            "from Elements " +
	            "left join TextGender on Elements.GenderID = TextGender.ID " +
	            "where Elements.Name like '" + codes[0] + "' " +
				"order by Types.Priority)"; //TODO проверить синтаксис
			ps = Connector.getInstance().getConnection().prepareStatement(sql);
			rs = ps.executeQuery();
			int columns = rs.getMetaData().getColumnCount();
			while (rs.next()) { 
		        List<Object> sublist = new ArrayList<Object>();
				for (int i = 0; i < columns; i++)  
			        sublist.add(rs.getString(i));
				//list.add(sublist);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { 
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException e) { 
				e.printStackTrace(); 
			}
		}
		return element;
	}
}
