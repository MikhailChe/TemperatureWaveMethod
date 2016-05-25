package ru.dolika.experiment.measurement;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.border.SoftBevelBorder;

import org.junit.Assert;
import org.junit.Test;

public class MeasurementViewer extends JPanel {
	private static final long serialVersionUID = 3555290921726804677L;
	final ArrayList<Measurement> measurements;

	public MeasurementViewer() {
		super();
		measurements = new ArrayList<Measurement>();
		Dimension d = new Dimension(640, 480);
		setPreferredSize(d);
		setSize(d);
		setMinimumSize(d);
		setMaximumSize(d);
		setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
	}

	public void addMeasurement(Measurement m) {
		if (m == null || m.temperature == null || m.tCond == null)
			throw new NullPointerException();
		if (m.temperature.size() == 0)
			return;
		if (m.tCond.size() == 0)
			return;
		if (m.temperature.get(0).value > maxTemp) {
			maxTemp = m.temperature.get(0).value;
		}
		if (m.temperature.get(0).value < minTemp) {
			minTemp = m.temperature.get(0).value;
		}
		for (TemperatureConductivity tc : m.tCond) {
			if (tc.tCond > maxTcond)
				maxTcond = tc.tCond;
			if (tc.tCond < minTcond)
				minTcond = tc.tCond;
		}
		measurements.add(m);
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		paintComponent(g2d);
	}

	final public double sanityMaxTcond = 3E-5;
	final public double sanityMinTcond = 1E-6;

	double minTemp = 9999, maxTemp = 0, minTcond = 1E-4, maxTcond = 0;

	private double map(double val, double min1, double max1, double min2, double max2) {
		return (val - min1) / (max1 - min1) * (max2 - min2) + min2;
	}

	public void paintComponent(Graphics2D g) {
		paintGrid(g);
		paintDots(g);

	}

	public void paintGrid(Graphics2D g) {
		double optimalTemperatureTick = getOptimalTemperatureTick();
		double optimalTcondTick = getOptimalTcondTick();
		paintLabels(g);
	}

	private double getOptimalTcondTick() {
		double dt = getSanityMaxTcond() - getSanityMinTcond();

		Math.scalb(1, Math.getExponent(dt));
		return 0;
	}

	@Test
	public void getOptimalTCondTickTest() {
		double expectedMagnitude = 1.27;
		int expectedExponent = -2;
		double value = 1.27E-2;

		String valStrSubdiv[] = String.format("%E", value).split("E");
		double magnitude = Double.valueOf(valStrSubdiv[0].replace(',', '.'));
		int exponent = Integer.valueOf(valStrSubdiv[1]);

		Assert.assertEquals(expectedMagnitude, magnitude, 1E-2);
		Assert.assertEquals(expectedExponent, exponent);
	}

	private double getOptimalTemperatureTick() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void paintLabels(Graphics2D g) {

	}

	public void paintDots(Graphics2D g) {
		final int componentWidth = getWidth();
		final int componentHeight = getHeight();
		double scMinTcond = getSanityMinTcond();
		double scMaxTcond = getSanityMaxTcond();
		for (Measurement mes : measurements) {
			int xScreen = (int) map(mes.temperature.get(0).value, minTemp, maxTemp, 0.1 * componentWidth,
					0.9 * componentWidth);
			for (int i = 0; i < mes.tCond.size(); i++) {
				TemperatureConductivity tc = mes.tCond.get(i);
				int yScreen = (int) map(tc.tCond, scMinTcond, scMaxTcond, 0.9 * componentHeight, 0.1 * componentHeight);
				g.setColor(Color.getHSBColor((float) i / (float) mes.tCond.size(), 1, 1));
				g.fillOval(xScreen - 3, yScreen - 3, 6, 6);
			}
		}
	}

	private double getSanityMaxTcond() {
		return Math.min(sanityMaxTcond, maxTcond);
	}

	private double getSanityMinTcond() {
		return Math.max(sanityMinTcond, minTcond);
	}
}
