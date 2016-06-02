package ru.dolika.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Mysql {
	final Connection conn_id;
	final Statement stmt;

	public Mysql(String address, String user, String password, String database)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		this(address, "3306", user, password, database, "mysql", "com.mysql.jdbc.Driver");
	}

	public Mysql(String address, String port, String user, String password, String database, String driverName,
			String driverClassName)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Class.forName(driverClassName).newInstance();

		String connectionUrl = "jdbc:" + driverName + "://" + address + ":" + port + "/" + database;
		conn_id = DriverManager.getConnection(connectionUrl, user, password);
		stmt = conn_id.createStatement();
	}

	public ResultSet query(String query) {
		try {
			return stmt.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
