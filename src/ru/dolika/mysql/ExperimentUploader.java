package ru.dolika.mysql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import ru.dolika.experiment.measurement.Measurement;
import ru.dolika.experiment.sample.Sample;

public class ExperimentUploader {

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
			createSampleTable(s);
		}

		Integer id = getSampleId(s);

		measures.stream().map(m -> uploadMeasurement(m, id)).filter(a -> a).count();
	}

	private Integer getSampleId(Sample s) {
		// TODO Auto-generated method stub
		return null;
	}

	private void createSampleTable(Sample s) {
		// TODO Auto-generated method stub

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
