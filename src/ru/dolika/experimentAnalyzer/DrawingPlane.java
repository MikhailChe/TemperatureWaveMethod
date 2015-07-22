package ru.dolika.experimentAnalyzer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.jtransforms.fft.DoubleFFT_1D;

public class DrawingPlane extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3432370326222944519L;
	final int BORDER = 5;
	public double data[];
	public int channelNumber = 0;
	public int peakShiftSamples = 0;
	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;
	public static boolean shouldFilter = false;
	public static boolean shouldAFC = false;
	public static int maxHarmony = 1;
	public static boolean shouldShowIndicies = false;
	public static int fftIndex = 100;
	public static boolean shouldShowSineWave = true;

	static double x1perc = 0;
	static double x2perc = 1;
	final static double ZOOM_AMOUNT = 1.25;

	boolean calculateSelection = false;
	int selectionMinIndex = 0;
	int selectionMaxIndex = 0;

	int colNum;

	ExperimentReader thisEreader = null;

	public DrawingPlane() {
		setPreferredSize(new Dimension(640, 480));
		setFocusable(true);
		DrawinPlaneListener listener = new DrawinPlaneListener();
		addMouseMotionListener(listener);
		addMouseListener(listener);
		addMouseWheelListener(listener);
		addKeyListener(listener);
	}

	public void loadData(ExperimentReader ereader, int channel, boolean full) {
		if (ereader != null) {
			thisEreader = ereader;
		} else {
			return;
		}
		if (channel < 0)
			return;
		if (full) {
			this.data = ereader.getCroppedData()[channel];
		} else {
			this.data = ereader.getOnePeriodSumm()[channel];
		}
		colNum = ereader.getColumnCount();
		this.channelNumber = channel;
		DrawingPlane.fftIndex = 1;
		double[] peaks = ereader.getDataColumn(0);
		for (int i = 0; i < Math.min(3000, peaks.length); i++) {
			if (peaks[i] > 5000) {
				this.peakShiftSamples = i;
				break;
			}
		}

		this.repaint();
	}

	private class DrawinPlaneListener implements MouseListener,
			MouseMotionListener, MouseWheelListener, KeyListener {

		boolean selectionStarted = true;
		int selectionStartIndex = -1;
		int selectionStopIndex = -1;
		private int arrayIndex = 0;

		private void setSelectionMaxMin(double Xcoord) {
			double periodLength = thisEreader.getOnePeriodLength() / 2.0;
			selectionStopIndex = getXindexAt((int) Xcoord);
			double diff = selectionStopIndex - selectionStartIndex;
			diff -= diff % periodLength;
			selectionStopIndex = (int) (selectionStartIndex + diff);
			if (selectionStartIndex < selectionStopIndex) {
				selectionMinIndex = selectionStartIndex;
				selectionMaxIndex = selectionStopIndex;
			} else {
				selectionMinIndex = selectionStopIndex;
				selectionMaxIndex = selectionStartIndex;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				if (selectionStarted) {
					setSelectionMaxMin(e.getX());
					repaint();
				}
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				selectionStarted = true;
				selectionStartIndex = getXindexAt(e.getX());
				selectionMinIndex = selectionMaxIndex = selectionStartIndex;
				repaint();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				if (selectionStarted) {
					setSelectionMaxMin(e.getX());
					repaint();
				}
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getWheelRotation() > 0) {
				zoomIn(e.getX(), e.getY());
			} else {
				zoomOut(e.getX(), e.getY());
			}
			repaint();
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		boolean displayFullArray = true;

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				if (arrayIndex < colNum - 1)
					arrayIndex++;
				loadData(thisEreader, arrayIndex, displayFullArray);
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				if (arrayIndex != 0)
					arrayIndex--;
				loadData(thisEreader, arrayIndex, displayFullArray);
			} else if (e.getKeyCode() == KeyEvent.VK_F) {
				DrawingPlane.shouldFilter = !DrawingPlane.shouldFilter;
				repaint();
			} else if (e.getKeyCode() == KeyEvent.VK_Q) {
				DrawingPlane.shouldAFC = !DrawingPlane.shouldAFC;
				repaint();
			} else if (e.getKeyCode() == KeyEvent.VK_S) {
				displayFullArray = false;
				loadData(thisEreader, arrayIndex, displayFullArray);
			} else if (e.getKeyCode() == KeyEvent.VK_N) {
				displayFullArray = true;
				loadData(thisEreader, arrayIndex, displayFullArray);
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				if (DrawingPlane.maxHarmony > 1) {
					DrawingPlane.maxHarmony--;
				}
				repaint();
			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				if (DrawingPlane.maxHarmony < 301) {
					DrawingPlane.maxHarmony++;
				}
				repaint();
			}
		}

	}

	public int getXindexAt(int xpoint) {
		return (int) map(xpoint, 0, getWidth(), getZoomMinimIndex(),
				getZoomMaximIndex());

	}

	public int getXpointAtIndex(int index) {
		return (int) map(index, getZoomMinimIndex(), getZoomMaximIndex(),
				BORDER, getWidth() - BORDER);
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

	public int getZoomMinimIndex() {
		int minim = (int) (data.length * x1perc);
		if (minim < 0) {
			minim = 0;
		}
		return minim;
	}

	public int getZoomMaximIndex() {
		int minim = (int) (data.length * x2perc);
		if (minim < 0) {
			minim = 0;
		}
		return minim;

	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		draw((Graphics2D) g);
	}

	boolean firstDraw = true;

	public void draw(Graphics2D g) {
		if (true) {
			firstDraw = false;
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
		}
		if (data == null)
			return;
		double[] dataCopy = null;
		double[] FFTdata = null;
		double[] SineWaveData = null;

		if (shouldFilter || shouldAFC) {
			FFTdata = Arrays.copyOf(data, data.length * 2);
			DoubleFFT_1D fourier = new DoubleFFT_1D(data.length);
			fourier.realForward(FFTdata);
			System.out.println("makeinf fourier");
			if (shouldFilter) {
				int periods = thisEreader.getCroppedDataPeriodsCount();
				for (int i = 0; i < FFTdata.length; i++) {
					int nearPeriod = ((int) (i / 2)) % periods;

					if (!(nearPeriod == 0)) {
						FFTdata[i] = 0;
					} else if ((int) (i / 2) == 0) {

					}
				}
				dataCopy = Arrays.copyOf(FFTdata, FFTdata.length);
				fourier.complexInverse(dataCopy, true);
				dataCopy = FFT.getAbs(dataCopy);
				findExtreme(dataCopy);
			}
		} else {
			dataCopy = data;
		}
		double signalAngle = 0;
		double signalAmplitude = 0;
		double signalZeroLevel = 0;
		shouldShowSineWave = true;
		if (shouldShowSineWave) {
			SineWaveData = Arrays.copyOf(thisEreader.getCroppedData()[1],
					thisEreader.getCroppedData()[1].length);
			final int FREQ_INDEX = thisEreader.getCroppedDataPeriodsCount() * 2;
			System.out.println("Freq_index = " + FREQ_INDEX);
			double[] fourierForIndex = FFT.getFourierForIndex(SineWaveData,
					FREQ_INDEX);
			signalAngle = FFT.getArgument(fourierForIndex, 0);
			System.out.println("SignalAngle = " + signalAngle);
			signalAmplitude = FFT.getAbs(fourierForIndex, 0);
			signalZeroLevel = FFT.getAbs(fourierForIndex, 0);
		}
		int maxim = getZoomMaximIndex();
		int minim = getZoomMinimIndex();
		double[] graphData = null;
		double[] theSineWave = new double[SineWaveData.length];
		for (int i = 0; i < theSineWave.length; i++) {
			theSineWave[i] = Math.cos((2.0 * Math.PI * ((double) (i - 1)))
					* this.thisEreader.getCroppedDataPeriodsCount()
					/ theSineWave.length + signalAngle)
					* signalAmplitude;
		}

		if (shouldAFC) {
			if (FFTdata == null)
				return;
			System.out.println("FFTdata OK");

			graphData = FFT.getAbs(FFTdata);

			graphData[0] = 0;
		} else {
			graphData = Arrays.copyOfRange(dataCopy, minim, maxim);
			theSineWave = Arrays.copyOfRange(theSineWave, minim, maxim);
		}

		findExtreme(graphData);
		int width = getWidth();
		int height = getHeight();

		if (minim == 0) {
			g.setColor(Color.red);
			g.drawLine(1, 0, 1, height);
		}
		if (maxim == data.length - 1) {
			g.setColor(Color.red);
			g.drawLine(width - 2, 0, width - 2, height);
		}

		int dataLength = graphData.length;

		if (graphData.length < width) {
			g.setStroke(new BasicStroke(2f));
		} else {
			float val = (float) (2.0 * width / graphData.length);
			if (val < 0.005) {
				val = 0.005f;
			}
			g.setStroke(new BasicStroke(val));
		}

		{
			double prevData = 0;
			if (dataLength > 0)
				prevData = graphData[0];
			double maxLog = Math.log(dataLength - 1);
			int zy = (int) map(0, min, max, height - BORDER - 1, BORDER);
			g.setColor(new Color(0, 128, 128, 128));
			g.drawLine(BORDER, zy, width - BORDER - 1, zy);

			for (int i = 1; i < dataLength; i++) {
				double curData = graphData[i];

				int y1 = (int) map(prevData, min, max, height - BORDER - 1,
						BORDER);
				int y2 = (int) map(curData, min, max, height - BORDER - 1,
						BORDER);
				int x1 = (int) map(i - 1, 0, dataLength - 1, BORDER, width
						- BORDER - 1);
				int x2 = (int) map(i, 0, dataLength - 1, BORDER, width - BORDER
						- 1);

				prevData = curData;
				if (shouldAFC) {
					if (i <= 1)
						continue;
					x1 = (int) map(Math.log(i - 1) / maxLog, 0, 1, BORDER,
							width - BORDER - 1);
					x2 = (int) map(Math.log(i) / maxLog, 0, 1, BORDER, width
							- BORDER - 1);
				}
				g.setColor(new Color(0, 128, 0));
				g.drawLine(x1, y1, x2, y2);

				if (shouldShowIndicies && dataLength < getWidth() / 2) {
					g.setColor(Color.RED);
					g.drawOval(x1 - 1, y1 - 1, 2, 2);
					g.setColor(Color.BLUE);
					g.drawString("" + ((i - 1) + minim), x1, y1);
				}

				g.setColor(new Color(128, 0, 0));
				y1 = (int) map(theSineWave[i], min, max, height - BORDER - 1,
						BORDER);
				y2 = (int) map(theSineWave[i], min, max, height - BORDER - 1,
						BORDER);
				g.drawLine(x1, y1, x2, y2);

			}
		}
		if (shouldAFC) {
			double frequency = FFT.getFreqency(fftIndex,
					1000 * thisEreader.getExperimentFrequency(), data.length);
			double angle = Math.toDegrees(FFT.getArgument(FFTdata, fftIndex));

			g.setColor(Color.black);
			g.setFont(g.getFont().deriveFont(20f));
			g.drawString("Наибольшая амплитуда на частоте: " + frequency + "; "
					+ fftIndex, BORDER, g.getFontMetrics().getHeight() + BORDER);
			g.drawString("Сдвиг фаз: " + angle, BORDER, g.getFontMetrics()
					.getHeight() * 2 + BORDER);
			g.drawString("Амплитуда:\t"
					+ ((int) FFT.getAbs(FFTdata, fftIndex) / FFTdata.length),
					BORDER, g.getFontMetrics().getHeight() * 3 + BORDER);
		} else {
			if (selectionMaxIndex != selectionMinIndex) {
				int minCoordX = getXpointAtIndex(selectionMinIndex);
				int maxCoordX = getXpointAtIndex(selectionMaxIndex);
				g.setColor(new Color(0, 0, 255, 64));
				g.fillRect(minCoordX, BORDER, maxCoordX - minCoordX,
						getHeight() - BORDER * 2);

				double[] partFFTdata = Arrays.copyOfRange(data,
						selectionMinIndex, selectionMaxIndex);

				// 1 period = 1000 samples;
				// 2*PI*x/1000 = currentOmega (w)
				double FFTfreq = 1.0 / 1000.0;
				double[] partFFTout = FFT.getFourierForIndex(partFFTdata,
						(int) (FFTfreq * partFFTdata.length));
				double argument = FFT.getArgument(partFFTout, 0);
				double amplitude = FFT.getAbs(partFFTout, 0)
						/ partFFTdata.length;
				double zeroShift = 2.0
						* Math.PI
						* (((selectionMinIndex % 1000) - peakShiftSamples) / 1000.0);
				while (zeroShift <= Math.PI) {
					zeroShift += 2.0 * Math.PI;
				}
				while (zeroShift >= Math.PI) {
					zeroShift -= 2.0 * Math.PI;
				}
				double targetAngle = -(argument - zeroShift);
				double currentShift = Batcher.getCurrentShift(channelNumber,
						thisEreader.getExperimentFrequency());
				double adjustAngle = targetAngle - Math.toRadians(currentShift);
				double editedAngle = adjustAngle - Math.PI / 4;
				while (editedAngle < 0) {
					editedAngle += Math.PI * 2;
				}
				while (editedAngle > 2 * Math.PI) {
					editedAngle -= Math.PI * 2;
				}
				double omega = 2 * Math.PI
						* thisEreader.getExperimentFrequency();
				double kappaSquared = 2 * (editedAngle * editedAngle);
				double A = (omega * Batcher.getSampleLength(channelNumber) * Batcher
						.getSampleLength(channelNumber)) / kappaSquared;
				g.setColor(Color.black);
				g.setFont(g.getFont().deriveFont(20f));
				int fontMetricHeight = g.getFontMetrics().getHeight();
				g.drawString("Температуропроводность: " + A, BORDER,
						fontMetricHeight + BORDER);
				g.drawString("Сдвиг фаз:\t" + Math.toDegrees(editedAngle),
						BORDER, fontMetricHeight * 2 + BORDER);
				g.drawString("нуль-сдвиг:\t" + zeroShift, BORDER,
						fontMetricHeight * 3 + BORDER);
				g.drawString("Амплитуда:\t" + amplitude, BORDER,
						fontMetricHeight * 4 + BORDER);

			}
		}
	}
}
