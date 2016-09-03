package ru.dolika.experiment.measurement.test;

import static java.util.Arrays.asList;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.junit.Test;

import ru.dolika.experiment.measurement.Measurement;
import ru.dolika.experiment.measurement.MeasurementViewer;
import ru.dolika.experiment.measurement.Temperature;
import ru.dolika.experiment.measurement.TemperatureConductivity;

public class MeasurementViewerTest {

	@Test
	public void testCompoenentAdd() {

		JFrame frame = new JFrame("Test");
		frame.setPreferredSize(new Dimension(640, 480));
		MeasurementViewer view = new MeasurementViewer();
		frame.add(view);
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);

		for (int i = 0; i < 400; i++) {
			Measurement m = new Measurement();
			m.frequency = 2;
			m.tCond.addAll(asList(
					new TemperatureConductivity()
							.settCond(7E-6 - Math.sin(i * 1E-2) * 1E-6),
					new TemperatureConductivity()
							.settCond(7.2E-6 - Math.cos(i * 1E-2) * 1E-6)));
			m.temperature.add(new Temperature().setValue(273 + i * 5));

			view.addMeasurement(m);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		while (frame.isVisible())
			;

	}
}
