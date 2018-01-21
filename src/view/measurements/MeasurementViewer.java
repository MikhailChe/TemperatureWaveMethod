package view.measurements;

import static java.awt.BorderLayout.CENTER;
import static java.util.stream.Collectors.toList;
import static javax.swing.border.BevelBorder.LOWERED;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.SoftBevelBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import model.experiment.measurement.Measurement;

public class MeasurementViewer extends JPanel {
	private static final long serialVersionUID = 3555290921726804677L;

	final private DefaultTableXYDataset dataset;

	final ChartPanel chartPanel;

	final class MeasurementProperty {
		final double freq;
		final int channel;

		public MeasurementProperty(Measurement m, int channel) {
			this.freq = m.frequency;
			this.channel = channel;
		}

		@Override
		final public boolean equals(Object o) {
			if (o == null)
				return false;
			if (o == this)
				return true;
			if (!(o instanceof MeasurementProperty))
				return false;
			MeasurementProperty m = (MeasurementProperty) o;
			return (m.channel == this.channel) && (m.freq == this.freq);
		}

		@Override
		final public int hashCode() {
			return Double.hashCode(freq) + Integer.hashCode(channel);
		}
	}

	final private Map<MeasurementProperty, XYSeries> datasetForProperty;

	public MeasurementViewer() {
		super();

		datasetForProperty = Collections.synchronizedMap(new Hashtable<>());

		NumberAxis xAxis = new NumberAxis("Температура");// (200, 300, 20);
		NumberAxis yAxis = new NumberAxis("Коэффициент температуропроводности");// (0, 2E-5, 1E-6);

		yAxis.setLowerBound(0);
		yAxis.setUpperBound(2E-5);
		yAxis.setAutoRangeIncludesZero(true);
		yAxis.setTickUnit(new NumberTickUnit(1E-6, new DecimalFormat("0.000E000")));

		// yAxis.setTickUnit(1E-6);
		// yAxis.setTickLabelFormatter(new StringConverter<Number>() {
		// @Override
		// public String toString(Number object) {
		//
		// return String.format("%3.2E", object.floatValue());
		// }
		//
		// @Override
		// public Number fromString(String string) {
		// return Double.valueOf(string);
		// }
		// });
		xAxis.setAutoRange(true);
		// xAxis.setAutoRanging(true);
		xAxis.setAutoRangeIncludesZero(false);
		// xAxis.setForceZeroInRange(false);

		dataset = new DefaultTableXYDataset(true);

		JFreeChart chart = new JFreeChart("Измерения", new XYPlot(dataset, xAxis, yAxis, new XYShapeRenderer()));
		chartPanel = new ChartPanel(chart);

		Dimension d = new Dimension(640, 480);
		setPreferredSize(d);
		setBorder(new SoftBevelBorder(LOWERED));

		this.setLayout(new BorderLayout());
		this.add(chartPanel, CENTER);

	}

	List<Measurement> measurements = new ArrayList<>();

	@SuppressWarnings("unused")
	public void addMeasurement(Measurement m) {
		measurements.add(m);
		try {
			double temperature = m.temperature.get(0).value;

			List<XYDataItem> dataPoints = m.diffusivity
					.stream()
					.map(t -> new XYDataItem(temperature, t.diffusivity))
					.collect(toList());
			SwingUtilities.invokeLater(() -> {
				for (int i = 0; i < dataPoints.size(); i++) {
					MeasurementProperty mp = new MeasurementProperty(m, i);
					XYSeries ser = dataForProperty(mp);
					ser.addOrUpdate(dataPoints.get(i));
				}
			});
		} catch (Exception ignore) {
			// Всё нормально, просто при добавлении произошли какие-то проблемы
		}
	}

	private XYSeries dataForProperty(MeasurementProperty mp) {
		return datasetForProperty.computeIfAbsent(mp, key -> {
			return newDataSeries(key);
		});
	}

	private XYSeries newDataSeries(MeasurementProperty mp) {
		XYSeries ser = new XYSeries(mp.freq + "Hz#" + mp.channel, true, false);
		dataset.addSeries(ser);
		return ser;
	}
}