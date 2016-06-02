package ru.dolika.mysql;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import ru.dolika.experiment.measurement.Measurement;
import ru.dolika.experiment.sample.Sample;

public class ExperimentUploader {

	final static private String sampleTableName = "tp_samples";
	final static private String measuresTableName = "tp_measures";

	Mysql mysql;

	public ExperimentUploader()
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		MySQLCredentials creds = new MySQLCredentials();
		mysql = new Mysql(creds.address, creds.username, creds.password, creds.database);
	}

	public void uploadExperimentResults(List<Measurement> measures, Sample s) {
		if (!dataStructureExists()) {
			createDataStructure();
		}
		if (!sampleExists(s)) {
			createSample(s);
		}
		Integer id = getSampleId(s).orElse(-1);

		measures.stream().map(m -> uploadMeasurement(m, id)).filter(a -> a).count();

	}

	private Optional<Integer> getSampleId(Sample s) {
		ResultSet result = mysql.query("SELECT `id` " + " FROM `" + sampleTableName + "` " + " WHERE `name` = '"
				+ s.name + "' " + " AND `length` = '" + s.length + "'");
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
		ResultSet result = mysql.queryf("INSERT INTO `%s` SET `name`='%s', `comment`='%s', `length`='%.6f'",
				sampleTableName, s.name, s.comments, s.length);
		Integer id = null;
		try {
			if (result.next()) {
				id = result.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Optional.ofNullable(id);
	}

	private boolean sampleExists(Sample s) {
		// TODO Auto-generated method stub
		return false;
	}

	private void createDataStructure() {
		// TODO Auto-generated method stub

	}

	private boolean dataStructureExists() {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean uploadMeasurement(Measurement m, Integer id) {
		boolean exists = measurementExists(m, id);

		return !exists;
	}

	private boolean measurementExists(Measurement m, Integer id) {
		// TODO Auto-generated method stub
		return false;
	}
}
