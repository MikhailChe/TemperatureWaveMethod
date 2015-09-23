package ru.dolika.experiment.measurement;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class MeasurementFactory {

	public static Measurement getMeasurement() {
		return new Measurement();
	}

	public static Measurement forFile(String filename) {
		Measurement m = null;
		try (FileInputStream fis = new FileInputStream(filename)) {
			ObjectInputStream ois = new ObjectInputStream(fis);
			Object o = ois.readObject();
			if (o instanceof Measurement) {
				m = (Measurement) o;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (m == null) {
			m = new Measurement();
		}
		return m;
	}

}
