package ru.dolika.experiment.Analyzer;

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

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import ru.dolika.experiment.measurement.Measurement;
import ru.dolika.experiment.measurement.Temperature;
import ru.dolika.experiment.measurement.TemperatureConductivity;
import ru.dolika.experiment.signalID.AdjustmentSignalID;
import ru.dolika.experiment.signalID.BaseSignalID;
import ru.dolika.experiment.signalID.DCsignalID;
import ru.dolika.experiment.signalID.SignalIdentifier;
import ru.dolika.experiment.workspace.Workspace;
import ru.dolika.experiment.zeroCrossing.ZeroCrossing;

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

	public static Measurement computeFolder(File folder, Workspace workspace,
			JFrame parent) {
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
			resultFile = new File(folder, "result-" + folder.getName() + ".tsv");
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
		ProgressMonitor pm = new ProgressMonitor(parent,
				"Папка обрабатывается слишком долго", "", 0, 1);
		pm.setMaximum(files.length);
		for (File f : files) {
			Callable<Measurement> callable = new ExperimentComputer(f,
					workspace);
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
					if (workspace.sample != null) {
						if (workspace.sample.measurements != null) {
							workspace.sample.measurements.add(answer);
						}
					}
					bw.write(String.format("%s%n", answer));
				}
				pm.setProgress(++currentProgress);
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

	SignalIdentifier[] SHIFTS2 = { null,
			new DCsignalID(),
			// new AdjustmentSignalID(),
			new BaseSignalID(new File(
					"config/just/20160428newAmpChangeTauLastCascade.txt")),
			new BaseSignalID(new File("config/just/20160427oldAmp.txt"))
	// new AdjustmentSignalID(),
	};

	SignalIdentifier[] SHIFTS = { null, null, null, new AdjustmentSignalID()
	// new AdjustmentSignalID(),
	};

	public ExperimentComputer(File filename, Workspace workspace) {
		this.file = filename;
		this.workspace = workspace;
		if (workspace.signalIDs != null) {
			if (workspace.signalIDs.size() > 0) {
				this.SHIFTS = (SignalIdentifier[]) workspace.signalIDs
						.toArray(new SignalIdentifier[workspace.signalIDs
								.size()]);
			}
		}
	}

	public SignalParameters getSignalParameters(double[] signal, int frequency) {
		double[] fourierForFreq = FFT.getFourierForIndex(signal, frequency);
		double phase = FFT.getArgument(fourierForFreq, 0);
		double amplitude = FFT.getAbs(fourierForFreq, 0) / signal.length;
		double nullOffsetFourier[] = FFT.getFourierForIndex(signal, 0);
		double nullOffset = FFT.getAbs(nullOffsetFourier, 0) / signal.length;

		SignalParameters params = new SignalParameters(phase, amplitude,
				nullOffset);

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
			double[][] croppedData = reader.getCroppedData();
			final int FREQ_INDEX = reader.getCroppedDataPeriodsCount() * 2;
			result.frequency = EXPERIMENT_FREQUENCY;
			for (int currentChannel = 1; currentChannel < Math.min(numCol,
					SHIFTS.length); currentChannel++) {
				if (SHIFTS[currentChannel] == null)
					continue;
				SignalParameters params = getSignalParameters(
						croppedData[currentChannel], FREQ_INDEX);
				if (SHIFTS[currentChannel] instanceof BaseSignalID) {

					BaseSignalID id = (BaseSignalID) SHIFTS[currentChannel];
					ZeroCrossing zc = id.zc;
					if (zc == null)
						continue;

					double signalAngle = params.phase;

					double targetAngle = -signalAngle;
					double currentShift = zc
							.getCurrentShift(EXPERIMENT_FREQUENCY);

					double adjustAngle = targetAngle
							- Math.toRadians(currentShift);
					double editedAngle = truncatePositive(adjustAngle - Math.PI
							/ 4.0);

					targetAngle = truncatePositive(targetAngle);

					double kappa = Math.sqrt(2) * (editedAngle);

					adjustAngle = truncatePositive(adjustAngle);

					// kappa = PhysicsModel.searchKappaFor(-adjustAngle, 0.001);

					double omega = 2 * Math.PI * EXPERIMENT_FREQUENCY;
					double A = (omega * workspace.sample.length * workspace.sample.length)
							/ (kappa * kappa);

					TemperatureConductivity tCond = new TemperatureConductivity();

					tCond.amplitude = params.amplitude;
					tCond.kappa = kappa;
					tCond.phase = adjustAngle;
					tCond.tCond = A;
					tCond.initSignalParams = params;

					result.tCond.add(tCond);

				} else if (SHIFTS[currentChannel] instanceof DCsignalID) {
					DCsignalID signID = (DCsignalID) SHIFTS[currentChannel];

					Temperature t = new Temperature();

					// t.value = params.nullOffset;
					t.signalLevel = signID.getVoltage(params.nullOffset);
					t.value = signID.getTemperature(signID
							.getVoltage(params.nullOffset) * 1000.0);
					result.temperature.add(t);
				} else if (SHIFTS[currentChannel] instanceof AdjustmentSignalID) {
					TemperatureConductivity tCond = new TemperatureConductivity();
					tCond.amplitude = params.amplitude;
					tCond.phase = -params.phase;
					result.tCond.add(tCond);
				}
			}
			reader = null;
			System.gc();
		}
		return result;
	}
}
