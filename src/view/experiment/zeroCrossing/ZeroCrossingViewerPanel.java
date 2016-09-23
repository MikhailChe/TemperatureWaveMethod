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

	public ZeroCrossingViewerPanel(ZeroCrossing zc) {
		super();
		Debug.println("Был вызван суперконструктор");
		setPreferredSize(new Dimension(256, 256));
		setMinimumSize(getPreferredSize());

		NumberAxis xAxis = new NumberAxis(0, 50, 10);
		NumberAxis yAxis = new NumberAxis(-180, 180, 90);

		xAxis.setAutoRanging(true);
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
		setZeroCrossing(zc);
	}

	public ZeroCrossing setZeroCrossing(ZeroCrossing newZC) {
		this.shifts = newZC;
		Platform.runLater(() -> {
			Debug.println(
					"Запущена процедура по добавлению данных в другом потоке.");
			List<Data<Number, Number>> list = chart.getData().get(0).getData();
			list.clear();
			for (double x = .1; x < 30; x += .1) {
				Data<Number, Number> data = new Data<>(x,
						shifts.getCurrentShift(x));
				list.add(data);
			}
			Debug.println("Процедура добавления данных выполнена");
		});

		return this.shifts;
	}
}
