package ru.dolika.experiment.measurement;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class MeasurementFactory {

	public static Measurement getMeasurement() {
		return new Measurement();
	}

	public static Measurement forBinary(String filename) {

		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				filename))) {
			Object o = ois.readObject();
			if (o instanceof Measurement) {
				return (Measurement) o;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
