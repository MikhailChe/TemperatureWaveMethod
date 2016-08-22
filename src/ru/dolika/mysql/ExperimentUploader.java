package ru.dolika.mysql;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import ru.dolika.experiment.measurement.Measurement;
import ru.dolika.experiment.sample.Sample;

public class ExperimentUploader {

	final static private String	sampleTableName		= "tp_samples";
	final static private String	measuresTableName	= "tp_measures";

	Mysql						mysql;

	public ExperimentUploader()
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, IOException {
		MySQLCredentials creds = new MySQLCredentials();
		mysql = new Mysql(creds.address, creds.username, creds.password,
				creds.database);
	}

	public void uploadExperimentResults(List<Measurement> measures, Sample s) {
		if (!dataStructureExists()) {
			createDataStructure();
		}
		if (!sampleExists(s)) {
			createSample(s);
		}
		Integer id = getSampleId(s).orElse(-1);

		measures.stream().forEach(m -> uploadMeasurement(m, id));

	}

	private Optional<Integer> getSampleId(Sample s) {
		Integer id = null;
		try {
			PreparedStatement stmt = mysql.conn_id
					.prepareStatement(
							"SELECT `id` " + "FROM `" + sampleTableName + "` "
									+ "WHERE `name` = '?'  AND `length` = '?'");
			stmt.setString(1, s.name);
			stmt.setDouble(2, s.length);
			ResultSet result = stmt.executeQuery();
			try {
				if (result.next()) {
					try {
						id = result.getInt("id");
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return Optional.ofNullable(id);
	}

	private Optional<Integer> createSample(Sample s) {
		Integer id = null;
		try {
			PreparedStatement stmt = mysql.conn_id.prepareStatement(
					"INSERT INTO `" + sampleTableName
							+ "` SET `name`='?', `comment`='?', `length`='?'");
			stmt.setString(1, s.name);
			stmt.setString(2, s.comments);
			stmt.setDouble(1, s.length);

			stmt.executeUpdate();
			ResultSet result = stmt.getGeneratedKeys();

			try {
				IntStream.rangeClosed(1, result.getMetaData().getColumnCount())
						.forEach(i -> {
							try {
								System.out.println(
										result.getMetaData().getColumnLabel(i));
							} catch (Exception e) {
							}
						});
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				if (result.next()) {
					id = result.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return Optional.ofNullable(id);
	}

	private boolean sampleExists(Sample s) {
		// TODO Auto-generated method stub
		return false;
	}

	private void createDataStructure() {
		mysql.queryUpdatef("CREATE TABLE IF NOT EXISTS `%s` ("
				+ "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
				+ "`name` varchar(128) NOT NULL DEFAULT 'name not chosen',"
				+ "`comment` varchar(256) NOT NULL DEFAULT 'comment not provided',"
				+ "`length` varchar(16) NOT NULL DEFAULT 'nolength')",
				sampleTableName);
		mysql.queryUpdatef("CREATE TABLE IF NOT EXISTS `%s` ("
				+ "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
				+ "`uploadtime` TIMESTAMP," + "`timestamp` BIGINT NOT NULL,"
				+ "`id_sample` INT NOT NULL,"
				+ "`id_channel` INT NOT NULL," + "`temperature` FLOAT NOT NULL,"
				+ "`frequency` FLOAT NOT NULL,"
				+ "`amplitude` FLOAT NOT NULL,"
				+ "`diffusivity` DOUBLE NOT NULL" + ")", measuresTableName);
	}

	private boolean dataStructureExists() {
		try {
			ResultSet rs = mysql.queryf("SELECT 1 FROM `%s`", sampleTableName);
			if (rs == null)
				return false;
			try {
				if (!rs.next())
					return false;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}

			rs = mysql.queryf("SELECT 1 FROM `%s`", measuresTableName);
			if (rs == null)
				return false;
			try {
				if (!rs.next())
					return false;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void uploadMeasurement(Measurement m, Integer id) {
		IntStream.range(0, m.tCond.size())
				.forEach(channel -> mysql.queryUpdatef(
						"INSERT INTO `%s` SET " + "`id_sample` = '%d',"
								+ "`id_channel` = '%d',"
								+ "`temperature` = %.1f,"
								+ "`timestamp` = '%d',"
								+ "`frequency` = '%.1f',"
								+ "`amplitude` = '%.3f',"
								+ "`diffusivity` = '%.12f'",
						measuresTableName, id, channel,
						m.temperature.get(0).value, m.time, m.frequency,
						m.tCond.get(channel).amplitude, m.tCond.get(0).tCond));
	}
}
