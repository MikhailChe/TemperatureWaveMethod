package controller.experiment.Analyzer;

import java.awt.Desktop;
import java.awt.Window;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import controller.mysql.ExperimentUploader;
import debug.JExceptionHandler;
import model.experiment.Analyzer.SignalParameters;
import model.experiment.measurement.Diffusivity;
import model.experiment.measurement.Measurement;
import model.experiment.measurement.Temperature;
import model.experiment.sample.Sample;
import model.experiment.signalID.AdjustmentSignalID;
import model.experiment.signalID.BaseSignalID;
import model.experiment.signalID.DCsignalID;
import model.experiment.signalID.SignalIdentifier;
import model.experiment.workspace.Workspace;
import model.experiment.zeroCrossing.ZeroCrossing;

public class TWMComputer implements Callable<Measurement> {

	public static List<Measurement> computeFolder(File folder, Window parent,
			ProgressMonitor opm) {
		if (!folder.isDirectory())
			return null;
		if (!folder.exists())
			return null;

		List<File> files = new ArrayList<>();
		files.addAll(Arrays.asList(folder.listFiles(
				pathname -> pathname.getName().matches("^[0-9]+.txt$"))));

		if (files.size() <= 0)
			return null;

		BufferedWriter bw = null;
		File resultFile = tryToCreateResultFile(folder);
		if (resultFile == null)
			return null;
		try {
			bw = Files.newBufferedWriter(resultFile.toPath(),
					StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
		} catch (IOException e1) {
			JExceptionHandler.getExceptionHanlder()
					.uncaughtException(Thread.currentThread(), e1);
			e1.printStackTrace();
		}
		if (bw == null)
			return null;

		ExecutorService pool = ForkJoinPool.commonPool();
		List<Future<Measurement>> futuresSet = new ArrayList<>();
		ProgressMonitor pm = new ProgressMonitor(parent,
				"Папка обрабатывается слишком долго", "", 0, 1);
		pm.setMillisToDecideToPopup(1000);
		pm.setMaximum(files.size());
		if (opm.isCanceled() || pm.isCanceled()) {
			return null;
		}
		files.forEach(f -> futuresSet.add(pool.submit(new TWMComputer(f))));

		int currentProgress = 0;
		boolean header = true;
		List<Measurement> measurements = new ArrayList<>();

		for (Future<Measurement> future : futuresSet) {
			if (opm.isCanceled() || pm.isCanceled()) {
				return null;
			}
			try {
				Measurement answer = future.get();
				if (answer != null) {
					Workspace workspace = Workspace.getInstance();
					Sample sample;
					if ((sample = workspace.getSample()) != null) {
						measurements.add(answer);
						if (sample.measurements != null) {
							sample.measurements.add(answer);
						}
					}
					if (header) {
						header = false;
						// Add magic UTF-8 BOM
						bw.write(new String(new byte[] { (byte) 0xEF,
								(byte) 0xBB, (byte) 0xBF }));
						bw.write(String.format("%s%n", answer.getHeader()));
					}
					bw.write(String.format("%s%n", answer));
				}
				pm.setProgress(++currentProgress);
			} catch (InterruptedException | ExecutionException
					| IOException e) {
				JExceptionHandler.getExceptionHanlder()
						.uncaughtException(Thread.currentThread(), e);
				e.printStackTrace();
			}
		}
		pm.close();

		if (JOptionPane.showConfirmDialog(parent,
				"Загрузить данные в базу?") == JOptionPane.OK_OPTION) {
			try {
				ExperimentUploader eu = new ExperimentUploader();

				eu.uploadExperimentResults(futuresSet.stream().map(future -> {
					try {
						return future.get();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}).collect(Collectors.toList()),
						Workspace.getInstance().getSample());
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			bw.flush();
			bw.close();
		} catch (IOException e) {
			JExceptionHandler.getExceptionHanlder()
					.uncaughtException(Thread.currentThread(), e);
			e.printStackTrace();
		}
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().open(resultFile);
			} catch (IOException e) {
				JExceptionHandler.getExceptionHanlder()
						.uncaughtException(Thread.currentThread(), e);
				e.printStackTrace();
			}
		}
		return measurements;
	}

	public static SignalParameters[] getAllSignalParameters(double[][] signals,
			int frequency) {
		SignalParameters[] params = new SignalParameters[signals.length];
		for (int i = 0; i < signals.length; i++) {
			double[] signal = signals[i];
			params[i] = getSignalParameters(signal, frequency);
		}
		return params;
	}

	public static SignalParameters getSignalParameters(double[] signal,
			int frequency) {
		double[] fourierForFreq = FFT.getFourierForIndex(signal, frequency);
		double phase = FFT.getArgument(fourierForFreq, 0);
		double amplitude = FFT.getAbs(fourierForFreq, 0) / signal.length;
		double nullOffsetFourier[] = FFT.getFourierForIndex(signal, 0);
		double nullOffset = FFT.getAbs(nullOffsetFourier, 0) / signal.length;

		SignalParameters params = new SignalParameters(phase, amplitude,
				nullOffset);

		return params;
	}

	public static File tryToCreateResultFile(File folder) {
		File resultFile;
		final String formatStringOfReulstFile = "result-%s.tsv";
		try {
			resultFile = new File(folder,
					String.format(formatStringOfReulstFile, folder.getName()));
			if (resultFile.exists()) {
				boolean exception = false;
				do {
					exception = false;
					try {
						Files.delete(resultFile.toPath());
					} catch (java.nio.file.FileSystemException e) {
						JExceptionHandler.getExceptionHanlder()
								.uncaughtException(Thread.currentThread(), e);
						e.printStackTrace();
						exception = true;

						JOptionPane.showMessageDialog(null,
								resultFile.toString(),
								"Пожалуйста, закройте файл с результатами.\n"
										+ "Иначе я не смогу записать туда новые результаты.\n"
										+ "При необходимости Вы можете сохранить копию файла вручную\n"
										+ "Я подожду и не буду трогать этот файл, пока Вы не закроете это окно\n",
								JOptionPane.ERROR_MESSAGE);
						System.err.printf("Пожалуйста, закройте файл: %s",
								resultFile.toString());
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							JExceptionHandler.getExceptionHanlder()
									.uncaughtException(Thread.currentThread(),
											e1);
							e1.printStackTrace();
						}
					}
				} while (exception);
			}
		} catch (IOException e) {
			JExceptionHandler.getExceptionHanlder()
					.uncaughtException(Thread.currentThread(), e);
			e.printStackTrace();
			return null;
		}
		return resultFile;
	}

	// Non-static functions
	final private File file;

	final private Workspace workspace;

	public Measurement result;
	SignalIdentifier[] SHIFTS;

	public TWMComputer(File filename) {
		this.file = filename;
		this.workspace = Workspace.getInstance();
		List<SignalIdentifier> signalIDs;
		if ((signalIDs = workspace.getSignalIDs()) != null) {
			if (signalIDs.size() > 0) {
				this.SHIFTS = (SignalIdentifier[]) signalIDs
						.toArray(new SignalIdentifier[signalIDs.size()]);
			}
		}

	}

	public Measurement call() {
		ExperimentFileReader reader = null;
		result = new Measurement();

		// Set high priority to read the file
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		try {
			reader = new ExperimentFileReader(file.toPath());
		} catch (Exception e) {
			JExceptionHandler.getExceptionHanlder()
					.uncaughtException(Thread.currentThread(), e);
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
			SignalParameters[] params = getAllSignalParameters(croppedData,
					FREQ_INDEX);
			for (int currentChannel = 1; currentChannel < Math.min(numCol,
					SHIFTS.length); currentChannel++) {
				if (SHIFTS[currentChannel] == null)
					continue;
				SignalParameters param = params[currentChannel];
				if (SHIFTS[currentChannel] instanceof BaseSignalID) {
					BaseSignalID id = (BaseSignalID) SHIFTS[currentChannel];
					ZeroCrossing zc = id.zc;
					if (zc == null)
						continue;

					double signalAngle = param.phase;

					double targetAngle = -signalAngle;
					double currentShift = zc
							.getCurrentShift(EXPERIMENT_FREQUENCY);

					double adjustAngle = targetAngle
							- Math.toRadians(currentShift);
					double editedAngle = truncatePositive(
							adjustAngle - Math.PI / 4.0);

					targetAngle = truncatePositive(targetAngle);

					double kappa = Math.sqrt(2) * (editedAngle);

					adjustAngle = truncatePositive(adjustAngle);

					// kappa = PhysicsModel.searchKappaFor(-adjustAngle, 0.001);

					double omega = 2 * Math.PI * EXPERIMENT_FREQUENCY;
					double A = (omega * workspace.getSample().getLength()
							* workspace.getSample().getLength()) / (kappa * kappa);

					Diffusivity tCond = new Diffusivity();

					tCond.amplitude = param.amplitude;
					tCond.kappa = kappa;
					tCond.phase = adjustAngle;
					tCond.diffusivity = A;
					tCond.initSignalParams = param;
					tCond.frequency = EXPERIMENT_FREQUENCY;
					tCond.signalID = id;

					result.diffusivity.add(tCond);

				} else if (SHIFTS[currentChannel] instanceof DCsignalID) {
					DCsignalID signID = (DCsignalID) SHIFTS[currentChannel];

					Temperature t = new Temperature();

					// t.value = params.nullOffset;
					t.signalLevel = signID.getVoltage(param.nullOffset);
					t.value = signID.getTemperature(
							signID.getVoltage(param.nullOffset) * 1000.0);
					result.temperature.add(t);
				} else if (SHIFTS[currentChannel] instanceof AdjustmentSignalID) {
					Diffusivity tCond = new Diffusivity();
					tCond.amplitude = param.amplitude;
					tCond.phase = -param.phase;
					result.diffusivity.add(tCond);
				}
			}
			reader = null;
			System.gc();
		}
		return result;
	}

	/**
	 * @param value
	 * @return positive angle (from 0 to 2 * Pi)
	 */
	public double truncatePositive(double value) {
		while (value < 0) {
			value += Math.PI * 2.0;
		}
		while (value > Math.PI * 2.0) {
			value -= Math.PI * 2.0;
		}
		return value;
	}
}
