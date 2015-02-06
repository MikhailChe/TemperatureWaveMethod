package ru.dolika.fft;

import java.io.IOException;
import java.util.Vector;

import javax.swing.JFileChooser;

public class ThisIsJustAtest {
	public static void main(String[] args) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.showOpenDialog(null);

		try {
			ExperimentReader ereader = new ExperimentReader(chooser
					.getSelectedFile().toPath());
			double[][] data = ereader.getData();
			double[] reference = data[0];
			Vector<Integer> indicies = new Vector<Integer>(100);
			{
				boolean trigger = false;
				for (int i = 0; i < reference.length; i++) {
					if (reference[i] > 5000) {
						if (!trigger) {
							trigger = true;
							indicies.add(i);
						}
					} else {
						if (trigger) {
							trigger = false;
						}
					}
				}
			}
			for (int i = 0; i < indicies.size() - 1; i++) {
				int startIndex = indicies.get(i);
				int stopIndex = indicies.get(i + 1);
				for (int j = startIndex; j < stopIndex; j++) {
					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
