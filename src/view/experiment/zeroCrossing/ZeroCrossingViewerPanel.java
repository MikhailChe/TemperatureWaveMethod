package view.experiment.zeroCrossing;

import java.awt.Dimension;
import java.util.List;

import debug.Debug;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import model.experiment.zeroCrossing.ZeroCrossing;

public class ZeroCrossingViewerPanel extends JFXPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7118964792998797256L;

	private ZeroCrossing shifts;
	private LineChart<Number, Number> chart;

	private NumberAxis xAxis, yAxis;

	public ZeroCrossingViewerPanel(ZeroCrossing zc) {
		super();

		Debug.println("Был вызван суперконструктор");
		setPreferredSize(new Dimension(256, 256));
		setMinimumSize(getPreferredSize());

		xAxis = new NumberAxis(0, 30, 5);

		yAxis = new NumberAxis(-360, 360, 45);

		xAxis.setAutoRanging(false);
		yAxis.setAutoRanging(false);

		xAxis.setPadding(new Insets(0));
		yAxis.setPadding(new Insets(0));

		Debug.println("Были созданы оси. Ничего страшного не должно быть");
		chart = new LineChart<>(xAxis, yAxis);

		Series<Number, Number> series = new Series<>();

		chart.getData().add(series);

		chart.setLegendVisible(false);

		chart.setPadding(new Insets(0));
		Debug.println("Были добавлены series");
		Debug.println("Создаём сцену");
		Scene scene = new Scene(chart);
		Debug.println("Устанавливаем сцену в панель");
		Platform.runLater(() -> this.setScene(scene));
		Debug.println("Сцена создана. Добавляем данные о юстировке");
		if (zc != null)
			setZeroCrossing(zc);
	}

	private void rescale() {
		double MINANGLE = 0;
		double minShift = this.shifts.minShift();
		if (minShift < -180) {
			MINANGLE = -360;
		} else if (minShift < 0) {
			MINANGLE = -180;
		}

		double MAXANGLE = 0;
		double maxShift = this.shifts.maxShift();
		if (maxShift > 180) {
			MAXANGLE = 360;
		} else if (maxShift > 0) {
			MAXANGLE = 180;
		}
		yAxis.setLowerBound(MINANGLE);
		yAxis.setUpperBound(MAXANGLE);
	}

	public ZeroCrossing setZeroCrossing(ZeroCrossing newZC) {
		if (newZC != null) {
			this.shifts = newZC;
		} else {
			// TODO: null pointer exception?
			Debug.println("Новые данные нулевые");
		}
		if (this.shifts != null) {
			Platform.runLater(() -> {
				rescale();
				Debug.println("Запущена процедура по добавлению данных в другом потоке.");
				List<Data<Number, Number>> list = chart.getData().get(0).getData();
				list.clear();
				for (double x = this.shifts.minFrequency(); x <= this.shifts.maxFrequency(); x += .1) {
					Data<Number, Number> data = new Data<>(x, shifts.getCurrentShift(x));
					list.add(data);
				}
				Debug.println("Процедура добавления данных выполнена");
			});
		}
		return this.shifts;
	}
}
