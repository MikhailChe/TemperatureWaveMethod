package ru.dolika.fft;

import java.util.Vector;

public class SignalAdder {
	public static double[] getOnePeriod(double[] sig1, double[] sig2) {
		double pMinVal = Integer.MAX_VALUE;
		double pMaxVal = Integer.MIN_VALUE;
		for (int i = 0; i < sig1.length; i++) {
			if (sig1[i] > pMaxVal) {
				pMaxVal = sig1[i];
			}
			if (sig1[i] < pMinVal) {
				pMinVal = sig1[i];
			}
		}
		Vector<Integer> indicies = new Vector<Integer>(50);
		boolean trigger = false;
		double threshold = pMinVal + (pMaxVal - pMinVal) * 0.5;
		int leastSpace = Integer.MAX_VALUE;
		int lastIndex = -1;

		for (int i = 0; i < sig1.length; i++) {
			if (sig1[i] > threshold) {
				if (!trigger) {
					trigger = true;
					indicies.add(i);
					if (lastIndex >= 0) {
						if (i - lastIndex < leastSpace) {
							leastSpace = (i - lastIndex);
						}
					}
				}
			} else {
				if (trigger) {
					lastIndex = indicies.lastElement();
					trigger = false;
				}
			}
		}

		if (leastSpace > 1000) {
			leastSpace = 1000;
		} else if (leastSpace % 2 == 1) {
			leastSpace--;
		}

		indicies.remove(indicies.size() - 1);
		double dataS[] = new double[leastSpace];
		for (int i = 0; i < indicies.size(); i++) {
			int curIndex = indicies.get(i);
			for (int j = 0; j < dataS.length; j++) {
				dataS[j] = sig2[j + curIndex];
			}
		}

		indicies.clear();
		return dataS;
	}
}
