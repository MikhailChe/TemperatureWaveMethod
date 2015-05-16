package ru.dolika.experimentAnalyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
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
import javax.swing.ProgressMonitor;

import ru.dolika.experimentAnalyzer.zeroCrossing.ZeroCrossingFactory;
import ru.dolika.experimentAnalyzer.zeroCrossing.ZeroCrossingFactory.ZeroCrossing;

public class Batcher implements Callable<String> {

	File file;
	public String result;

	public Batcher(File filename) {
		file = filename;
	}

	public static void compute(File folder) {
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
		ProgressMonitor pm = new ProgressMonitor(null,
				"Папка обрабатывается слишком долго", "", 0, 1);
		pm.setMaximum(files.length);
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
		int currentProgress = 0;
		for (Future<String> future : set) {
			try {
				String answer = future.get();
				pm.setProgress(++currentProgress);
				bw.write(String.format("%s%n", answer));
			} catch (InterruptedException | ExecutionException | IOException e) {
				e.printStackTrace();
			}
		}
		pm.close();
		pool.shutdown();

		try {
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	final static double[] oldAdjust06032015 = new double[] { 0, 350.04, 35.84,
			53.26, 62.35, 67.59, 71.38, 74.06, 76.14, 77.75, 79.06, 80.04,
			80.93, 81.87, 82.62, 83.13, 83.81, 84.38, 85.13, 85.53, 86.16,
			86.73, 87.08, 87.62, 87.95, 88.53, 88.95, 89.42, 89.95, 90.41 };
	final static double[] oldAdjust = new double[] { 0, 39.72, 39.72, 55.8,
			64.49, 69.81, 73.28, 76.5, 78.2, 79.69, 81.32, 82.44, 82.93, 83.77,
			84.41, 85.26, 86.2, 86.84, 87.67, 88.01, 87.98, 88.12 };

	final static double[] newAdjustCrossZero = new double[] { 0, 87.48, 87.48,
			87.48, 87.48, 87.48, 87.48, 87.48, 87.48, 87.48, 87.48, 87.48,
			87.48, 87.48, 87.48, 87.48, 87.48, 87.48, 87.48, 87.48, 87.48,
			87.48, 87.48, 87.48, 87.48, 87.48, 87.48, 87.48, 87.48, 87.48,
			87.48, 87.48, 87.48, 87.48, 87.48, 87.48, 87.48, 87.48, 87.48,
			87.48, 87.48, 87.48, 87.48, 87.48, 87.48, 87.48, };
	final static double[] newAdjust = new double[] { 0, 87.58, 88.13, 88.11,
			88.27, 88.27, 88.55, 88.62, 88.51, 88.62, 88.71, 88.53, 88.69,
			88.66, 88.59, 88.85, 88.74, 88.71, 88.81, 88.85, 88.88, 88.96,
			88.93, 88.98, 88.96, 88.91, 89.01, 89.02, 89.13, 89.27 };

	final static double[] newAdjust0to20 = new double[] { 0, 89.90, 89.9,
			90.03, 90.09333333, 90.19333333, 90.22333333, 90.33666667, 90.37,
			90.36333333, 90.35333333, 90.34666667, 90.36, 90.33, 90.31333333,
			90.33, 90.32333333, 90.38333333, 90.37333333, 90.43333333, 90.37,
			90.27 };
	final static String adjustment = "";

	final static String[] SHIFTS = { null, "newAmp.txt", "newAmp.txt"};

	final static double getSampleLength(int index) {
		return 0.935 / 1000.0;
	}

	public String call() {
		ExperimentReader reader = null;
		// Set high priority to read the file
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		try {
			reader = new ExperimentReader(file.toPath());
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		// Set low priority, so that other threads could easily read the file
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		int numCol = reader.getColumnCount();
		if (numCol > 1) {

			final double EXPERIMENT_FREQUENCY = reader.getExperimentFrequency();

			StringBuilder sb = new StringBuilder();

			// double[][] singlePeriodSumm = reader.getOnePeriodSumm();
			double[][] croppedData = reader.getCroppedData();
			final int FREQ_INDEX = reader.getCroppedDataPeriodsCount() * 2;

			sb.append(String.format(Locale.getDefault(), "%4.1f\t",
					EXPERIMENT_FREQUENCY));
			for (int currentChannel = 1; currentChannel < Math.min(numCol,
					SHIFTS.length); currentChannel++) {
				if (SHIFTS[currentChannel] == null)
					continue;
				// double[] col2S = singlePeriodSumm[currentChannel];
				double[] col2S = croppedData[currentChannel];
				double[] fourierForIndex = FFT.getFourierForIndex(col2S,
						FREQ_INDEX);
				double signalAngle = FFT.getArgument(fourierForIndex, 0);
				double targetAngle = -signalAngle;
				double omega = 2 * Math.PI * EXPERIMENT_FREQUENCY;
				double currentShift = getCurrentShift(currentChannel,
						EXPERIMENT_FREQUENCY);
				double adjustAngle = targetAngle - Math.toRadians(currentShift);
				double editedAngle = adjustAngle - Math.PI / 4.0;

				while (targetAngle < 0) {
					targetAngle += Math.PI * 2.0;
				}
				while (targetAngle > 2.0 * Math.PI) {
					targetAngle -= Math.PI * 2.0;
				}

				double sineTargetAngle = targetAngle - Math.PI / 2;
				while (sineTargetAngle < Math.PI * 2.0) {
					sineTargetAngle += Math.PI * 2.0;
				}
				while (sineTargetAngle > 0) {
					sineTargetAngle -= Math.PI * 2.0;
				}

				while (editedAngle < 0)
					editedAngle += Math.PI * 2;

				while (editedAngle > 2 * Math.PI)
					editedAngle -= Math.PI * 2;

				double kappa = Math.sqrt(2) * (editedAngle);

				double A = (omega * getSampleLength(currentChannel) * getSampleLength(currentChannel))
						/ (kappa * kappa);

				if (SHIFTS[currentChannel] == adjustment) {
					sb.append(String.format(Locale.getDefault(),
							"%.0f\t%.2f\t%.2f\t",
							FFT.getAbs(fourierForIndex, 0) / 1000,
							Math.toDegrees(targetAngle),
							Math.toDegrees(sineTargetAngle)));
				} else {
					sb.append(String.format(Locale.getDefault(),
							"%.3f\t%.4e\t%.0f\t%.3f\t%.3f\t%.3f\t", kappa, A,
							FFT.getAbs(fourierForIndex, 0) / 1000,
							Math.toDegrees(targetAngle),
							Math.toDegrees(sineTargetAngle),
							Math.toDegrees(editedAngle)));
				}

			}
			reader = null;
			System.gc();
			return sb.toString();
		}
		return "";
	}

	public static double getCurrentShift(final int channel,
			final double frequency) {
		if (channel > SHIFTS.length || channel < 0) {
			throw new ArrayIndexOutOfBoundsException(channel);
		}
		if (SHIFTS[channel] == null || SHIFTS[channel].length() == 0) {
			return 0;
		}
		ZeroCrossing zc;
		try {
			zc = ZeroCrossingFactory.forFile(SHIFTS[channel]);
			return zc.getCurrentShift(frequency);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
