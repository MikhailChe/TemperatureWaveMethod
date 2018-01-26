package controller.experiment.analyzer;

import static controller.experiment.analyzer.PhaseUtils.truncateNegative;
import static controller.experiment.analyzer.PhaseUtils.truncatePositive;
import static debug.Debug.println;
import static debug.JExceptionHandler.showException;
import static java.lang.Thread.currentThread;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

import java.awt.Desktop;
import java.awt.Window;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import controller.experiment.reader.ExperimentFileReader;
import controller.mysql.ExperimentUploader;
import debug.Debug;
import debug.JExceptionHandler;
import model.analyzer.SignalParameters;
import model.measurement.Diffusivity;
import model.measurement.Measurement;
import model.measurement.Temperature;
import model.phaseAdjust.PhaseAdjust;
import model.sample.Sample;
import model.signalID.AdjustmentSignalID;
import model.signalID.BaseSignalID;
import model.signalID.DCsignalID;
import model.signalID.SignalIdentifier;
import model.workspace.Workspace;

public class TWMComputer implements Callable<Measurement> {

	public static List<Measurement> computeFolder(File folder, Window parent, ProgressMonitor opm) {
		// Начальные проверки
		if (!folder.isDirectory())
			return null;
		if (!folder.exists())
			return null;

		// Создаём и заполняем список файлами из выбранной папки
		List<File> files = new ArrayList<>();
		files.addAll(Arrays.asList(folder.listFiles(pathname -> pathname.getName().matches("^[0-9]+.txt$"))));

		if (files.size() <= 0)
			return null;

		// Создаём выходной файл
		File resultFile = tryToCreateResultFile(folder);
		if (resultFile == null)
			return null;
		try (BufferedWriter bw = Files.newBufferedWriter(resultFile.toPath(), StandardOpenOption.CREATE_NEW,
				StandardOpenOption.WRITE)) {

			// Создаём пул потоков для параллельного вычисления
			ExecutorService pool = ForkJoinPool.commonPool();
			List<Future<Measurement>> futuresSet = new ArrayList<>();
			ProgressMonitor pm = new ProgressMonitor(parent, "Папка обрабатывается слишком долго", "", 0, 1);
			pm.setMillisToDecideToPopup(1000);
			pm.setMaximum(files.size());
			if (opm.isCanceled() || pm.isCanceled()) {
				return null;
			}
			// Запускаем параллельные вычисления для каждого файла
			files.forEach(f -> futuresSet.add(pool.submit(new TWMComputer(f))));

			int currentProgress = 0;
			boolean header = true;
			List<Measurement> measurements = new ArrayList<>();
			// Ожидаем окончания измзерений
			for (Future<Measurement> future : futuresSet) {
				// Если в прогресс-монтиоре нажали отмену - отменяем всё
				try {
					// Ждёмс завершения текущего расчёта. Не забываем запрашивать
					// состояние у графического интерфейса
					while (!future.isDone()) {
						if (opm.isCanceled() || pm.isCanceled()) {
							// TODO: возможны сбои в работе
							// Убиваем пул потоков, которые всё-ещё обрабатывают
							// файлы
							pool.shutdownNow();
							return null;
						}
						Thread.yield();
					}

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
							bw.write(new String(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF }));
							bw.write(String.format("%s%n", answer.getHeader()));
						}
						bw.write(String.format("%s%n", answer));
					}
					pm.setProgress(++currentProgress);
				} catch (InterruptedException | ExecutionException | IOException e) {
					showException(currentThread(), e);
					println("Возможная ошибка во время расчётов. " + e.getLocalizedMessage());
				}
			}
			pm.close();
			try {
				bw.flush();
				bw.close();
			} catch (IOException e) {
				showException(currentThread(), e);
				println("Ошибка при записи в выходной файл. " + e.getLocalizedMessage());
			}

			// Отркываем файл для просмотра на десктопе
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().open(resultFile);
				} catch (IOException e) {
					showException(e);
					Debug.println("Не удалось открыть файл с результатами. " + e.getLocalizedMessage());
				}
			}
			return measurements;
		} catch (IOException e1) {
			showException(currentThread(), e1);
			println("Ошибка ввода-вывода. " + e1.getLocalizedMessage());
		}
		return null;
	}

	public static File saveToFile(List<Measurement> measurements, File folder) {
		return saveToFile(measurements, folder, false);
	}

	public static File saveToFile(final List<Measurement> measurements, final File folder,
			final boolean appendToSample) {
		// Создаём выходной файл
		Objects.requireNonNull(folder, "Папка не должна быть null");
		Objects.requireNonNull(measurements, "Список измерения не может быть null");
		File resultFile = tryToCreateResultFile(folder);
		if (resultFile == null)
			return null;
		try (BufferedWriter bw = newBufferedWriter(resultFile.toPath(), CREATE_NEW, WRITE)) {
			boolean header = true;
			for (Measurement answer : measurements) {
				if (answer == null)
					continue;
				Workspace workspace = Workspace.getInstance();
				if (appendToSample) {
					Sample sample;
					if ((sample = workspace.getSample()) != null) {
						if (sample.measurements != null) {
							sample.measurements.add(answer);
						}
					}
				}
				if (header) {
					header = false;
					// Add magic UTF-8 BOM
					bw.write(new String(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF }));
					bw.write(String.format("%s%n", answer.getHeader()));
				}
				bw.write(String.format("%s%n", answer));

			}
			bw.flush();
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultFile;
	}

	public void writeToDB(List<Future<Measurement>> futuresSet) {

		// if (JOptionPane.showConfirmDialog(parent, "Загрузить данные в базу?") ==
		// JOptionPane.OK_OPTION) {
		try {
			ExperimentUploader eu = new ExperimentUploader();

			eu.uploadExperimentResults(futuresSet.stream().map(future -> {
				try {
					return future.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}).collect(Collectors.toList()), Workspace.getInstance().getSample());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException
				| IOException e) {
			e.printStackTrace();
		}
		// }
	}

	// Выдаём параметры всех сигналов
	public static SignalParameters[] getAllSignalParameters(double[][] signals, int frequency) {
		SignalParameters[] params = new SignalParameters[signals.length];
		for (int i = 0; i < signals.length; i++) {
			double[] signal = signals[i];
			params[i] = getSignalParameters(signal, frequency);
		}
		return params;
	}

	/**
	 * Выдаём параметры сигнала. Для этого вычисляем Фурье для частоты эксперимента
	 * и среднее значение сигнала.
	 * 
	 * Во-первых достаём оттуда фазу. Фазу сдвигаем на 90 градусов, чтобы она была
	 * по синусу, а не по косинусу. Роли в итоговых вычислениях это не играет, так
	 * как мы используем разницу фаз, зато с человеческой точки зрения смотреть на
	 * синусовую фазу приятнее.
	 *
	 * Затём привязываем фазу к диапазону -Pi..Pi
	 * 
	 * Амплитуда сигнала берётся из того же преобразования Фурье. Для нормализации
	 * делим её на длину входных данных
	 * 
	 * Постоянная составляющая также берётся из Фурье сделанного для нулевой
	 * частоты. По своей сути, фурье для нулевой частоты это такой дикий способ
	 * попросить взять среднее арифметическое от данных. Однако для поддержания
	 * целостности кода в ущерб производительности берём Фурье.
	 * 
	 * Записываем это всё в параметры и отправляем вызывавшему
	 * 
	 * @param signal
	 * @param frequency
	 * @return
	 */
	public static SignalParameters getSignalParameters(double[] signal, int frequency) {
		double[] fourierForFreq = FFT.getFourierForIndex(signal, frequency);

		double phase = FFT.getArgument(fourierForFreq, 0) + Math.PI / 2.0;
		truncateNegative(phase);

		double amplitude = 2.0 * FFT.getAbs(fourierForFreq, 0) / signal.length;
		double nullOffset = Arrays.stream(signal).parallel().average().orElse(Double.NaN);

		SignalParameters params = new SignalParameters(phase, amplitude, nullOffset);

		return params;
	}

	/**
	 * Создаём файл. Если файл открыт пользователем, то ждём пока он его закроет
	 * 
	 * @param folder
	 * @return
	 */
	public static File tryToCreateResultFile(File folder) {
		File resultFile;
		final String formatStringOfReulstFile = "result-%s.tsv";
		try {
			resultFile = new File(folder, String.format(formatStringOfReulstFile, folder.getName()));
			if (resultFile.exists()) {
				boolean exception = false;
				do {
					exception = false;
					try {
						Files.delete(resultFile.toPath());
					} catch (FileSystemException e) {

						exception = true;
						int clicked = JOptionPane.showConfirmDialog(null,
								"Пожалуйста, закройте файл с результатами.\n"
										+ "Иначе я не смогу записать туда новые результаты.\n"
										+ "При необходимости Вы можете сохранить копию файла вручную\n"
										+ "Я подожду и не буду трогать этот файл, пока Вы не закроете это окно\n"
										+ e.getOtherFile(),
								resultFile.toString(), JOptionPane.OK_CANCEL_OPTION);
						if (JOptionPane.OK_OPTION != clicked) {
							return null;
						}
						System.err.printf("Пожалуйста, закройте файл: %s", resultFile.toString());
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							JExceptionHandler.getExceptionHanlder().uncaughtException(Thread.currentThread(), e1);
							e1.printStackTrace();
						}
					}
				} while (exception);
			}
		} catch (IOException e) {
			JExceptionHandler.showException(e);
			return null;
		}
		return resultFile;
	}

	// Non-static functions
	final private File file;

	final private Workspace workspace;

	public Measurement result;
	SignalIdentifier[] SHIFTS;

	private Predicate<Diffusivity> diffFilter;

	public TWMComputer(File filename, Predicate<Diffusivity> diffFilter) {
		this(filename);
		this.diffFilter = diffFilter;
	}

	public TWMComputer(File filename) {
		this.file = filename;
		this.workspace = Workspace.getInstance();
		List<SignalIdentifier> signalIDs;
		if ((signalIDs = workspace.getSignalIDs()) != null) {
			if (signalIDs.size() > 0) {
				this.SHIFTS = signalIDs.toArray(new SignalIdentifier[signalIDs.size()]);
			}
		}

	}

	@Override
	public Measurement call() {
		ExperimentFileReader reader = null;
		result = new Measurement();
		// Set high priority to read the file
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		try {
			reader = new ExperimentFileReader(file.toPath());
		} catch (Exception e) {
			// showException(currentThread(), e);
			Debug.println("Не удалось прочитать файл с измерениями. " + e.getLocalizedMessage());
			return result;
		}
		// Set low priority, so that other threads could easily read the file
		result.time = reader.getTime();
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		// ===========

		int numCol = reader.getColumnCount();
		if (numCol > 1) {

			final double EXPERIMENT_FREQUENCY = reader.getExperimentFrequency();
			double[][] croppedData = reader.getCroppedData();
			final int FREQ_INDEX = reader.getCroppedDataPeriodsCount();
			result.frequency = EXPERIMENT_FREQUENCY;
			SignalParameters[] params = getAllSignalParameters(croppedData, FREQ_INDEX);
			for (int currentChannel = 1; currentChannel < Math.min(numCol, SHIFTS.length); currentChannel++) {
				if (SHIFTS[currentChannel] == null) {
					continue;
				}
				SignalParameters param = params[currentChannel];

				if (SHIFTS[currentChannel] instanceof BaseSignalID) {
					BaseSignalID id = (BaseSignalID) SHIFTS[currentChannel];
					PhaseAdjust zc = id.zc;
					if (zc == null)
						continue;
					double currentShift = zc.getCurrentShift(EXPERIMENT_FREQUENCY);

					if (id.inverse) {
						param = new SignalParameters(param.phase + Math.toRadians(180), param.amplitude,
								param.nullOffset);
					}

					Diffusivity tCond = getPhysicalProperties(param, currentShift, EXPERIMENT_FREQUENCY);
					tCond.signalID = id;
					tCond.channelNumber = currentChannel;

					if (diffFilter != null) {
						if (!diffFilter.test(tCond))
							continue;
					}

					result.diffusivity.add(tCond);

				} else if (SHIFTS[currentChannel] instanceof DCsignalID) {
					DCsignalID signID = (DCsignalID) SHIFTS[currentChannel];

					Temperature t = new Temperature();

					// t.value = params.nullOffset;
					t.signalLevel = signID.getVoltage(param.nullOffset);
					t.value = signID.getTemperature(signID.getVoltage(param.nullOffset) * 1000.0);
					result.temperature.add(t);
				} else if (SHIFTS[currentChannel] instanceof AdjustmentSignalID) {
					Diffusivity tCond = new Diffusivity();

					tCond.amplitude = param.amplitude;
					tCond.phase = -param.phase;
					tCond.frequency = EXPERIMENT_FREQUENCY;
					tCond.channelNumber = currentChannel;
					result.diffusivity.add(tCond);
				}
			}
			reader = null;
			System.gc();
		}
		return result;
	}

	public Diffusivity getPhysicalProperties(final SignalParameters PARAM, final double CURRENT_SHIFT,
			final double EXPERIMENT_FREQUENCY) {

		// Берём начальный сигнал. По идее он может быть как положительным, так
		// и отрицательным, но он представляет собой отставание, а отставание обычно
		// отрицательное
		final double signalAngle = truncateNegative(PARAM.phase);

		// Но мы его всё-равно разворачиваем, чтобы сделать положительным
		double targetAngle = -signalAngle;
		// То есть получается сигнал, который отстаёт на targetAngle градусов

		// Вычитаем из него сдвиг по фазе. Сдвиг уже также был повёрнут в положительную
		// сторону
		// то есть сдвиг так же представляет собой отставание на CURRENT_SHIFT градусов.
		double adjustedAngle = truncatePositive(targetAngle - Math.toRadians(CURRENT_SHIFT));

		// подгатавливаем угол для вычисления капы для этого вычитаем из него 45
		// градусов
		// и принуждаем к тому чтобы оставаться позитивным.
		// Это вряд ли может случиться. Обычно это значит, что всё плохо
		final double preKappaAngle = truncatePositive(adjustedAngle - Math.PI / 4.0);

		// Теперь умножаем на корень из двух и получаем капу
		final double kappa = Math.sqrt(2) * (preKappaAngle);

		// выравниваем углы в положительную сторону
		adjustedAngle = truncatePositive(adjustedAngle);
		targetAngle = truncatePositive(targetAngle);

		// Здесь мы могли бы использовать физическую модель для определения
		// каппы, но она, как оказалось, не даёт большого прироста в точности,
		// но сильно влияет на производительность
		// kappa = PhysicsModel.searchKappaFor(-adjustAngle, 0.001);

		final double omega = 2.0d * Math.PI * EXPERIMENT_FREQUENCY;
		final double length = workspace.getSample().getLength();
		// каноническая формула
		double A = (omega * length * length) / (kappa * kappa);

		// final double DENSITY = 8079;
		final double DENSITY = workspace.getSample().getDensity();

		final double HEAT_FLUX = 1E8;

		// вычисляем теплоёмкость
		double capacitance = HEAT_FLUX * kappa / (PARAM.amplitude * DENSITY * workspace.getSample().getLength() * omega
				* Math.sqrt(Math.pow(Math.sinh(preKappaAngle), 2) + Math.pow(Math.sin(preKappaAngle), 2)));

		Diffusivity tCond = new Diffusivity();

		tCond.amplitude = PARAM.amplitude;
		tCond.kappa = kappa;
		tCond.phase = adjustedAngle;
		tCond.diffusivity = A;
		tCond.initSignalParams = PARAM;
		tCond.frequency = EXPERIMENT_FREQUENCY;
		tCond.capacitance = capacitance;
		return tCond;
	}

}
