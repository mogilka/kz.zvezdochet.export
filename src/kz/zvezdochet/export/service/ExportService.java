package kz.zvezdochet.export.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kz.zvezdochet.bean.Element;
import kz.zvezdochet.core.service.DataAccessException;
import kz.zvezdochet.core.tool.Connector;

/**
 * Класс, обеспечивающий взаимодействие с БД
 * @author Nataly Didenko
 */
public class ExportService { 

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
