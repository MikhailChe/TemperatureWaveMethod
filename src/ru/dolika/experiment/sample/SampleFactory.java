package ru.dolika.experiment.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.ProgressMonitorInputStream;

import ru.dolika.experiment.measurement.Measurement;
import ru.dolika.experiment.measurement.MeasurementFactory;
import ru.dolika.experiment.measurement.Temperature;
import ru.dolika.experiment.measurement.TemperatureConductivity;

public class SampleFactory {

	static boolean debug = true;

	public static void main(String... args) {
		String filename = "Samplebinary.smpl";
		Sample sample = null;
		if (new File(filename).exists()) {
			sample = SampleFactory.forBinary(filename);

		}

		if (sample == null) {
			sample = SampleFactory.getSample();
			sample.comments = "Рандомный комент";
			sample.length = 0.962E-3;
			sample.name = "Припой Ag82Fe12Al6";

			for (int i = 1; i < 4; i++) {
				Measurement m = MeasurementFactory.getMeasurement();
				m.frequency = i;
				TemperatureConductivity tCond = new TemperatureConductivity();
				tCond.amplitude = 100 + Math.random();
				tCond.phase = 3.1;
				tCond.kappa = Math.sqrt(2) * (tCond.phase - Math.PI / 4.0);

				tCond.tCond = ((2.0 * Math.PI * m.frequency) * (sample.length)) / (tCond.kappa * tCond.kappa);
				m.tCond.add(tCond);

				Temperature temp = new Temperature();
				temp.value = 300 + Math.random() * 100;
				m.temperature.add(temp);

				sample.measurements.add(m);
			}
		}
		sample.sort();
		System.out.println(sample);
		SampleFactory.saveSample(filename, sample);
	}

	public static Sample getSample() {
		return new Sample();
	}

	public static Sample forBinary(String filename) {
		if (debug)
			System.out.println("Opening samplefile " + filename);

		try (ObjectInputStream ois = new ObjectInputStream(
				new ProgressMonitorInputStream(null, "Открытие", new FileInputStream(filename)))) {
			Object o = ois.readObject();
			if (o instanceof Sample) {
				Sample sample = (Sample) o;
				if (debug)
					System.out.println("Opened sample binary");
				if (debug)
					if (sample.name == null)
						System.out.println("Sample name empty (null)");
					else
						System.out.println("Sample name: " + sample.name);

				return sample;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (debug)
			System.out.println("Problems openning sample file");
		return null;
	}

	public static boolean saveSample(String filename, Sample sample) {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
			oos.writeObject(sample);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
