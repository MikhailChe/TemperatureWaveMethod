package ru.dolika.experiment.Analyzer.drawing;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;

public class JDrawingPlane extends JScrollPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1039411952928965988L;
	List<double[]> graphs = new ArrayList<>();
	JGraphImagePlane graphPlane = null;

	public boolean addGraph(double[] array) {
		return graphs.add(array);
	}

	public void removeGraph(int index) {
		graphs.remove(index);
	}

	private JDrawingPlane() {
		super();
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
	}

	public JDrawingPlane(double[] array) {
		this();
		addGraph(array);

		setViewportView((new JGraphImagePlane((double[][]) graphs.toArray(new double[graphs.size()][]))));
	}

	public JDrawingPlane(double[][] arrays) {
		this();
		for (int i = 0; i < arrays.length; i++) {
			if (!addGraph(arrays[i])) {
				break;
			}
		}
		setViewportView((new JGraphImagePlane((double[][]) graphs.toArray(new double[graphs.size()][]))));
	}
}
