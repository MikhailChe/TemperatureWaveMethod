package ru.dolika.experiment.measurement;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.stream.IntStream;

import javax.swing.JPanel;
import javax.swing.border.SoftBevelBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.SeriesException;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

public class MeasurementViewer extends JPanel {
	private static final long serialVersionUID = 3555290921726804677L;

	final DefaultTableXYDataset dataset;
	final JFreeChart chart;
	final ChartPanel chartPanel;

	public MeasurementViewer() {
		super();
		Dimension d = new Dimension(640, 480);
		setPreferredSize(d);
		setSize(d);
		setMinimumSize(d);
		setMaximumSize(d);
		setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));

		dataset = new DefaultTableXYDataset(true);
		chart = ChartFactory.createScatterPlot("Измерения", "Температура", "Коэффициент температуропроводности",
				dataset);

		this.setLayout(new BorderLayout());
		chartPanel = new ChartPanel(chart);
		this.add(chartPanel, BorderLayout.CENTER);
	}

	public void addMeasurement(Measurement m) {
		if (m == null || m.temperature == null || m.tCond == null)
			throw new NullPointerException();
		if (m.temperature.size() == 0) {
			System.err.println("Temperature is not present");
			return;
		}
		if (m.tCond.size() == 0) {
			System.err.println("Temperature conductivity is not present");
			return;
		}

		if (dataset.getSeriesCount() != m.tCond.size()) {
			for (int i = dataset.getSeriesCount(); i < m.tCond.size(); i++) {
				dataset.addSeries(new XYSeries("Канал " + (i + 1), false, false));
			}
		}

		IntStream.range(0, m.tCond.size()).parallel().forEach(i -> {
			TemperatureConductivity tc = m.tCond.get(i);
			if (tc.tCond < 10E-5 && tc.tCond > 1E-6) {
				XYSeries series = dataset.getSeries(i);

				try {
					series.addOrUpdate(m.temperature.get(0).value, tc.tCond);
				} catch (SeriesException e) {
					// May throw that X-value already exists. Ignore it.
				}
			}
		});
	}

}
