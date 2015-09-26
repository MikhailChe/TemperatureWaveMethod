package ru.dolika.experimentAnalyzer;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import ru.dolika.experiment.measurement.Measurement;
import ru.dolika.experiment.measurement.Temperature;
import ru.dolika.experiment.measurement.TemperatureConductivity;
import ru.dolika.experiment.sample.Sample;
import ru.dolika.experimentAnalyzer.zeroCrossing.ZeroCrossing;
import ru.dolika.experimentAnalyzer.zeroCrossing.ZeroCrossingFactory;

public class Batcher implements Callable<Measurement> {

	File file;
	public String result;

	public Batcher(File filename) {
		file = filename;
	}

	public static Measurement compute(File folder, Sample sample) {
		if (!folder.isDirectory())
			return null;
		if (!folder.exists())
			return null;

		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().matches("^[0-9]+.txt$");
			}
		});
		if (files.length <= 0)
			return null;
		BufferedWriter bw;
		File resultFile;
		try {
			resultFile = new File(folder, "���������.tsv");
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
			return null;
		}
		ExecutorService pool = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors() * 2);
		Vector<Future<Measurement>> set = new Vector<Future<Measurement>>();
		ProgressMonitor pm = new ProgressMonitor(null,
				"����� �������������� ������� �����", "", 0, 1);
		pm.setMaximum(files.length);
		for (File f : files) {
			Callable<Measurement> callable = new Batcher(f);
			Future<Measurement> future = pool.submit(callable);
			set.add(future);
		}
		try {
			bw.write("f\t");
			for (int i = 0; i < 4; i++) {
				bw.write("K\tA\tUmax\tphiEdited\t");
			}
			bw.write("\r\n");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		int currentProgress = 0;
		for (Future<Measurement> future : set) {
			try {
				Measurement answer = future.get();
				if (answer != null) {
					sample.measurements.add(answer);
				}
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
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().open(resultFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;

	}

	final static String adjustment = "adjustment";
	final static String DC_cascade = "DCCASCADE";

	// final static String[] SHIFTS = { null, "newAmp.txt", "newAmp.txt",
	// "oldAdjust.txt"};
	// final static String[] SHIFTS = { null, adjustment, adjustment,
	// adjustment};
	final Object[] SHIFTS = { null, DC_cascade,
			ZeroCrossingFactory.forFile("newAmp20150910.txt"), null };

	// final static String[] SHIFTS = { null, adjustment, adjustment, null };

	final static double getSampleLength(int index) {
		return 1.641 / 1000.0;
	}

	public Measurement call() {
		ExperimentReader reader = null;
		Measurement m = new Measurement();

		// Set high priority to read the file
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		try {
			reader = new ExperimentReader(file.toPath());
		} catch (Exception e) {
			e.printStackTrace();
			return m;
		}
		// Set low priority, so that other threads could easily read the file
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		int numCol = reader.getColumnCount();
		if (numCol > 1) {

			final double EXPERIMENT_FREQUENCY = reader.getExperimentFrequency();
			// double[][] singlePeriodSumm = reader.getOnePeriodSumm();
			double[][] croppedData = reader.getCroppedData();
			final int FREQ_INDEX = reader.getCroppedDataPeriodsCount() * 2;
			m.frequency = EXPERIMENT_FREQUENCY;
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
				if (SHIFTS[currentChannel] instanceof ZeroCrossing) {
					ZeroCrossing zc = (ZeroCrossing) SHIFTS[currentChannel];
					currentShift = zc.getCurrentShift(EXPERIMENT_FREQUENCY);
				}
				if (SHIFTS[currentChannel] == DC_cascade) {
					Temperature t = new Temperature();
					t.value = 0;
					m.temperature.add(t);
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
				if (SHIFTS[currentChannel] != DC_cascade
						&& SHIFTS[currentChannel] != adjustment) {
					TemperatureConductivity tCond = new TemperatureConductivity();
					tCond.amplitude = FFT.getAbs(fourierForIndex, 0)
							/ FREQ_INDEX;
					tCond.kappa = kappa;
					tCond.phase = editedAngle;
					tCond.tCond = A;
					m.tCond.add(tCond);
				}
			}
			reader = null;
			System.gc();
		}
		return m;
	}

}
