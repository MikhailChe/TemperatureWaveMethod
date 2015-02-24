package ru.dolika.fft;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;

public class Batcher implements Callable<String> {

	File file;
	public String result;

	public Batcher(File filename) {
		file = filename;
	}

	// @onreturn Profiler.getInstance().stopProfiler();
	public static void compute(File folder) {
		// Profiler.getInstance().startProfiler();
		if (!folder.isDirectory())
			return;
		if (!folder.exists())
			return;

		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().matches("^[0-9]+.txt$");
			}
		});
		if (files.length <= 0)
			return;
		BufferedWriter bw;
		File resultFile;
		try {
			resultFile = new File(folder, "результат.tsv");
			if (resultFile.exists()) {
				boolean exception = false;
				do {
					exception = false;
					try {
						Files.delete(resultFile.toPath());
					} catch (java.nio.file.FileSystemException e) {
						exception = true;
						JOptionPane.showMessageDialog(null,
								resultFile.toString(), "Close the file!!!",
								JOptionPane.ERROR_MESSAGE);
						System.err.println("Please, close the file: "
								+ resultFile.toString());
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				} while (exception);
			}
			bw = Files.newBufferedWriter(resultFile.toPath(),
					StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		ExecutorService pool = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors() * 2);
		Vector<Future<String>> set = new Vector<Future<String>>();
		for (File f : files) {
			Callable<String> callable = new Batcher(f);
			Future<String> future = pool.submit(callable);
			set.add(future);
		}
		try {
			bw.write("f\t");
			for (int i = 0; i < 4; i++) {
				bw.write("K\tA\tUmax\tphiTarget\tphiAdjust\tphiEdited\t");
			}
			bw.write("\r\n");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		for (Future<String> future : set) {
			try {
				String answer = future.get();
				bw.write(String.format("%s%n", answer));
			} catch (InterruptedException | ExecutionException | IOException e) {
				e.printStackTrace();
			}
		}
		pool.shutdown();

		try {
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Profiler.getInstance().stopProfiler();
		/*
		 * if (Desktop.isDesktopSupported()) { try {
		 * Desktop.getDesktop().open(resultFile);
		 * 
		 * } catch (IOException e) { e.printStackTrace(); } }
		 */
	}

	final static double[] oldAdjust = new double[] { 0, -30.28125, 22.45054,
			42.31053, 52.66231, 57.94369, 61.25883, 63.19492, 65.91531,
			66.59776, 67.54032, 68.92635, 69.68961, 69.81704, 71.40068,
			70.82494, 72.1481, 72.75135, 72.83383, 74.10869, 73.81043,
			74.90718, 73.45702, 72.85313, 74.73567, 75.30558, 74.34102,
			77.86631, 77.71087, 78.22714, 79.71899 };
	final static double[] newAdjust = new double[] { 0, 75.18617, 75.71124,
			76.25895, 76.12594, 76.31025, 75.96452, 76.08309, 76.43141,
			76.50549, 76.72915, 77.74432, 77.32127, 77.24342, 76.88853,
			76.40208, 77.10222, 77.0721, 76.88899, 77.56667, 78.07193,
			77.74368, 76.24314, 76.58774, 76.23381, 78.21338, 76.15771,
			79.31749, 78.52636, 78.72754, 79.91789 };
	final static double[] newAdjustnewFilter = new double[] { 0, 71.02, 71.97,
			72.03, 72.22, 72.06, 72.4, 72.62, 72.38, 72.84, 73.32, 73.31,
			73.16, 73.43, 73.71, 73.96, 73.89, 74.07, 73.67, 74.23, 74.29,
			74.53, 74.64, 75.11, 75.22, 75.27, 75.93, 75.75, 76.62, 76.89,
			76.77 };
	final static double[] newAdjustnewFilterShld = new double[] { 0, 69.68,
			70.36, 70.13, 70.67, 71.11, 71.21, 72.75, 70.8, 72.3, 71.99, 74.06,
			72.73, 71.27, 73.26, 71.75, 73, 72.93, 74.72, 73.79, 73.46, 72.72,
			73.22, 74.48, 73.05, 74.38, 73.35, 74.25, 72.08, 77.03, 78.83 };

	final static double[][] SHIFTS = { null, newAdjustnewFilterShld,
			newAdjustnewFilterShld, null, null };
	final static double sampleLength = (0.935) / 1000.0;

	public String call() {
		Profiler.getInstance().startProfiler();
		ExperimentReader reader = null;
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		try {
			reader = new ExperimentReader(file.toPath());
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		int numCol = reader.getColumnCount();
		if (numCol > 1) {

			final double EXPERIMENT_FREQUENCY = reader.getExperimentFrequency();
			final int FREQ_INDEX = 2;

			StringBuilder sb = new StringBuilder();
			double[][] singlePeriod = reader.getOnePeriod();

			sb.append(String.format(Locale.getDefault(), "%4.1f\t",
					EXPERIMENT_FREQUENCY));
			for (int currentChannel = 1; currentChannel < Math.min(numCol,
					SHIFTS.length); currentChannel++) {
				if (SHIFTS[currentChannel] == null)
					continue;
				double[] col2S = singlePeriod[currentChannel];
				double[] fourierForIndex = FFT.getFourierForIndex(col2S,
						FREQ_INDEX);
				double signalAngle = FFT.getArgument(fourierForIndex, 0);
				double targetAngle = -signalAngle;
				double omega = 2 * Math.PI * EXPERIMENT_FREQUENCY;
				double currentShift = getCurrentShift(currentChannel,
						EXPERIMENT_FREQUENCY);
				double adjustAngle = targetAngle - Math.toRadians(currentShift);
				double editedAngle = adjustAngle - Math.PI / 4;

				while (editedAngle < 0)
					editedAngle += Math.PI * 2;

				while (editedAngle > 2 * Math.PI)
					editedAngle -= Math.PI * 2;

				double kappa = Math.sqrt(2) * (editedAngle);

				double A = (omega * sampleLength * sampleLength)
						/ (kappa * kappa);

				sb.append(String.format(Locale.getDefault(),
						"%.3f\t%.2e\t%.0f\t%.2f\t%.2f\t%.2f\t", kappa, A,
						FFT.getAbs(fourierForIndex, 0) / 1000,
						Math.toDegrees(targetAngle),
						Math.toDegrees(adjustAngle),
						Math.toDegrees(editedAngle)));

			}

			Profiler.getInstance().stopProfiler();
			return sb.toString();
		}
		return "";
	}

	public static double getCurrentShift(final int channel,
			final double frequency) {
		if (channel > SHIFTS.length || channel < 0) {
			throw new ArrayIndexOutOfBoundsException(channel);
		}
		if (SHIFTS[channel] == null) {
			return 0;
		}
		double[] SHIFT = SHIFTS[channel];
		double currentShift = 0;
		if (frequency == (int) frequency) {
			int index = (int) frequency;
			if (index >= SHIFT.length)
				index = SHIFT.length - 1;
			currentShift = SHIFT[index];
		} else {
			int i1 = (int) frequency;
			int i2 = (int) (frequency + 0.5);
			if (i1 >= SHIFT.length)
				i1 = SHIFT.length - 1;
			if (i2 >= SHIFT.length)
				i2 = SHIFT.length - 1;
			double coeff = frequency - (int) frequency;
			currentShift = SHIFT[i1] * (1 - coeff) + SHIFT[i2] * (coeff);
		}
		return currentShift;
	}

}
