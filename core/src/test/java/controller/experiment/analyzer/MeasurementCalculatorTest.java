package controller.experiment.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import controller.experiment.analyzer.MeasurementCalculator.SampleProperties;
import model.measurement.Measurement;
import model.phaseAdjust.PhaseAdjust;
import model.signalID.AdjustmentSignalID;
import model.signalID.BaseSignalID;
import model.signalID.DCsignalID;
import model.thermocouple.graduate.Graduate;

public class MeasurementCalculatorTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void reusesLoadedCalibrationAcrossMeasurements() throws IOException {
		Path calibrationFile = folder.newFile("phase.zc").toPath();
		Files.writeString(calibrationFile, "0,5\t7,5\n1,5\t12,5\n");
		PhaseAdjust calibration = PhaseAdjust.load(calibrationFile, Locale.FRANCE);
		Files.delete(calibrationFile);

		BaseSignalID base = new BaseSignalID();
		base.phaseAdjust = calibration;
		Graduate graduate = new Graduate() {
			@Override
			public double getTemperature(double millivolts, double zeroTemperature) {
				return zeroTemperature + millivolts * 2;
			}
		};
		var channels = Arrays.asList(null, base, null, new DCsignalID(graduate), new AdjustmentSignalID());
		Path firstFile = writeMeasurement("first.txt");
		Path secondFile = writeMeasurement("second.txt");
		Measurement first = new MeasurementCalculator(new SampleProperties(0.002, 8000), channels)
				.calculate(firstFile, 1234);
		Measurement second = new MeasurementCalculator(new SampleProperties(0.004, 8000), channels)
				.calculate(secondFile, 5678);

		assertEquals(1234, first.time);
		assertEquals(5678, second.time);
		assertEquals(1, first.frequency, 0);
		assertEquals(2, first.diffusivity.size());
		assertEquals(1, first.diffusivity.get(0).channelNumber);
		assertEquals(4, first.diffusivity.get(1).channelNumber);
		assertEquals(1000, first.diffusivity.get(0).amplitude, 0.1);
		assertEquals(500, first.diffusivity.get(1).amplitude, 0.1);
		assertEquals(Math.toRadians(80), first.diffusivity.get(0).phase, 1e-10);
		double expected = Math.PI * 0.002 * 0.002 / Math.pow(Math.toRadians(35), 2);
		assertEquals(expected, first.diffusivity.get(0).diffusivity, 1e-12);
		assertEquals(expected * 4, second.diffusivity.get(0).diffusivity, 1e-12);
		assertEquals(1, first.temperature.size());
		double voltage = 1000.0 / 32767.5 * 10 / 270;
		assertEquals(voltage, first.temperature.get(0).signalLevel, 1e-12);
		assertEquals(291 + voltage * 2000, first.temperature.get(0).value, 1e-10);
	}

	@Test
	public void reportsMalformedMeasurementAndMissingReferencePeriod() throws IOException {
		Path file = folder.newFile("invalid.txt").toPath();
		var calculator = new MeasurementCalculator(null, Arrays.asList(null, new AdjustmentSignalID()));
		Files.writeString(file, "1\t10\n0\tinvalid\n");
		IOException parseError = assertThrows(IOException.class, () -> calculator.calculate(file, 0));
		assertTrue(parseError.getMessage().contains("line 2"));
		Files.writeString(file, "2\t10\n0\t100\n0\t100\n");
		IllegalArgumentException periodError = assertThrows(IllegalArgumentException.class,
				() -> calculator.calculate(file, 0));
		assertTrue(periodError.getMessage().contains("reference channel"));
	}

	private Path writeMeasurement(String name) throws IOException {
		int period = 1024;
		int rows = period * 3;
		StringBuilder content = new StringBuilder().append(rows).append("\t10\n");
		for (int row = 0; row < rows; row++) {
			int reference = row % period < 128 ? -10 : -1;
			double phase = 2 * Math.PI * (row - 128) / period;
			content.append(reference).append('\t').append(Math.round(-1000 * Math.cos(phase)))
					.append("\t0\t1000\t").append(Math.round(500 * Math.sin(phase))).append('\n');
		}
		return Files.writeString(folder.newFile(name).toPath(), content);
	}
}
