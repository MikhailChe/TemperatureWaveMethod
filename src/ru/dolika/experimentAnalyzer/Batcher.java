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

	final static String adjustment = "adjustment";
	final static String DC_cascade = "DCCASCADE";

	// final static String[] SHIFTS = { null, "newAmp.txt", "newAmp.txt",
	// "oldAdjust.txt"};
	// final static String[] SHIFTS = { null, adjustment, adjustment,
	// adjustment};
	final static String[] SHIFTS = { null, DC_cascade, "newAmp20150910.txt",
			null };

	// final static String[] SHIFTS = { null, adjustment, adjustment, null };

	final static double getSampleLength(int index) {
		return 1.641 / 1000.0;
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
				double currentShift = 0;
				if (SHIFTS[currentChannel] != DC_cascade
						&& SHIFTS[currentChannel] != adjustment) {
					currentShift = getCurrentShift(currentChannel,
							EXPERIMENT_FREQUENCY);
				}
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
				} else if (SHIFTS[currentChannel] == DC_cascade) {
					sb.append(String.format(
							Locale.getDefault(),
							"%.3f\t%.4e\t%.0f\t%.3f\t%.3f\t%.3f\t",
							0f,
							0f,
							FFT.getAbs(FFT.getFourierForIndex(col2S, 0), 0) / 1000,
							0f, 0f, 0f));
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
