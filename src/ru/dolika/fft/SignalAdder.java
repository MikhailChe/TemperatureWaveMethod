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
		Vector<Integer> indicies = new Vector<Integer>();
		boolean trigger = false;
		for (int i = 0; i < sig1.length; i++) {
			if (sig1[i] > pMinVal + (pMaxVal - pMinVal) * 0.5) {
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
		int leastSpace = Integer.MAX_VALUE;
		for (int i = 0; i < indicies.size() - 1; i++) {
			int distance = indicies.get(i + 1) - indicies.get(i);
			if (distance < leastSpace) {
				leastSpace = distance;
			}
		}
		if (leastSpace % 2 == 1) {
			leastSpace--;
		}
		if (leastSpace > 1000) {
			leastSpace = 1000;
		}
		indicies.remove(indicies.size() - 1);
		double dataS[] = new double[leastSpace];
		//double max = 0, min = 0;
		for (int i = 0; i < indicies.size(); i++) {
			int curIndex = indicies.get(i);
			for (int j = 0; j < dataS.length; j++) {
				dataS[j] = sig2[j + curIndex];
				/*
				 * if (i == indicies.size() - 1) { if (j == 0) { max = dataS[j];
				 * min = dataS[j]; } else { if (max < dataS[j]) { max =
				 * dataS[j]; } if (min > dataS[j]) { min = dataS[j]; } } }
				 */
			}
		}
		/*
		 * for (int i = 0; i < dataS.length; i++) { dataS[i] -= (max / 2.0 + min
		 * / 2.0); }
		 */
		indicies.clear();
		return dataS;
	}
}
