package mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import debug.Debug;
import experiment.sample.Sample;

public class Mysql {
	final Connection conn_id;

	public Mysql(String address, String user, String password, String database)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		this(address, "3306", user, password, database, "mysql",
				"com.mysql.jdbc.Driver");
	}

	public Mysql(String address, String port, String user, String password,
			String database, String driverName, String driverClassName)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		Class.forName(driverClassName).newInstance();

		String connectionUrl = "jdbc:" + driverName + "://" + address + ":"
				+ port + "/" + database
				+ "?useUnicode=true&characterEncoding=utf-8";
		conn_id = DriverManager.getConnection(connectionUrl, user, password);
	}

	public PreparedStatement prepareStatement(String query)
			throws SQLException {
		return conn_id.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
	}

	public PreparedStatement parametrizedPreparedStatement(String query,
			Object... args) throws SQLException {
		PreparedStatement stmt = prepareStatement(query);
		Debug.println(query);
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg == null) {
				stmt.setNull((i + 1), java.sql.Types.NULL);
			}
			if (arg instanceof Number) {
				if (arg instanceof Integer) {
					stmt.setInt((i + 1), (int) arg);
				} else if (arg instanceof Double) {
					stmt.setDouble((i + 1), (double) arg);
				} else if (arg instanceof Float) {
					stmt.setFloat((i + 1), (float) arg);
				}
			} else if (arg instanceof Date) {
				stmt.setDate((i + 1), (Date) arg);
			} else if (arg instanceof String) {
				stmt.setString((i + 1), arg.toString());
			} else {
				stmt.setObject((i + 1), arg);
			}
		}
		return stmt;
	}

	public ResultSet query(String prepString, Object... args) {
		try {
			return parametrizedPreparedStatement(prepString, args)
					.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ResultSet queryUpdate(String query, Object... args) {
		try {
			PreparedStatement stmt = parametrizedPreparedStatement(query, args);
			stmt.executeUpdate();
			stmt.getGeneratedKeys();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static void showResultSet(ResultSet set) throws SQLException {
		final int columnCount = set.getMetaData().getColumnCount();
		int[] displaySize = new int[columnCount];

		for (int i = 1; i <= columnCount; i++) {
			displaySize[i - 1] = set.getMetaData().getColumnDisplaySize(i);
			if (displaySize[i - 1] > 32) {
				displaySize[i - 1] = 32;
			}
			System.out.printf("%" + displaySize[i - 1] + "s|", set.getMetaData()
					.getColumnLabel(i));
		}
		System.out.println();

		while (set.next()) {
			for (int i = 1; i <= columnCount; i++) {
				System.out.printf("%" + displaySize[i - 1] + "s|", set
						.getString(i));
			}
			System.out.println();
		}
	}

	public static void main(String[] args) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException,
			IOException {

		ExperimentUploader eu = new ExperimentUploader();
		Sample s = new Sample();
		s.setName("Михаил");
		s.setComment("Черноскутов");
		s.setLength(0.001515);

		System.out.println(eu.sampleExists(s));
	}
}
