package ru.dolika.experimentAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class ExperimentReader {
	private String[] headerInfo;
	private double experimentFrequecny;
	private double[][] initialData;
	private double[][] croppedData;
	private int croppedDataPeriods = 0;
	private double[][] onePeriodData;
	private double onePeriodLength;
	private long onePeriodLengthSumm = 0;
	private int onePeriodLengthCount = 0;
	private double[] maxValues;
	private double[] minValues;
	private Vector<Integer> indicies = null;
	private int leastSpace = Integer.MAX_VALUE;

	public ExperimentReader(Path filepath) throws IOException {
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

		initialData = new double[colNum][sizeToValidate];
		maxValues = new double[colNum];
		minValues = new double[colNum];
		Arrays.fill(maxValues, Double.MIN_VALUE);
		Arrays.fill(minValues, Double.MAX_VALUE);
		onePeriodData = null;
		croppedData = null;
		for (int i = 1; i < strings.size(); i++) {
			String line = strings.get(i);
			String[] dataSplit = line.split("\t");
			for (int j = 0; j < initialData.length; j++) {
				initialData[j][i - 1] = Integer.parseInt(dataSplit[j]);
				if (maxValues[j] < initialData[j][i - 1]) {
					maxValues[j] = initialData[j][i - 1];
				}
				if (minValues[j] > initialData[j][i - 1]) {
					minValues[j] = initialData[j][i - 1];
				}
			}
		}
		strings.clear();
	}

	public double getExperimentFrequency() {
		return experimentFrequecny;
	}

	public synchronized double[][] getCroppedData() {
		if (croppedData == null) {
			croppedData = new double[initialData.length][];
			int[] indicies = getPulseIndicies();

			int startIndex = indicies[0];
			int stopIndex = indicies[indicies.length - 1];
			croppedDataPeriods = 1;
			int length = (stopIndex - startIndex) / croppedDataPeriods;
			while (length > 2010) {
				croppedDataPeriods++;
				length = (stopIndex - startIndex) / croppedDataPeriods;
			}

			for (int i = 0; i < croppedData.length; i++) {

				croppedData[i] = Arrays.copyOfRange(initialData[i], startIndex,
						stopIndex);
			}
		}
		return croppedData;
	}

	public int getCroppedDataPeriodsCount() {
		if (croppedData == null) {
			getCroppedData();
		}
		return croppedDataPeriods;
	}

	public double[][] getInitialData() {
		return initialData;
	}

	public double[] getDataColumn(int channel) {
		if (channel < 0)
			channel = 0;
		if (channel >= initialData.length)
			channel = initialData.length - 1;
		return initialData[channel];
	}

	public int getColumnCount() {
		return initialData.length;
	}

	public double getOnePeriodLength() {
		if (onePeriodData == null) {
			getOnePeriodSumm();
		}
		return onePeriodLength;
	}

	public double[][] getOnePeriodSumm() {
		if (onePeriodData != null) {
			return onePeriodData;
		}

		onePeriodLength = (double) onePeriodLengthSumm
				/ (double) onePeriodLengthCount;

		onePeriodData = new double[initialData.length][leastSpace];
		for (int pulseArrayIndex = 0; pulseArrayIndex < indicies.size() - 1; pulseArrayIndex++) {
			int curIndex = indicies.get(pulseArrayIndex);
			for (int channel = 0; channel < initialData.length; channel++) {
				for (int summIndex = 0; summIndex < onePeriodData[channel].length; summIndex++) {
					onePeriodData[channel][summIndex] += initialData[channel][summIndex
							+ curIndex];
				}
			}
		}
		return onePeriodData;
	}

	public int[] getPulseIndicies() {
		if (indicies == null) {
			indicies = new Vector<Integer>(100);
			double[] refsignal = getDataColumn(0);
			boolean trigger = false;
			double threshold = (maxValues[0] + minValues[0]) / 2;

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
		}
		Integer[] inds = (Integer[]) indicies.toArray(new Integer[indicies
				.size()]);
		int[] outinds = new int[inds.length];
		int i = 0;
		for (int value : inds) {
			outinds[i++] = value;
		}
		return outinds;
	}
}
