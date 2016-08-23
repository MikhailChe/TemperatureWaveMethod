package ru.dolika.experiment.Analyzer;

import java.io.File;
import java.io.IOException;

import ru.dolika.experiment.measurement.Measurement;
import ru.dolika.experiment.measurement.Temperature;
import ru.dolika.experiment.measurement.TemperatureConductivity;

public class TestFaultyData {
	public static void main(String[] args) throws IOException {
		File f = new File(
				"C:\\Users\\Mikhail\\Documents\\FaultyData!\\290616115712.txt");
		ExperimentFileReader fileReader = new ExperimentFileReader(f.toPath());
		System.out.println("File info from fileReader");
		System.out.printf("Columns: %d%n", fileReader.getColumnCount());
		System.out.printf("Cropped data array size: %d%n",
				fileReader.getCroppedData().length);
		System.out.print("File column lengths: ");
		for (int i = 0; i < fileReader.getColumnCount(); i++) {
			System.out.printf("%d ", fileReader.getCroppedData()[i].length);
		}
		System.out.println();
		System.out.printf("Periods: %d%n",
				fileReader.getCroppedDataPeriodsCount());
		System.out.printf("Pulses: %d%n", fileReader.getPulseIndicies().length);
		System.out.println("|||||||||||||||||||||||||||||||||");
		TWMComputer compute = new TWMComputer(f);
		Measurement m = compute.call();
		System.out.printf("%12s%f%n", "Frequency: ", m.frequency);
		System.out.printf("%12s%d%n", "Time: ", m.time);

		System.out.println("|||||||||||||||||||||||||||||||||");
		for (TemperatureConductivity tc : m.tCond) {
			System.out.printf("Freq:\t%f%nAmp:\t%f%nPhase:\t%f%nNull:\t%f%n",
					tc.frequency, tc.initSignalParams.amplitude,
					tc.initSignalParams.phase, tc.initSignalParams.nullOffset);
			System.out.println("======================");
		}
		System.out.println("|||||||||||||||||||||||||||||||||");
		for (Temperature t : m.temperature) {
			System.out.printf("Temperature: %f K%n", t.value);
		}
		System.out.println("|||||||||||||||||||||||||||||||||");
	}
}
