package view.measurements;

import static java.util.stream.Collectors.toList;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.border.SoftBevelBorder;

import experiment.measurement.Measurement;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.util.StringConverter;

public class MeasurementViewer extends JPanel {
	private static final long serialVersionUID = 3555290921726804677L;

	final ScatterChart<Number, Number> chart;
	final JFXPanel chartPanel;

	public MeasurementViewer() {
		super();
		Dimension d = new Dimension(640, 480);
		setPreferredSize(d);
		setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));

		chartPanel = new JFXPanel();

		NumberAxis xAxis = new NumberAxis(200, 300, 20);
		NumberAxis yAxis = new NumberAxis(0, 1E-5, 1E-6);

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
	}

	public void addMeasurement(Measurement m) {
		try {

			double temperature = m.temperature.get(0).value;
			List<Data<Number, Number>> dataPoints = m.tCond.stream()
					.map(t -> new Data<Number, Number>(temperature, t.tCond))
					.collect(toList());
			Iterator<Data<Number, Number>> iter = dataPoints.iterator();
			Platform.runLater(() -> {
				matchSeriesSize(m.tCond.size());
				chart.getData().stream().map(Series::getData)
						.forEachOrdered(data -> data.add(iter.next()));
			});
		} catch (NullPointerException ignore) {

		}
	}

	private void matchSeriesSize(int size) {
		if (chart.getData().size() < size) {
			List<Series<Number, Number>> data = chart.getData();

			while (data.size() < size) {
				XYChart.Series<Number, Number> ser = new XYChart.Series<>();
				ser.setName("Канал " + (data.size() + 1));
				data.add(ser);
			}
		}
	}

}
