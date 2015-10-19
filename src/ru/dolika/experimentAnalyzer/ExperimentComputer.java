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
import ru.dolika.experiment.workspace.Workspace;
import ru.dolika.experimentAnalyzer.signalID.AdjustmentSignalID;
import ru.dolika.experimentAnalyzer.signalID.BaseSignalID;
import ru.dolika.experimentAnalyzer.signalID.DCsignalID;
import ru.dolika.experimentAnalyzer.signalID.SignalIdentifier;
import ru.dolika.experimentAnalyzer.zeroCrossing.ZeroCrossing;

public class ExperimentComputer implements Callable<Measurement> {

	public double truncatePositive(double value) {
		while (value < 0) {
			value += Math.PI * 2.0;
		}
		while (value > Math.PI * 2.0) {
			value -= Math.PI * 2.0;
		}
		return value;
	}

	public static Measurement computeFolder(File folder, Workspace workspace) {
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
			resultFile = new File(folder, "result.tsv");
			if (resultFile.exists()) {
				boolean exception = false;
				do {
					exception = false;
					try {
						Files.delete(resultFile.toPath());
					} catch (java.nio.file.FileSystemException e) {
						exception = true;
						JOptionPane.showMessageDialog(null, resultFile.toString(), "Close the file!!!",
								JOptionPane.ERROR_MESSAGE);
						System.err.println("Please, close the file: " + resultFile.toString());
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				} while (exception);
			}
			bw = Files.newBufferedWriter(resultFile.toPath(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
		Vector<Future<Measurement>> set = new Vector<Future<Measurement>>();
		ProgressMonitor pm = new ProgressMonitor(null, "Папка обрабатывается слишком долго", "", 0, 1);
		pm.setMaximum(files.length);
		for (File f : files) {
			Callable<Measurement> callable = new ExperimentComputer(f, workspace);
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
					workspace.sample.measurements.add(answer);
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

	// Non-static functions
	File file;
	Workspace workspace;
	public Measurement result;
	SignalIdentifier[] SHIFTS = { null, new DCsignalID(), new BaseSignalID("newAmp20150910.txt", (ZeroCrossing) null),
			null };

	public ExperimentComputer(File filename) {
		file = filename;
	}

	public ExperimentComputer(File filename, Workspace workspace) {
		this(filename);
		this.workspace = workspace;
		if (workspace.signalIDs != null) {
			if (workspace.signalIDs.size() > 0) {
				this.SHIFTS = (SignalIdentifier[]) workspace.signalIDs
						.toArray(new SignalIdentifier[workspace.signalIDs.size()]);
			}
		}
	}

	public ExperimentComputer(File filename, SignalIdentifier[] shifts) {
		this(filename);
		if (shifts != null) {
			this.SHIFTS = shifts;
		}
	}

	public SignalParameters getSignalParameters(double[] signal, int frequency) {
		double[] fourierForFreq = FFT.getFourierForIndex(signal, frequency);
		double phase = FFT.getArgument(fourierForFreq, 0);
		double amplitude = FFT.getAbs(fourierForFreq, 0) / signal.length;
		double nullOffsetFourier[] = FFT.getFourierForIndex(signal, 0);
		double nullOffset = FFT.getAbs(nullOffsetFourier, 0) / signal.length;

		SignalParameters params = new SignalParameters(phase, amplitude, nullOffset);

		return params;
	}

	public Measurement call() {
		ExperimentReader reader = null;
		result = new Measurement();

		// Set high priority to read the file
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		try {
			reader = new ExperimentReader(file.toPath());
		} catch (Exception e) {
			e.printStackTrace();
			return result;
		}
		// Set low priority, so that other threads could easily read the file
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		int numCol = reader.getColumnCount();
		if (numCol > 1) {

			final double EXPERIMENT_FREQUENCY = reader.getExperimentFrequency();
			// double[][] singlePeriodSumm = reader.getOnePeriodSumm();
			double[][] croppedData = reader.getCroppedData();
			final int FREQ_INDEX = reader.getCroppedDataPeriodsCount() * 2;
			result.frequency = EXPERIMENT_FREQUENCY;
			for (int currentChannel = 1; currentChannel < Math.min(numCol, SHIFTS.length); currentChannel++) {
				if (SHIFTS[currentChannel] == null)
					continue;
				SignalParameters params = getSignalParameters(croppedData[currentChannel], FREQ_INDEX);
				if (SHIFTS[currentChannel] instanceof BaseSignalID) {

					BaseSignalID id = (BaseSignalID) SHIFTS[currentChannel];
					ZeroCrossing zc = id.zc;
					if (zc == null)
						continue;

					double signalAngle = params.phase;
					double targetAngle = -signalAngle;
					double omega = 2 * Math.PI * EXPERIMENT_FREQUENCY;
					double currentShift = 0;

					currentShift = zc.getCurrentShift(EXPERIMENT_FREQUENCY);

					double adjustAngle = targetAngle - Math.toRadians(currentShift);
					double editedAngle = truncatePositive(adjustAngle - Math.PI / 4.0);

					targetAngle = truncatePositive(targetAngle);

					double kappa = Math.sqrt(2) * (editedAngle);

					double A = (omega * workspace.sample.length * workspace.sample.length) / (kappa * kappa);

					TemperatureConductivity tCond = new TemperatureConductivity();
					tCond.amplitude = params.amplitude;
					tCond.kappa = kappa;
					tCond.phase = editedAngle;
					tCond.tCond = A;
					result.tCond.add(tCond);

				} else if (SHIFTS[currentChannel] instanceof DCsignalID) {
					Temperature t = new Temperature();
					t.value = params.nullOffset;
					result.temperature.add(t);
				} else if (SHIFTS[currentChannel] instanceof AdjustmentSignalID) {

				}
			}
			reader = null;
			System.gc();
		}
		return result;
	}

}
