package view.experiment.phaseAdjust;

import java.awt.Dimension;

import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import debug.Debug;
import model.phaseAdjust.PhaseAdjust;

public class PhaseAdjustViewerPanel extends ChartPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7118964792998797256L;

	private PhaseAdjust shifts;

	final private NumberAxis xAxis, yAxis;

	final private DefaultXYDataset dataset;

	public PhaseAdjustViewerPanel(PhaseAdjust zc) {
		super(null);
		Debug.println("Был вызван суперконструктор");
		setPreferredSize(new Dimension(256, 256));
		setMinimumSize(getPreferredSize());

		// xAxis = new CategoryAx

		dataset = new DefaultXYDataset();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
		JFreeChart chart = ChartFactory.createScatterPlot(null, null, null, dataset, PlotOrientation.HORIZONTAL, false,
				false, false);

		XYPlot xyPlot = chart.getXYPlot();
		xyPlot.setRenderer(renderer);

		xAxis = (NumberAxis) xyPlot.getRangeAxis();
		yAxis = (NumberAxis) xyPlot.getDomainAxis();

		xAxis.setAutoRange(false);
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(30);

		yAxis.setAutoRange(false);

		setChart(chart);
		setPopupMenu(null);

		this.setMouseZoomable(false, false);
//		this.setDomainZoomable(false);
//		this.setRangeZoomable(false);

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

	public PhaseAdjust setZeroCrossing(PhaseAdjust newZC) {
		if (newZC != null) {
			this.shifts = newZC;
		} else {
			// TODO: null pointer exception?
			Debug.println("Новые данные нулевые");
		}
		if (this.shifts != null) {
			SwingUtilities.invokeLater(() -> {
				rescale();
				Debug.println("Запущена процедура по добавлению данных в другом потоке.");

				double minFreq = this.shifts.minFrequency();
				double maxFreq = this.shifts.maxFrequency();

				int numberOfDots = (int) Math.round(Math.ceil((maxFreq - minFreq) / .1));

				double[][] data = new double[2][numberOfDots];

				for (int i = 0; i < numberOfDots; i++) {
					double freq = minFreq + i * .1;
					data[1][i] = freq;
					data[0][i] = this.shifts.getCurrentShift(freq);
					Debug.println(String.format("freq: %f -> %f", freq, data[1][i]));
				}

				dataset.addSeries(0, data);

				Debug.println("Процедура добавления данных выполнена");
			});
		}
		return this.shifts;
	}
}
