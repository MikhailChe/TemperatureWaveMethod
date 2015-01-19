package ru.dolika.fft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ExperimentReader {
	private String[] headerInfo;
	private double experimentFrequecny;
	private double[][] data;

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

		data = new double[colNum][sizeToValidate];
		for (int i = 1; i < strings.size(); i++) {
			String line = strings.get(i);
			String[] dataSplit = line.split("\t");
			for (int j = 0; j < data.length; j++) {
				data[j][i - 1] = Integer.parseInt(dataSplit[j]);
			}
		}
		strings.clear();
		System.gc();
	}

	public double getExperimentFrequency() {
		return experimentFrequecny;
	}

	public double[][] getData() {
		return data;
	}

	public double[] getDataColumn(int index) {
		if (index < 0)
			index = 0;
		if (index >= data.length)
			index = data.length - 1;
		return data[index];
	}

	public int getColumnCount() {
		return data.length;
	}
}
