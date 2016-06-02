package ru.dolika.experiment.Analyzer.drawing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;

public class JGraphImagePlane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3435828605900305971L;
	public static boolean shouldShowIndicies = false;

	class ArraysStats {
		public double minValue = Double.MAX_VALUE;
		public double maxValue = Double.MIN_VALUE;

		public ArraysStats(double[][] arrays) {
			if (arrays == null) {
				return;
			}
			minValue = Arrays.asList(arrays).stream().mapToDouble(arr -> {
				return DoubleStream.of(arr).min().orElse(Double.MAX_VALUE);
			}).min().orElse(Double.MAX_VALUE);

			maxValue = Arrays.asList(arrays).stream().mapToDouble(arr -> {
				return DoubleStream.of(arr).max().orElse(Double.MIN_VALUE);
			}).max().orElse(Double.MIN_VALUE);
		}
	}

	class ArraySelectionContextMenu extends JPopupMenu {
		private static final long serialVersionUID = 8708433669119770570L;
		JCheckBoxMenuItem[] items = null;

		public ArraySelectionContextMenu() {
			items = new JCheckBoxMenuItem[showThisArray.length];
			IntStream.range(0, showThisArray.length).forEach(i -> {
				items[i] = new JCheckBoxMenuItem("������ " + i);
				items[i].setSelected(showThisArray[i]);
				items[i].addActionListener(e -> {
					showThisArray[i] = !showThisArray[i];
				});
				add(items[i]);
			});
		}
	}

	public ArraysStats stats = null;
	public double[][] arrays;
	public boolean[] showThisArray;

	public JGraphImagePlane(double[][] arrays) {
		if (arrays == null) {
			throw new NullPointerException("Arrays are null");
		}
		setBackground(Color.BLACK);
		setForeground(Color.WHITE);
		this.arrays = arrays;
		this.showThisArray = new boolean[arrays.length];
		Arrays.fill(showThisArray, true);
		stats = new ArraysStats(arrays);
		Dimension size = new Dimension(500, 500);
		setPreferredSize(size);
		setSize(size);
		setFocusable(true);
		MyZoomListener listener = new MyZoomListener();
		addMouseWheelListener(listener);
		addMouseListener(new PopupClickListener());
	}

	class PopupClickListener extends MouseAdapter {
		@Override
		public void mousePressed(java.awt.event.MouseEvent e) {
			if (e.isPopupTrigger()) {
				doPop(e);
			}
		}

		@Override
		public void mouseReleased(java.awt.event.MouseEvent e) {
			if (e.isPopupTrigger()) {
				doPop(e);
			}
		}

		private void doPop(MouseEvent e) {
			ArraySelectionContextMenu menu = new ArraySelectionContextMenu();
			menu.show(e.getComponent(), (int) e.getX(), (int) e.getY());
		}
	}

	class MyZoomListener implements MouseWheelListener {

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {

			if ((e.getModifiersEx() & MouseWheelEvent.CTRL_DOWN_MASK) == MouseWheelEvent.CTRL_DOWN_MASK) {
				final double ZOOM_AMOUNT = 1.1;
				int width = JGraphImagePlane.this.getWidth();
				int height = JGraphImagePlane.this.getHeight();
				Dimension oldSize = JGraphImagePlane.this.getSize();
				Dimension newSize = null;
				if (Math.abs(e.getWheelRotation()) > 0) {
					if (e.getWheelRotation() > 0) {
						if (width / ZOOM_AMOUNT > (getParent() == null ? 1024 : getParent().getWidth())) {
							newSize = new Dimension((int) (width / ZOOM_AMOUNT), height);
						}
					} else {
						newSize = new Dimension((int) (width * ZOOM_AMOUNT), height);
					}
					if (newSize != null) {
						JGraphImagePlane.this.setSize(newSize);
						JGraphImagePlane.this.setPreferredSize(newSize);
						if (getParent() instanceof JViewport) {
							JViewport vp = (JViewport) getParent();
							vp.setViewPosition(new Point(
									(int) map(vp.getViewPosition().x, 0, oldSize.getWidth(), 0, newSize.getWidth()),
									vp.getViewPosition().y));
						}
					}
				}
			}

		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Rectangle view = new Rectangle();
		if (getParent() instanceof JViewport) {
			JViewport vp = (JViewport) getParent();
			view = vp.getViewRect();
		} else {
			view = new Rectangle(0, 0, getWidth(), getHeight());
		}
		g2.setColor(getBackground());
		g2.fillRect(view.x, view.y, view.width, view.height);
		for (int i = 0; i < arrays.length; i++) {
			if (arrays[i] == null)
				continue;
			if (!showThisArray[i])
				continue;
			double[] array = arrays[i];
			for (int j = 1; j < array.length; j++) {
				g2.setColor(Color.getHSBColor((float) i / (float) arrays.length, 1f, 1f));
				if (array.length < getWidth()) {
					g2.setStroke(new BasicStroke(2f));
				} else {
					float val = (float) (2.0 * getWidth() / array.length);
					if (val < 0.005) {
						val = 0.005f;
					}
					g2.setStroke(new BasicStroke(val));
				}
				int y1 = (int) Math.round(map(array[j - 1], stats.minValue, stats.maxValue, getHeight(), 0));
				int y2 = (int) Math.round(map(array[j], stats.minValue, stats.maxValue, getHeight(), 0));
				int x1 = (int) Math.round(map(j - 1, 0, array.length, 0, getWidth()));
				int x2 = (int) Math.round(map(j, 0, array.length, 0, getWidth()));

				if (((x1 >= view.x) && (x1 <= (view.x + view.width)) && (y1 >= view.y)
						&& (y1 <= (view.y + view.height)))
						|| ((x2 >= view.x) && (x2 <= (view.x + view.width)) && (y2 >= view.y)
								&& (y2 <= (view.y + view.height)))) {
					g2.drawLine(x1, y1, x2, y2);
					drawAdditionalData(g2, x1, y1, x2, y2, j - 1, j);
				}
			}
		}
		g2.dispose();
	}

	public void drawAdditionalData(Graphics2D g, int x1, int y1, int x2, int y2, int index1, int index2) {
		if (shouldShowIndicies) {
			final double i1fwidth = g.getFontMetrics().stringWidth("9999") * 1.1;
			if (i1fwidth < Math.abs(x2 - x1)) {
				int ny1 = y1;
				if (y1 - g.getFontMetrics().getAscent() < 5) {
					ny1 += g.getFontMetrics().getHeight();
				}
				g.setColor(getForeground());
				g.drawOval(x1 - 1, y1 - 1, 2, 2);
				g.drawString("" + index1, x1, ny1);
			}
		}

	}

	private double map(double val, double min1, double max1, double min2, double max2) {
		return (val - min1) / (max1 - min1) * (max2 - min2) + min2;
	}
}
