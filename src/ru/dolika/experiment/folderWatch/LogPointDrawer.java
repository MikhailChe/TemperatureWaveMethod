package ru.dolika.experiment.folderWatch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class LogPointDrawer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8438802012588636854L;

	final double[] points;

	double max = -Double.MIN_VALUE, min = Double.MAX_VALUE;

	public LogPointDrawer(double[] points) {
		super();
		this.points = points;
		for (double point : points) {
			if (point > max)
				max = point;

			if (point < min)
				min = point;
		}
		setPreferredSize(new Dimension(points.length * 2, 600));
		setSize(getPreferredSize());
	}

	private double map(double val, double min1, double max1, double min2, double max2) {
		return (val - min1) / (max1 - min1) * (max2 - min2) + min2;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		System.out.println("paint component");
		final int componentWidth = getWidth();
		final int componentHeight = getHeight();
		g.setColor(Color.black);
		g.fillRect(0, 0, componentWidth, componentHeight);
		g.setColor(Color.DARK_GRAY);
		for (int i = 0; i < 20; i++) {
			g.drawLine(i * componentWidth / 20, 0, i * componentWidth / 20, componentHeight);
		}
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(componentWidth / 2, 0, componentWidth / 2, componentHeight);
		g.drawLine(componentWidth / 4, 0, componentWidth / 4, componentHeight);
		g.drawLine(3 * componentWidth / 4, 0, 3 * componentWidth / 4, componentHeight);
		g.setColor(Color.RED);
		double maxLog = Math.log(max);
		double minLog = Math.log(min);

		for (int i = 0; i < points.length - 1; i++) {
			double x = map(i, 0, points.length, 0, componentWidth);
			double y = map(Math.log(points[i]), minLog, maxLog, componentHeight, 0);
			g.fillOval((int) (x - 2), (int) (y - 2), 4, 4);
		}
	}
}
