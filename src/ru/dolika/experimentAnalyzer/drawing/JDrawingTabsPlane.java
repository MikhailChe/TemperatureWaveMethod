package ru.dolika.experimentAnalyzer.drawing;

import javax.swing.JTabbedPane;

public class JDrawingTabsPlane extends JTabbedPane {

	/*
	 * public static void main(String[] args) { JFrame frame = new JFrame("yo");
	 * frame.setLayout(new BorderLayout(5, 5)); frame.add(new
	 * JDrawingTabsPlane()); frame.pack();
	 * frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	 * frame.setVisible(true); }
	 */

	public JDrawingTabsPlane() {
		super();

	}

	public JDrawingTabsPlane(double[][][] data) {
		this();
		for (int i = 0; i < data.length; i++) {
			addTab("Канал №" + (i + 1), new JDrawingPlane(data[i]));
		}
	}

	public JDrawingTabsPlane(double[][] data) {
		this();
		for (int i = 0; i < data.length; i++) {
			addTab("Канал №" + (i + 1), new JDrawingPlane(data[i]));
		}
	}

	public void addSignalTab(double[][] data, String name) {
		addTab((name == null ? ("Канал №" + (getTabCount())) : name),
				new JDrawingPlane(data));
	}
}
