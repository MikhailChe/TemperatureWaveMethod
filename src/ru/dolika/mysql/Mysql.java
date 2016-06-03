package ru.dolika.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import ru.dolika.debug.Debug;

public class Mysql {
	final Connection conn_id;

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
	}

	public ResultSet query(String query) {
		if (Debug.debug) {
			System.out.println(query);
		}
		try {
			Statement stmt = conn_id.createStatement();
			return stmt.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet queryf(String format, Object... args) {
		return query(String.format(Locale.ENGLISH, format, args));
	}

	public ResultSet queryUpdate(String query) {
		if (Debug.debug) {
			System.out.println(query);
		}
		try {
			Statement stmt = conn_id.createStatement();
			stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			return stmt.getGeneratedKeys();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ResultSet queryUpdatef(String format, Object... args) {

		return queryUpdate(String.format(Locale.ENGLISH, format, args));
	}
}
