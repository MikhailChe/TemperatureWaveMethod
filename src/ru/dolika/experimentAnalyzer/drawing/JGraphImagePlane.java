package ru.dolika.experimentAnalyzer.drawing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class JGraphImagePlane extends JPanel {

	private static class ArraysStats {
		public double minValue = Double.MAX_VALUE;
		public double maxValue = Double.MIN_VALUE;
		public int maxArraysLength = 0;

		private ArraysStats() {

		}

		public ArraysStats(double[][] arrays) {
			if (arrays == null) {
				return;
			}

			for (int i = 0; i < arrays.length; i++) {
				if (arrays[i] == null)
					continue;
				if (maxArraysLength < arrays[i].length) {
					maxArraysLength = arrays[i].length;
				}
				for (int j = 0; j < arrays[i].length; j++) {
					if (minValue > arrays[i][j]) {
						minValue = arrays[i][j];
					}
					if (maxValue < arrays[i][j]) {
						maxValue = arrays[i][j];
					}
				}
			}
		}
	}

	public BufferedImage internalImage = null;
	public ArraysStats stats = null;

	public JGraphImagePlane(double[][] arrays) {
		if (arrays == null) {
			throw new NullPointerException("Arrays are null");
		}

		stats = new ArraysStats(arrays);
		internalImage = new BufferedImage(stats.maxArraysLength * 10, 1024,
				BufferedImage.TYPE_INT_RGB);

		Dimension size = new Dimension(internalImage.getWidth(),
				internalImage.getHeight());
		setPreferredSize(size);
		setSize(size);
		setMaximumSize(size);
		setMinimumSize(size);

		paintImage(arrays);

	}

	@Override
	public void paintComponent(Graphics g) {
		Dimension size = new Dimension(internalImage.getWidth(),
				internalImage.getHeight());
		setPreferredSize(size);
		setSize(size);
		g.drawImage(internalImage, 0, 0, null);
	}

	public void paintImage(double[][] arrays) {
		if (arrays == null)
			return;
		Graphics g = internalImage.getGraphics().create();
		g.clearRect(0, 0, internalImage.getWidth(), internalImage.getHeight());
		for (int i = 0; i < arrays.length; i++) {
			if (arrays[i] == null)
				continue;
			double[] array = arrays[i];
			g.setColor(Color.getHSBColor((float) i / (float) arrays.length, 1f,
					1f));
			for (int j = 1; j < array.length; j++) {
				int y1 = (int) Math.round(map(array[j - 1], stats.minValue,
						stats.maxValue, 0, internalImage.getHeight()));
				int y2 = (int) Math.round(map(array[j], stats.minValue,
						stats.maxValue, 0, internalImage.getHeight()));
				int x1 = (int) Math.round(map(j - 1, 0, array.length, 0,
						internalImage.getWidth()));
				int x2 = (int) Math.round(map(j, 0, array.length, 0,
						internalImage.getWidth()));
				g.drawLine(x1, y1, x2, y2);
			}
		}
		g.dispose();
	}

	private double map(double val, double min1, double max1, double min2,
			double max2) {
		return (val - min1) / (max1 - min1) * (max2 - min2) + min2;
	}
}
