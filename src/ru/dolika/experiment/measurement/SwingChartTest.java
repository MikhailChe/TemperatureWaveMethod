package ru.dolika.experiment.measurement;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

public class SwingChartTest {
	private static ScatterChart<Number, Number> chart;

	public static void main(String[] argv) {
		final JFrame frame = new JFrame("Swing&FX");
		final JFXPanel fxPanel = new JFXPanel();
		frame.add(fxPanel);

		frame.setSize(800, 600);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		Platform.runLater(() -> {
			Scene scene = createScene();
			fxPanel.setScene(scene);
		});

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ObservableList<Data<Number, Number>> data = chart.getData()
				.get(0)
				.getData();

		Platform.runLater(() -> {
			data.add(new Data<Number, Number>(1, 1));
			data.add(new Data<Number, Number>(2, 1));
			data.add(new Data<Number, Number>(3, 1));
			data.add(new Data<Number, Number>(3, 2));
			data.add(new Data<Number, Number>(2, 2));
			data.add(new Data<Number, Number>(1, 2));
		});

	}

	private static Scene createScene() {

		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setAutoRanging(true);
		yAxis.setAutoRanging(true);
		chart = new ScatterChart<>(xAxis, yAxis);

		xAxis.setLabel("Temperature");
		yAxis.setLabel("Температуропроводность");
		chart.setTitle("Онлайн-график");

		XYChart.Series<Number, Number> series1 = new XYChart.Series<>();

		series1.setName("1Канал");

		// series1.getData().add(new Data<Number, Number>(4.2, 193.2));
		// series1.getData().add(new Data<Number, Number>(2.8, 33.6));
		// series1.getData().add(new Data<Number, Number>(6.2, 24.8));
		// series1.getData().add(new Data<Number, Number>(1, 14));
		// series1.getData().add(new Data<Number, Number>(1.2, 26.4));
		// series1.getData().add(new Data<Number, Number>(4.4, 114.4));
		// series1.getData().add(new Data<Number, Number>(8.5, 323));
		// series1.getData().add(new Data<Number, Number>(6.9, 289.8));
		// series1.getData().add(new Data<Number, Number>(9.9, 287.1));
		// series1.getData().add(new Data<Number, Number>(0.9, -9));
		// series1.getData().add(new Data<Number, Number>(3.2, 150.8));
		// series1.getData().add(new Data<Number, Number>(4.8, 20.8));
		// series1.getData().add(new Data<Number, Number>(7.3, -42.3));
		// series1.getData().add(new Data<Number, Number>(1.8, 81.4));
		// series1.getData().add(new Data<Number, Number>(7.3, 110.3));
		// series1.getData().add(new Data<Number, Number>(2.7, 41.2));
		chart.setAnimated(false);
		chart.setLegendSide(Side.LEFT);
		chart.setTitleSide(Side.BOTTOM);
		chart.getData().add(series1);
		final Scene scene = new Scene(chart);

		return scene;
	}
}
