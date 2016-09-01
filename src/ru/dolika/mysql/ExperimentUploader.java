package ru.dolika.mysql;

import java.io.IOException;
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

	final Mysql					mysql;

	public ExperimentUploader() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException,
			IOException {
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
		ResultSet result = mysql.query("SELECT `id` FROM `" + sampleTableName
				+ "`  WHERE `name` = ? AND `length` = ?", s.getName(),
				s.getLength());
		Integer id = null;
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
		return Optional.ofNullable(id);
	}

	private Optional<Integer> createSample(Sample s) {
		Integer id = null;
		try {
			ResultSet result = mysql
					.queryUpdate("INSERT INTO `" + sampleTableName
							+ "` SET `name`=?, `comment`=?, `length`=?", s.getName(),
							s.getComment(),
							s.getLength());
			if (result.next()) {
				id = result.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Optional.ofNullable(id);
	}

	boolean sampleExists(Sample s) {
		try {
			ResultSet rs = mysql.query("SELECT * FROM `" + sampleTableName
					+ "` WHERE `name` = ? AND `comment`=? AND `length`=?",
					s.getName(),
					s.getComment(), s.getLength());
			if (rs == null)
				return false;
			if (rs.next() == false)
				return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	private void createDataStructure() {
		mysql.queryUpdate(String.format("CREATE TABLE IF NOT EXISTS `%s` ("
				+ "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
				+ "`name` varchar(128) NOT NULL DEFAULT 'name not chosen',"
				+ "`comment` varchar(256) NOT NULL DEFAULT 'comment not provided',"
				+ "`length` varchar(16) NOT NULL DEFAULT 'nolength')",
				sampleTableName));
		mysql.queryUpdate(String.format("CREATE TABLE IF NOT EXISTS `%s` ("
				+ "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
				+ "`uploadtime` TIMESTAMP," + "`timestamp` BIGINT NOT NULL,"
				+ "`id_sample` INT NOT NULL," + "`id_channel` INT NOT NULL,"
				+ "`temperature` FLOAT NOT NULL,"
				+ "`frequency` FLOAT NOT NULL," + "`amplitude` FLOAT NOT NULL,"
				+ "`diffusivity` DOUBLE NOT NULL" + ")", measuresTableName));
	}

	private boolean dataStructureExists() {
		try {
			ResultSet rs = mysql.query(String.format("SELECT 1 FROM `%s`",
					sampleTableName));
			if (rs == null)
				return false;
			try {
				if (!rs.next())
					return false;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}

			rs = mysql.query(String.format("SELECT 1 FROM `%s`",
					measuresTableName));
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
		IntStream.range(0, m.tCond.size()).forEach(channel -> mysql.queryUpdate(
				String.format("INSERT INTO `%s` SET " + "`id_sample` = ?,"
						+ "`id_channel` = ?," + "`temperature` = ?,"
						+ "`timestamp` = ?," + "`frequency` = ?,"
						+ "`amplitude` = ?," + "`diffusivity` = ?",
						measuresTableName),
				id, channel, m.temperature.get(
						0).value,
				m.time, m.frequency, m.tCond.get(
						channel).amplitude,
				m.tCond.get(
						0).tCond));
	}
}
