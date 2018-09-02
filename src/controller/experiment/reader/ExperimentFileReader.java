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
		List<String> strings = readAllLines(filepath, StandardCharsets.UTF_8);
		time = Files.getLastModifiedTime(filepath).toMillis();
		if (strings == null)
			throw new IOException("Couldn't load file content or file is empty");
		if (strings.size() <= 1)
			throw new IOException("Invalid file.");

		String header = strings.remove(0);

		headerInfo = header.split("\t");
		if (headerInfo.length != 2)
			throw new IOException("Invalid header format");

		int sizeToValidate = Integer.parseInt(headerInfo[0]);

		if (strings.size() != sizeToValidate)
			throw new IOException("Size in the header doesn't match size of file");

		experimentFrequecny = (Integer.parseInt(headerInfo[1]) / 10.0);

		int colNum = strings.get(1).split("\t").length;

		initialData = new double[colNum][sizeToValidate];
		maxValues = new double[colNum];
		minValues = new double[colNum];

		Arrays.fill(maxValues, Double.MIN_VALUE);
		Arrays.fill(minValues, Double.MAX_VALUE);
		croppedData = null;

		IntStream.range(0, strings.size()).parallel().forEach(i -> {
			String line = strings.get(i);
			String[] dataSplit = line.split("\t");
			for (int j = 0; j < initialData.length; j++) {
				initialData[j][i] = Integer.parseInt(dataSplit[j]);
				if (maxValues[j] < initialData[j][i]) {
					maxValues[j] = initialData[j][i];
				}
				if (minValues[j] > initialData[j][i]) {
					minValues[j] = initialData[j][i];
				}
			}
		});
		strings.clear();
	}

	public double getExperimentFrequency() {
		return experimentFrequecny;
	}

	public synchronized double[][] getCroppedData() {
		// TODO: Keep an eye on this
		if (croppedData == null) {
			croppedData = new double[initialData.length][];
			List<Integer> pulseIndicies = getPulseIndicies();

			int mindiff = IntStream
					.range(0, pulseIndicies.size() - 1)
					.parallel()
					.map(i -> pulseIndicies.get(i + 1) - pulseIndicies.get(i))
					.filter(i -> i > 500)
					.min()
					.orElse(Integer.MAX_VALUE);

			int startIndex = pulseIndicies.get(0);
			int stopIndex = pulseIndicies.get(pulseIndicies.size() - 1);
			croppedDataPeriods = (int) Math.round((((double) stopIndex) - ((double) startIndex)) / mindiff);
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
