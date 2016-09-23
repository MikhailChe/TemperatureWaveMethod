package view.measurements;

import static java.util.stream.Collectors.toList;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.border.SoftBevelBorder;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.util.StringConverter;
import model.experiment.measurement.Measurement;

public class MeasurementViewer extends JPanel {
	private static final long serialVersionUID = 3555290921726804677L;

	final ScatterChart<Number, Number> chart;
	final JFXPanel chartPanel;

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

	final private Map<MeasurementProperty, XYChart.Series<Number, Number>> chanForProp;

	public MeasurementViewer() {
		super();
		Dimension d = new Dimension(640, 480);
		setPreferredSize(d);
		setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));

		chartPanel = new JFXPanel();

		NumberAxis xAxis = new NumberAxis(200, 300, 20);
		NumberAxis yAxis = new NumberAxis(0, 2E-5, 1E-6);

		xAxis.setLabel("Температура");
		yAxis.setLabel("Коэффициент температуропроводности");

		yAxis.setTickUnit(1E-6);
		yAxis.setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public String toString(Number object) {

				return String.format("%3.2E", object.floatValue());
			}

			@Override
			public Number fromString(String string) {
				return Double.valueOf(string);
			}
		});

		xAxis.setAutoRanging(true);
		xAxis.setForceZeroInRange(false);

		chart = new ScatterChart<>(xAxis, yAxis);
		chart.setTitle("Измерения");

		Scene scene = new Scene(chart);
		chartPanel.setScene(scene);

		this.setLayout(new BorderLayout());
		this.add(chartPanel, BorderLayout.CENTER);

		chanForProp = Collections.synchronizedMap(new Hashtable<>());
	}

	public void addMeasurement(Measurement m) {
		try {
			double temperature = m.temperature.get(0).value;
			List<Data<Number, Number>> dataPoints = m.diffusivity.stream().map(
					t -> new Data<Number, Number>(temperature, t.diffusivity))
					.collect(toList());
			Platform.runLater(() -> {
				for (int i = 0; i < dataPoints.size(); i++) {
					MeasurementProperty mp = new MeasurementProperty(m, i);
					Series<Number, Number> ser = dataForProperty(mp);
					ser.getData().add(dataPoints.get(i));
				}
			});
		} catch (NullPointerException ignore) {

		}
	}

	private Series<Number, Number> dataForProperty(MeasurementProperty mp) {
		return chanForProp.computeIfAbsent(mp, key -> {
			return newDataSeries(key);
		});
	}

	private Series<Number, Number> newDataSeries(MeasurementProperty mp) {
		List<Series<Number, Number>> data = chart.getData();
		XYChart.Series<Number, Number> ser = new XYChart.Series<>();
		ser.setName(mp.freq + "Hz#" + mp.channel);
		data.add(ser);
		return ser;
	}
}