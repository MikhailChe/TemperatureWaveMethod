package ru.dolika.fft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class ExperimentReader {
	private String[] headerInfo;
	private double experimentFrequecny;
	private double[][] data;
	private double[][] onePeriodData;
	private double onePeriodLength;
	private double[] maxValues;
	private double[] minValues;

	public ExperimentReader(Path filepath) throws IOException {
		Profiler.getInstance().startProfiler();
		List<String> strings = null;

		strings = Files.readAllLines(filepath);

		if (strings == null) {
			System.err.println("couldn't load file content or file is empty");
			return;
		}
		if (strings.size() <= 1) {
			System.err.println("Invalid file.");
			return;
		}
		String header = strings.get(0);
		headerInfo = header.split("\t");
		if (headerInfo.length != 2) {
			System.err.println("Invalid header format");
			System.exit(3);
			return;
		}
		int sizeToValidate = Integer.parseInt(headerInfo[0]);
		if (strings.size() - 1 != sizeToValidate) {
			System.err.println("Size in the header doesn't match size of file");
			System.exit(4);
			return;
		}

		experimentFrequecny = (Integer.parseInt(headerInfo[1]) / 10.0);

		int colNum = strings.get(1).split("\t").length;

		data = new double[colNum][sizeToValidate];
		maxValues = new double[colNum];
		minValues = new double[colNum];
		Arrays.fill(maxValues, Double.MIN_VALUE);
		Arrays.fill(minValues, Double.MAX_VALUE);
		onePeriodData = null;
		for (int i = 1; i < strings.size(); i++) {
			String line = strings.get(i);
			String[] dataSplit = line.split("\t");
			for (int j = 0; j < data.length; j++) {
				data[j][i - 1] = Integer.parseInt(dataSplit[j]);
				if (maxValues[j] < data[j][i - 1]) {
					maxValues[j] = data[j][i - 1];
				}
				if (minValues[j] > data[j][i - 1]) {
					minValues[j] = data[j][i - 1];
				}
			}
		}
		strings.clear();
		Profiler.getInstance().stopProfiler();
	}

	public double getExperimentFrequency() {
		return experimentFrequecny;
	}

	public double[][] getData() {
		return data;
	}

	public double[] getDataColumn(int channel) {
		if (channel < 0)
			channel = 0;
		if (channel >= data.length)
			channel = data.length - 1;
		return data[channel];
	}

	public int getColumnCount() {
		return data.length;
	}

	public double getOnePeriodLength() {
		if (onePeriodData == null) {
			getOnePeriod();
		}
		return onePeriodLength;
	}

	public double[][] getOnePeriod() {
		if (onePeriodData != null) {
			return onePeriodData;
		}
		double[] refsignal = getDataColumn(0);
		Vector<Integer> indicies = new Vector<Integer>(100);
		boolean trigger = false;
		double threshold = 5000;
		int leastSpace = Integer.MAX_VALUE;
		long onePeriodLengthSumm = 0;
		int onePeriodLengthCount = 0;
		int lastIndex = -1;
		for (int i = 0; i < refsignal.length; i++) {
			if (refsignal[i] > threshold) {
				if (!trigger) {
					trigger = true;
					indicies.add(i);
					if (lastIndex >= 0) {
						if (i - lastIndex < leastSpace) {
							leastSpace = (i - lastIndex);
							onePeriodLengthSumm += (i - lastIndex);
							onePeriodLengthCount++;
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
		onePeriodLength = (double) onePeriodLengthSumm
				/ (double) onePeriodLengthCount;

		if (leastSpace > 3000) {
			leastSpace = 3000;
		}/*
		 * else if (leastSpace % 2 == 1) { leastSpace--; }
		 */

		indicies.remove(indicies.size() - 1);
		onePeriodData = new double[data.length][leastSpace];
		for (int pulseArrayIndex = 0; pulseArrayIndex < indicies.size(); pulseArrayIndex++) {
			int curIndex = indicies.get(pulseArrayIndex);
			for (int channel = 0; channel < data.length; channel++) {
				for (int summIndex = 0; summIndex < onePeriodData[channel].length; summIndex++) {
					onePeriodData[channel][summIndex] = data[channel][summIndex
							+ curIndex];
				}
			}
		}
		indicies.clear();
		return onePeriodData;
	}
}
