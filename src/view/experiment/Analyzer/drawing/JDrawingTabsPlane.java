package view.experiment.Analyzer.drawing;

import javax.swing.JTabbedPane;

public class JDrawingTabsPlane extends JTabbedPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4799499926744105296L;

	public JDrawingTabsPlane() {
		super();

	}

	public JDrawingTabsPlane(double[][][] data) {
		this();
		for (int i = 0; i < data.length; i++) {
			addTab("����� �" + (i + 1), new JDrawingPlane(data[i]));
		}
	}

	public JDrawingTabsPlane(double[][] data) {
		this();
		for (int i = 0; i < data.length; i++) {
			addTab("����� �" + (i + 1), new JDrawingPlane(data[i]));
		}
	}

	public void addSignalTab(double[][] data, String name) {
		addTab((name == null ? ("����� �" + (getTabCount())) : name),
				new JDrawingPlane(data));
	}
}
