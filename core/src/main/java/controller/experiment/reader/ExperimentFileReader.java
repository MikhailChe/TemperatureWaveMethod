package controller.experiment.reader;

import static java.nio.file.Files.readAllLines;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class ExperimentFileReader {
	private String[] headerInfo;
	private double experimentFrequecny;
	private double[][] initialData;
	private double[][] croppedData;
	private int croppedDataPeriods = 0;

	private double[] maxValues;
	private double[] minValues;
	private volatile List<Integer> indicies = null;
	private int leastSpace = Integer.MAX_VALUE;

	private final long time;

	public ExperimentFileReader(Path filepath) throws IOException {
		this(filepath, Files.getLastModifiedTime(filepath).toMillis());
	}

	public ExperimentFileReader(Path filepath, long measuredAtMillis) throws IOException {
		List<String> strings = readAllLines(filepath, StandardCharsets.UTF_8);
		time = measuredAtMillis;
		if (strings.size() <= 1)
			throw new IOException("Invalid file.");

		String header = strings.remove(0);

		headerInfo = header.split("\t");
		if (headerInfo.length != 2)
			throw new IOException("Invalid header format");

		int sizeToValidate = parseInteger(headerInfo[0], filepath, 1);

		if (strings.size() != sizeToValidate)
			throw new IOException("Size in the header doesn't match size of file");

		experimentFrequecny = (parseInteger(headerInfo[1], filepath, 1) / 10.0);

		int colNum = strings.get(0).split("\t").length;

		initialData = new double[colNum][sizeToValidate];
		maxValues = new double[colNum];
		minValues = new double[colNum];

		Arrays.fill(maxValues, Double.NEGATIVE_INFINITY);
		Arrays.fill(minValues, Double.MAX_VALUE);
		croppedData = null;

		for (int i = 0; i < strings.size(); i++) {
			String line = strings.get(i);
			String[] dataSplit = line.split("\t");
			if (dataSplit.length != colNum) {
				throw new IOException("Unexpected channel count on line " + (i + 2) + " of " + filepath);
			}
			for (int j = 0; j < initialData.length; j++) {
				initialData[j][i] = parseInteger(dataSplit[j], filepath, i + 2);
				if (maxValues[j] < initialData[j][i]) {
					maxValues[j] = initialData[j][i];
				}
				if (minValues[j] > initialData[j][i]) {
					minValues[j] = initialData[j][i];
				}
			}
		}
	}

	private static int parseInteger(String value, Path file, int line) throws IOException {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new IOException("Invalid integer on line " + line + " of " + file, e);
		}
	}

	public double getExperimentFrequency() {
		return experimentFrequecny;
	}

	public synchronized double[][] getCroppedData() {
		// TODO: Keep an eye on this
		if (croppedData == null) {
			List<Integer> pulseIndicies = getPulseIndicies();

			int mindiff = IntStream
					.range(0, pulseIndicies.size() - 1)
					.parallel()
					.map(i -> pulseIndicies.get(i + 1) - pulseIndicies.get(i))
					.filter(i -> i > 500)
					.min()
					.orElse(Integer.MAX_VALUE);

			if (mindiff == Integer.MAX_VALUE) {
				throw new IllegalArgumentException("The reference channel contains no complete detectable period");
			}
			int startIndex = pulseIndicies.get(0);
			int stopIndex = pulseIndicies.get(pulseIndicies.size() - 1);
			croppedDataPeriods = (int) Math.round((((double) stopIndex) - ((double) startIndex)) / mindiff);
			croppedData = new double[initialData.length][];
			for (int i = 0; i < croppedData.length; i++) {
				croppedData[i] = Arrays.copyOfRange(initialData[i], startIndex, stopIndex);
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

	private double[] getDataColumn(int channel) {
		if (channel < 0)
			channel = 0;
		if (channel >= initialData.length)
			channel = initialData.length - 1;
		return initialData[channel];
	}

	public int getColumnCount() {
		return initialData.length;
	}

	public List<Integer> getPulseIndicies() {
		if (indicies == null) {
			synchronized (this) {
				if (indicies == null) {
					List<Integer> idcs = new ArrayList<>(100);
					double[] refsignal = getDataColumn(0);
					boolean trigger = false;
					boolean firstSlope = true;
					double threshold = (maxValues[0] + minValues[0]) / 2;

					int lastIndex = -1;
					for (int i = 0; i < refsignal.length; i++) {
						if (firstSlope) {
							if (refsignal[i] > threshold) {
								continue;
							}
							firstSlope = !firstSlope;
						}
						if (refsignal[i] > threshold) {
							if (!trigger) {
								trigger = true;
								idcs.add(i);
								if (lastIndex >= 0) {
									if (i - lastIndex < leastSpace) {
										leastSpace = (i - lastIndex);
									}
								}
							}
						} else {
							if (trigger) {
								lastIndex = idcs.get(idcs.size() - 1);
								trigger = false;
							}
						}
					}
					indicies = Collections.unmodifiableList(idcs);
				}
			}
		}
		return indicies;
	}

	public long getTime() {
		return time;
	}
}
