package ru.dolika.fft;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Arrays;

import javax.swing.JComponent;

import org.jtransforms.fft.DoubleFFT_1D;

public class DrawingPlane extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3432370326222944519L;

	public DrawingPlane() {
		setPreferredSize(new Dimension(640, 480));
		setFocusable(true);
	}

	public void zoomIn(int x, int y) {
		double percScr = (double) x / (double) getWidth();

		double percScrAbs = map(percScr, 0, 1, x1perc, x2perc);
		double newLength = (x2perc - x1perc) / ZOOM_AMOUNT;
		applyZoom(percScrAbs, percScr, newLength);
	}

	public void zoomOut(int x, int y) {
		double percScr = (double) x / (double) getWidth();

		double percScrAbs = map(percScr, 0, 1, x1perc, x2perc);
		double newLength = (x2perc - x1perc) * ZOOM_AMOUNT;
		applyZoom(percScrAbs, percScr, newLength);
	}

	public void applyZoom(double percScrAbs, double percScr, double newLength) {
		double x1percTemp = percScrAbs - percScr * newLength;
		if (x1percTemp < 0)
			x1percTemp = 0;
		if (x1percTemp > 1) {
			x1percTemp = 1;
		}

		double x2percTemp = x1percTemp + newLength;
		if (x2percTemp < 0)
			x2percTemp = 0;
		if (x2percTemp > 1)
			x2percTemp = 1;
		x1perc = x1percTemp;
		x2perc = x2percTemp;
	}

	private double map(double val, double min1, double max1, double min2,
			double max2) {
		return (val - min1) / (max1 - min1) * (max2 - min2) + min2;
	}

	public void findExtreme() {
		findExtreme(data);
	}

	private void findExtreme(double[] array) {
		if (array == null)
			return;
		if (array.length == 0)
			return;

		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (array[i] > max)
				max = array[i];
			if (array[i] < min)
				min = array[i];
		}

	}

	double data[];
	static double expFreq;
	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;
	public static boolean shouldFilter = false;
	public static boolean shouldFFT = false;
	public static boolean isWavelet = false;

	static double x1perc = 0;
	static double x2perc = 1;
	final static double ZOOM_AMOUNT = 1.25;

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		draw((Graphics2D) g);
	}

	public void draw(Graphics2D g) {

		if (data == null)
			return;

		double[] dataCopy = Arrays.copyOf(data, data.length);
		double[] graphData;

		int maxim = (int) (dataCopy.length * x2perc + 0.5);
		if (maxim >= dataCopy.length) {
			maxim = dataCopy.length - 1;
		}
		int minim = (int) (dataCopy.length * x1perc);
		if (minim < 0) {
			minim = 0;
		}

		if (shouldFilter) {
			DoubleFFT_1D filter = new DoubleFFT_1D(dataCopy.length);
			filter.realForward(dataCopy);
			int filterFreqIndex = 2;
			for (int i = 0; i < dataCopy.length; i++) {
				if (i / 2 != filterFreqIndex) {
					dataCopy[i] = 0;
				}

			}
			filter.realInverse(dataCopy, true);
		}
		graphData = Arrays.copyOfRange(dataCopy, minim, maxim);
		findExtreme(graphData);

		double[] FFTdata = null;
		if (shouldFFT) {
			if (isWavelet) {
				int[] wavelet = new int[dataCopy.length];
				for (int i = 0; i < dataCopy.length; i++) {
					wavelet[i] = (int) dataCopy[i];
				}
				wavelet = FFT.discreteHaarWaveletTransform(wavelet);
				double[] wvd = new double[wavelet.length];
				for (int i = 0; i < wvd.length; i++) {
					wvd[i] = wavelet[i];
				}

				graphData = Arrays.copyOfRange(wvd,
						wvd.length / 4096 / 128 + 8, wvd.length);

			} else {
				FFTdata = Arrays.copyOf(dataCopy, dataCopy.length);

				DoubleFFT_1D fft = new DoubleFFT_1D(FFTdata.length);
				fft.realForward(FFTdata);
				graphData = FFT.getAbs(FFTdata);
				graphData[0] = 0;
			}
			findExtreme(graphData);
		}

		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_DISABLE);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_PURE);

		int width = getWidth();
		int height = getHeight();

		if (minim == 0) {
			g.setColor(Color.red);
			g.drawLine(1, 0, 1, height);
		}
		if (maxim == dataCopy.length - 1) {
			g.setColor(Color.red);
			g.drawLine(width - 2, 0, width - 2, height);
		}

		final int BORDER = 5;
		int dataLength = graphData.length;

		if (graphData.length < width) {
			g.setStroke(new BasicStroke(2f));
		} else {
			float val = (float) (2.0 * width / graphData.length);
			if (val < 0.25)
				val = 0.25f;
			g.setStroke(new BasicStroke(val));
		}
		{
			double prevData = 0;
			if (dataLength > 0)
				prevData = graphData[0];
			double maxLog = Math.log(dataLength - 1);
			for (int i = 1; i < dataLength; i++) {

				double curData = graphData[i];

				g.setColor(new Color(0, 128, 0));
				int y1 = (int) map(prevData, min, max, height - BORDER - 1,
						BORDER);
				int y2 = (int) map(curData, min, max, height - BORDER - 1,
						BORDER);
				int x1 = (int) map(i - 1, 0, dataLength - 1, BORDER, width
						- BORDER - 1);
				int x2 = (int) map(i, 0, dataLength - 1, BORDER, width - BORDER
						- 1);
				prevData = curData;
				if (shouldFFT && !isWavelet) {
					if (i <= 1)
						continue;
					x1 = (int) map(Math.log(i - 1) / maxLog, 0, 1, BORDER,
							width - BORDER - 1);
					x2 = (int) map(Math.log(i) / maxLog, 0, 1, BORDER, width
							- BORDER - 1);
				}
				g.drawLine(x1, y1, x2, y2);
			}
		}
		if (shouldFFT && !isWavelet) {
			int maxIndex = 1;
			for (int i = maxIndex + 1; i < FFTdata.length / 2; i++) {
				if (FFT.getAbs(FFTdata, i) > FFT.getAbs(FFTdata, maxIndex)) {
					maxIndex = i;
				}
			}
			double frequency = FFT.getFreqency(maxIndex, 1000 * expFreq,
					FFTdata.length);
			double angle = Math.toDegrees(FFT.getArgument(FFTdata, maxIndex));

			g.setColor(Color.black);
			g.setFont(g.getFont().deriveFont(20f));
			g.drawString("Наибольшая амплитуда на частоте: " + frequency + "; "
					+ maxIndex, BORDER, g.getFontMetrics().getHeight() + BORDER);
			g.drawString("Сдвиг фаз: " + angle, BORDER, g.getFontMetrics()
					.getHeight() * 2 + BORDER);
			g.drawString("Амплитуда:\t"
					+ ((int) FFT.getAbs(FFTdata, maxIndex) / FFTdata.length),
					BORDER, g.getFontMetrics().getHeight() * 3 + BORDER);
			System.out.format("%.2f; %+.4f >> %.4f\r\n", frequency, angle, max);

		}
	}
}
