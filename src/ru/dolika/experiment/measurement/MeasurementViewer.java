package ru.dolika.experiment.measurement;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.border.SoftBevelBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

public class MeasurementViewer extends JPanel {
	private static final long serialVersionUID = 3555290921726804677L;

	final DefaultTableXYDataset dataset;

	public MeasurementViewer() {
		super();
		Dimension d = new Dimension(640, 480);
		setPreferredSize(d);
		setSize(d);
		setMinimumSize(d);
		setMaximumSize(d);
		setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));

		dataset = new DefaultTableXYDataset(true);
		JFreeChart chart = ChartFactory.createScatterPlot("Измерения", "Температура",
				"Коэффициент температуропроводности", dataset);

		this.setLayout(new BorderLayout());
		this.add(new ChartPanel(chart), BorderLayout.CENTER);
	}

	public void addMeasurement(Measurement m) {
		if (m == null || m.temperature == null || m.tCond == null)
			throw new NullPointerException();
		if (m.temperature.size() == 0)
			return;
		if (m.tCond.size() == 0)
			return;

		if (dataset.getSeriesCount() != m.tCond.size()) {
			for (int i = dataset.getSeriesCount(); i < m.tCond.size(); i++) {
				dataset.addSeries(new XYSeries("Канал " + (i + 1), false, false));
			}
		}

		for (int i = 0; i < m.tCond.size(); i++) {
			TemperatureConductivity tc = m.tCond.get(i);
			XYSeries series = dataset.getSeries(i);
			series.addOrUpdate(m.temperature.get(0).value, tc.tCond);
		}
		repaint();
	}

}
