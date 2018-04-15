package controller.experiment.analyzer;

import static controller.experiment.analyzer.PhaseUtils.truncateNegative;
import static controller.experiment.analyzer.PhaseUtils.truncatePositive;
import static debug.Debug.println;
import static debug.JExceptionHandler.showException;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import static java.lang.Thread.currentThread;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import java.awt.Desktop;
import java.awt.Window;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JComponent;
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
	final static Charset UTF8_CHARSET = Charset.forName("UTF-8");

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

		try (BufferedOutputStream bos = new BufferedOutputStream(
				Files.newOutputStream(resultFile.toPath(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));) {

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

							bos.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
							bos.write(String.format("%s%n", answer.getHeader()).getBytes(UTF8_CHARSET));
						}
						bos.write(String.format("%s%n", answer).getBytes(UTF8_CHARSET));
					}
					pm.setProgress(++currentProgress);
				} catch (InterruptedException | ExecutionException | IOException e) {
					showException(currentThread(), e);
					println("Возможная ошибка во время расчётов. " + e.getLocalizedMessage());
				}
			}
			pm.close();
			try {
				bos.flush();
				bos.close();
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

	public static File saveToFile(List<Measurement> measurements, File folder) throws IOException {
		return saveToFile(measurements, folder, false);
	}

	public static File saveToFile(final List<Measurement> inputMeasurements, final File folder,
			final boolean appendToSample) throws IOException {
		// Создаём выходной файл
		Objects.requireNonNull(folder, "Папка не должна быть null");
		Objects.requireNonNull(inputMeasurements, "Список измерения не может быть null");
		List<Measurement> measurements = new ArrayList<>(inputMeasurements);
		Collections.sort(measurements, Comparator.comparingLong(Measurement::getTime));

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
			throw e;
		}
		return resultFile;
	}

	public void writeToDB(List<Future<Measurement>> futuresSet) {
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
	}

	// Выдаём параметры всех сигналов
	public static List<SignalParameters> getAllSignalParameters(double[][] signals, int freq_index, double frequency) {
		List<SignalParameters> params = new ArrayList<>(signals.length);
		for (int i = 0; i < signals.length; i++) {
			double[] signal = signals[i];
			params.add(getSignalParameters(signal, freq_index, frequency));
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
	 * @param freq_index
	 * @return
	 */
	public static SignalParameters getSignalParameters(double[] signal, int freq_index, double frequency) {
		double[] fourierForFreq = FFT.getFourierForIndex(signal, freq_index);

		double phase = FFT.getArgument(fourierForFreq, 0) + Math.PI / 2.0;
		truncateNegative(phase);

		double amplitude = 2.0 * FFT.getAbs(fourierForFreq, 0) / signal.length;
		double nullOffset = Arrays.stream(signal).parallel().average().orElse(Double.NaN);

		SignalParameters params = new SignalParameters(phase, amplitude, nullOffset, frequency);

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
		LocalDateTime ldt = LocalDateTime.now();
		Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		final String formatStringOfReulstFile = "result-%1$tY%1$tm%1$td%1$tH%1$tM%1$tS-%2$s.tsv";
		try {
			resultFile = new File(folder, String.format(formatStringOfReulstFile, date, folder.getName()));
			if (resultFile.exists()) {
				boolean exception = false;
				do {
					exception = false;
					try {
						Files.delete(resultFile.toPath());
					} catch (FileSystemException e) {
						System.out.println(Locale.getDefault());
						JComponent.setDefaultLocale(Locale.getDefault());
						exception = true;
						int clicked = JOptionPane.showConfirmDialog(null, "Пожалуйста, закройте файл с результатами.\n"
								+ "Иначе я не смогу записать туда новые результаты.\n"
								+ "При необходимости Вы можете сохранить копию файла вручную\n"
								+ "Я подожду и не буду трогать этот файл, пока Вы не закроете это окно\n" + e.getFile(),
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
	AtomicBoolean computing = new AtomicBoolean(false);
	final List<SignalIdentifier> signalIDs;

	private Predicate<Diffusivity> diffFilter;

	public TWMComputer(File filename, Predicate<Diffusivity> diffFilter) {
		this(filename);
		this.diffFilter = diffFilter;
	}

	public TWMComputer(File filename) {
		this.file = filename;
		this.workspace = Workspace.getInstance();
		List<SignalIdentifier> signalIDs1;
		if ((signalIDs1 = workspace.getSignalIDs()) != null) {
			if (signalIDs1.size() > 0) {
				this.signalIDs = unmodifiableList(signalIDs1);
			} else {
				this.signalIDs = emptyList();
			}
		} else {
			this.signalIDs = emptyList();
		}

	}

	@Override
	public synchronized Measurement call() {
		if (result != null) {
			return result;
		}
		ExperimentFileReader reader = getFileReader();
		if (reader == null)
			return null;

		Measurement res = new Measurement();
		res.time = reader.getTime();
		int colCount = reader.getColumnCount();
		if (colCount > 1) {
			res.frequency = reader.getExperimentFrequency();
			double[][] croppedData = reader.getCroppedData();
			final int FREQ_INDEX = reader.getCroppedDataPeriodsCount();
			List<SignalParameters> params = getAllSignalParameters(croppedData, FREQ_INDEX, res.frequency);
			for (int channel = 1; channel < Math.min(colCount, signalIDs.size()); channel++) {
				if (signalIDs.get(channel) == null) {
					continue;
				}
				SignalParameters param = params.get(channel);
				selectConsumer(signalIDs.get(channel), res).accept(channel, param);
			}
		}
		result = res;
		return result;
	}

	private BiConsumer<Integer, SignalParameters> selectConsumer(final SignalIdentifier ident, Measurement m) {
		if (ident instanceof BaseSignalID) {
			return (chan, param) -> baseSignalConsumer(chan, param, (BaseSignalID) ident, m);

		} else if (ident instanceof DCsignalID) {
			return (chan, param) -> dcSignalConsumer(chan, param, (DCsignalID) ident, m);

		} else if (ident instanceof AdjustmentSignalID) {
			return (chan, param) -> adjustmentSignalConsumer(chan, param, (AdjustmentSignalID) ident, m);

		}
		return (a, b) -> {
			// DO nothing.
		};
	}

	private void baseSignalConsumer(int currentChannel, SignalParameters param, BaseSignalID id, Measurement result) {

		PhaseAdjust adjust = id.phaseAdjust;
		if (adjust == null)
			return;
		double currentShift = adjust.getCurrentShift(param.frequency);

		// Если случилось так, что неправильно установили фазу - переворачиваем
		currentShift = toDegrees(
				truncateNegative(id.inverse ? toRadians(currentShift + 180) : toRadians(currentShift)));

		Diffusivity tCond = getPhysicalProperties(param, currentShift, param.frequency);
		tCond.signalID = id;
		tCond.channelNumber = currentChannel;

		if (diffFilter != null) {
			if (!diffFilter.test(tCond))
				return;
		}

		result.diffusivity.add(tCond);
	}

	/**
	 * @param currentChannel
	 */
	private static void dcSignalConsumer(int currentChannel, SignalParameters param, DCsignalID signID,
			Measurement result) {
		Temperature t = new Temperature();

		// t.value = params.nullOffset;
		t.signalLevel = signID.getVoltage(param.nullOffset);
		t.value = signID.getTemperature(signID.getVoltage(param.nullOffset) * 1000.0);
		result.temperature.add(t);
	}

	/**
	 * @param id
	 *            adjustment id. May contain additional info. unused for now
	 */
	private static void adjustmentSignalConsumer(int currentChannel, SignalParameters param, AdjustmentSignalID id,
			Measurement result) {
		Diffusivity tCond = new Diffusivity();

		tCond.amplitude = param.amplitude;
		tCond.phase = -param.phase;
		tCond.frequency = param.frequency;
		tCond.channelNumber = currentChannel;
		result.diffusivity.add(tCond);
	}

	private ExperimentFileReader getFileReader() {
		ExperimentFileReader reader = null;
		// Set high priority to read the file
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		try {
			reader = new ExperimentFileReader(file.toPath());
		} catch (Exception e) {
			// showException(currentThread(), e);
			Debug.println("Не удалось прочитать файл с измерениями. " + e.getLocalizedMessage());
		}
		// Set low priority, so that other threads could easily read the file
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		// ===========
		return reader;
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
