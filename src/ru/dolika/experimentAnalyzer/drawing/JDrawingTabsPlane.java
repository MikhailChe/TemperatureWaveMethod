package ru.dolika.experimentAnalyzer.drawing;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import ru.dolika.experimentAnalyzer.ExperimentReader;

public class JDrawingTabsPlane extends JTabbedPane {

	public static void main(String[] args) {
		JFrame frame = new JFrame("yo");
		frame.setLayout(new BorderLayout(5, 5));
		frame.add(new JDrawingTabsPlane());
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}

	public JDrawingTabsPlane() {
		super();
		for (int i = 0; i < 4; i++) {
			double[][] vals = new double[2][1000];
			for (double[] val : vals) {
				double freq = Math.random() * 2.0 + 5.0;
				double amplitude = Math.random() * 1000 + 10000;
				for (int j = 0; j < val.length; j++) {
					val[j] = Math.sin(2.0 * Math.PI * freq * j
							/ ((float) val.length))
							* amplitude;
				}
			}
			addTab("Сигнал #" + (i + 1), new JDrawingPlane(vals));
			System.out.println(i);
		}
	}

	public void loadData(ExperimentReader ereader, int channel, boolean full) {
		double[][] data = ereader.getCroppedData();
		for (int i = 0; i < data.length; i++) {
			addTab("Канал №" + (i + 1), new JDrawingPlane(data[i]));
		}
	}
}
