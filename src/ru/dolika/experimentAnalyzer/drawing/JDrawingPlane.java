package ru.dolika.experimentAnalyzer.drawing;

import javax.swing.JScrollPane;

public class JDrawingPlane extends JScrollPane {

	private double[][] graphs = new double[8][];

	public int addGraph(double[] array) {
		for (int i = 0; i < graphs.length; i++) {
			if (graphs[i] == null) {
				graphs[i] = array;
				return i;
			}
		}
		return -1;
	}

	public void removeGraph(int index) {
		graphs[index] = null;
	}

	private JDrawingPlane() {
		super();
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);

	}

	public JDrawingPlane(double[] array) {
		this();
		addGraph(array);
		setViewportView((new JGraphImagePlane(graphs)));
	}

	public JDrawingPlane(double[][] arrays) {
		this();
		for (int i = 0; i < arrays.length; i++) {
			if (addGraph(arrays[i]) < 0) {
				break;
			}
		}
		setViewportView((new JGraphImagePlane(graphs)));
	}
}
