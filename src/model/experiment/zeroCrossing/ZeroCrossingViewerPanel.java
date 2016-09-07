package model.experiment.zeroCrossing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class ZeroCrossingViewerPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7118964792998797256L;

	private ZeroCrossing shifts;

	public ZeroCrossingViewerPanel(ZeroCrossing zc) {
		super(true);
		this.shifts = zc;
		setPreferredSize(new Dimension(64, 64));
	}

	public ZeroCrossing setZeroCrossing(ZeroCrossing newZC) {
		this.shifts = newZC;
		this.repaint();
		return this.shifts;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		paintGrid(g2d);
		paintComponent(g2d);
	}

	private double map(double val, double min1, double max1, double min2, double max2) {
		return (val - min1) / (max1 - min1) * (max2 - min2) + min2;
	}

	final int MARGIN = 16;

	private void paintGrid(Graphics2D g) {
		final int componentWidth = getWidth();
		final int componentHeight = getHeight();
		g.setColor(new Color(128, 128, 128, 128));
		for (int i = 0; i <= 30; i++) {
			int xScreen = (int) map(i, 0, 30, 0 + MARGIN, componentWidth - MARGIN);
			if (i % 10 == 0) {
				g.setStroke(new BasicStroke(1));
				g.drawString(i + "", xScreen, componentHeight);
			} else {
				g.setStroke(new BasicStroke(0.125f));
			}
			g.drawLine(xScreen, MARGIN, xScreen, componentHeight - MARGIN);

		}

		for (int i = -180; i <= 180; i++) {
			int yScreen = (int) map(i, -180, 180, componentHeight - MARGIN, MARGIN);
			if (i % 45 == 0) {
				g.setStroke(new BasicStroke(1));
				if (i % 180 == 0) {
					g.drawString(i + "", 0, yScreen);
				}
			} else {
				g.setStroke(new BasicStroke(0.125f));
			}
			g.drawLine(MARGIN, yScreen, componentWidth - MARGIN, yScreen);
		}
	}

	private void paintComponent(Graphics2D g) {
		final int componentWidth = getWidth();
		final int componentHeight = getHeight();
		g.setColor(Color.blue);
		for (double freq = 0.1; freq < 30; freq += 0.1) {
			int xScreen = (int) map(freq, 0, 30, 0 + MARGIN, componentWidth - MARGIN);
			int yScreen = componentHeight / 2;
			if (shifts != null) {
				yScreen = (int) map(shifts.getCurrentShift(freq), -180, 180, componentHeight - MARGIN, 0 + MARGIN);
			}

			g.fillOval(xScreen - 2, yScreen - 2, 4, 4);
		}
	}
}
