package controller.experiment.analyzer;

import static debug.Debug.println;
import static debug.JExceptionHandler.showException;
import static java.lang.Thread.currentThread;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

import java.awt.Window;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import controller.experiment.reader.ExperimentFileReader;
import controller.mysql.ExperimentUploader;
import debug.Debug;
import debug.JExceptionHandler;
import model.measurement.Diffusivity;
import model.measurement.Measurement;
import model.sample.Sample;
import model.workspace.Workspace;

public class TWMComputer implements Callable<Measurement> {
	final static Charset UTF8_CHARSET = Charset.forName("UTF-8");

	public static String resultFileSampleID() {
		String sampleName = "";
		try {
			sampleName = String.format("%s-%.3fмм", Workspace.getInstance().getSample().getName(),
					Workspace.getInstance().getSample().getLength() * 1000);
		} catch (NullPointerException e) {// IGNORE
		}
		return sampleName;
	}

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
		MeasurementCalculator calculator = calculatorFor(Workspace.getInstance());
		files.forEach(f -> futuresSet.add(pool.submit(new TWMComputer(f, calculator, null))));
		int currentProgress = 0;

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
					TimeUnit.MILLISECONDS.sleep(100);
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
				}
				pm.setProgress(++currentProgress);
			} catch (InterruptedException | ExecutionException e) {
				showException(currentThread(), e);
				println("Возможная ошибка во время расчётов. " + e.getLocalizedMessage());
			}
		}
		pm.close();

		return measurements;
	}

	public static File saveToDirectory(List<Measurement> measurements, File folder) throws IOException {
		String sampleName = resultFileSampleID();
		return saveToDirectory(measurements, folder, sampleName, false);
	}

	public static File saveToDirectory(final List<Measurement> inputMeasurements, final File folder, String sampleName,
			final boolean appendToSample) throws IOException {
		// Создаём выходной файл
		Objects.requireNonNull(folder, "Папка не должна быть null");
		Objects.requireNonNull(inputMeasurements, "Список измерения не может быть null");
		List<Measurement> measurements = new ArrayList<>(inputMeasurements);
		Collections.sort(measurements, Comparator.comparingLong(Measurement::getTime));

		File resultFile = tryToCreateResultFile(folder, sampleName);
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

	public static void writeToDB(List<Future<Measurement>> futuresSet) {
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

	/**
	 * Создаём файл. Если файл открыт пользователем, то ждём пока он его закроет
	 * 
	 * @param folder
	 * @return
	 */
	public static File tryToCreateResultFile(File folder, String sampleName) {
		File resultFile;
		LocalDateTime ldt = LocalDateTime.now();
		Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		final String formatStringOfReulstFile = "result-%2$s-%1$tY%1$tm%1$td%1$tH%1$tM%1$tS.tsv";
		try {
			resultFile = new File(folder, String.format(formatStringOfReulstFile, date, sampleName));
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

	private final File file;
	private final MeasurementCalculator calculator;
	private final Predicate<Diffusivity> diffFilter;
	private Measurement result;

	public TWMComputer(File file, Predicate<Diffusivity> diffFilter) {
		this(file, calculatorFor(Workspace.getInstance()), diffFilter);
	}

	public TWMComputer(File file) {
		this(file, null);
	}

	private TWMComputer(File file, MeasurementCalculator calculator, Predicate<Diffusivity> diffFilter) {
		this.file = file;
		this.calculator = calculator;
		this.diffFilter = diffFilter;
	}

	private static MeasurementCalculator calculatorFor(Workspace workspace) {
		Sample sample = workspace.getSample();
		var properties = sample == null ? null
				: new MeasurementCalculator.SampleProperties(sample.getLength(), sample.getDensity());
		return new MeasurementCalculator(properties, workspace.getSignalIDs());
	}

	@Override
	public synchronized Measurement call() {
		if (result == null) {
			ExperimentFileReader reader;
			try {
				reader = new ExperimentFileReader(file.toPath());
			} catch (IOException | RuntimeException e) {
				Debug.println("Не удалось прочитать файл с измерениями. " + e.getLocalizedMessage());
				return null;
			}
			Measurement calculated = calculator.calculate(reader);
			if (diffFilter != null) {
				calculated.diffusivity.removeIf(diff -> diff.signalID != null && !diffFilter.test(diff));
			}
			result = calculated;
		}
		return result;
	}
}
