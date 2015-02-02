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
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Batcher implements Callable<String> {
	static final String LAST_FOLDER = "experiment_storage_lastdirectory";
	static Preferences prefs = Preferences.userNodeForPackage(Batcher.class);

	public static void main(String[] args) {

		Locale.setDefault(new Locale("ru"));

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getCrossPlatformLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException | UnsupportedLookAndFeelException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

		JFrame frame = new JFrame("Ёкспериментатор 2.0");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setVisible(true);
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		{
			String lastFolder = prefs.get(LAST_FOLDER, null);
			if (lastFolder != null) {
				try {
					File dir = new File(new File(lastFolder).getCanonicalPath());
					fileChooser.setSelectedFile(dir);
				} catch (Exception e) {

				}
			}
		}

		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			frame.dispose();
			File[] folders = fileChooser.getSelectedFiles();
			long time = System.currentTimeMillis();
			ExecutorService epool = Executors.newFixedThreadPool(2);

			for (int i = 0; i < folders.length; i++) {
				File folder = folders[i];
				epool.execute(new Runnable() {
					public void run() {
						compute(folder);
						System.out.println(folder);
					}
				});
			}
			epool.shutdown();
			try {
				epool.awaitTermination(1, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println((System.currentTimeMillis() - time));
			if (folders.length > 0) {
				prefs.put(LAST_FOLDER, folders[folders.length - 1].toString());
			}
		}
		frame.dispose();
		System.exit(0);
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
			resultFile = new File(folder, "результат.txt");
			if (resultFile.exists()) {
				Files.delete(resultFile.toPath());
			}
			bw = Files.newBufferedWriter(resultFile.toPath(),
					StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		ExecutorService pool = Executors.newFixedThreadPool(2);
		Vector<Future<String>> set = new Vector<Future<String>>();
		for (File f : files) {
			Callable<String> callable = new Batcher(f);
			Future<String> future = pool.submit(callable);
			set.add(future);
		}
		int i = 0;
		for (Future<String> future : set) {
			try {
				String answer = future.get();
				bw.write(String.format("%s%n", answer, i));
				i++;
			} catch (InterruptedException | ExecutionException | IOException e) {
				e.printStackTrace();
			}
		}
		pool.shutdown();

		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * if (Desktop.isDesktopSupported()) { try {
		 * Desktop.getDesktop().open(resultFile);
		 * 
		 * } catch (IOException e) { e.printStackTrace(); } }
		 */
	}

	File file;
	public String result;

	public Batcher(File filename) {
		file = filename;
	}

	double[] oldAdjust = new double[] { 0, -30.28125, 22.45054, 42.31053,
			52.66231, 57.94369, 61.25883, 63.19492, 65.91531, 66.59776,
			67.54032, 68.92635, 69.68961, 69.81704, 71.40068, 70.82494,
			72.1481, 72.75135, 72.83383, 74.10869, 73.81043, 74.90718,
			73.45702, 72.85313, 74.73567, 75.30558, 74.34102, 77.86631,
			77.71087, 78.22714, 79.71899 };
	double[] newAdjust = new double[] { 0, 75.18617, 75.71124, 76.25895,
			76.12594, 76.31025, 75.96452, 76.08309, 76.43141, 76.50549,
			76.72915, 77.74432, 77.32127, 77.24342, 76.88853, 76.40208,
			77.10222, 77.0721, 76.88899, 77.56667, 78.07193, 77.74368,
			76.24314, 76.58774, 76.23381, 78.21338, 76.15771, 79.31749,
			78.52636, 78.72754, 79.91789 };

	final double[] SHIFT = newAdjust;

	public String call() {

		try {
			ExperimentReader reader = new ExperimentReader(file.toPath());
			int numCol = reader.getColumnCount();
			if (numCol > 1) {
				// Making calculations
				double[] col1 = reader.getDataColumn(0);
				double[] col2 = reader.getDataColumn(4);
				double[] col2S;
				double signalAngle;
				double freqency = reader.getExperimentFrequency();
				int freqIndex = 1;
				col2S = SignalAdder.getOnePeriod(col1, col2);

				/*
				 * double[] FFTdata; DoubleFFT_1D fft = new
				 * DoubleFFT_1D(col2S.length); { FFTdata = Arrays.copyOf(col2S,
				 * col2S.length * 2); fft.realForwardFull(FFTdata); signalAngle
				 * = FFT.getArgument(FFTdata, freqIndex); }
				 */
				double[] fourierForIndex = FFT.getFourierForIndex(col2S,
						freqIndex);
				signalAngle = FFT.getArgument(fourierForIndex, 0);
				double targetAngle = -signalAngle;
				final double l = (0.935) / 1000.0;
				double omega = 2 * Math.PI * freqency;
				double currentShift = getCurrentShift(freqency);
				double adjustAngle = targetAngle - Math.toRadians(currentShift);
				double editedAngle = adjustAngle - Math.PI / 4;

				while (editedAngle < 0)
					editedAngle += Math.PI * 2;

				while (editedAngle > 2 * Math.PI)
					editedAngle -= Math.PI * 2;

				double kappa = Math.sqrt(2) * (editedAngle);

				double A = (omega * l * l) / (kappa * kappa);

				StringBuilder sb = new StringBuilder();
				sb.append(String.format(Locale.forLanguageTag("RU"),
						"%4.1f\t%.2f\t%e\t%.0f\t%.5f\t%.5f\t%.5f", freqency,
						kappa, A, FFT.getAbs(fourierForIndex, 0),
						Math.toDegrees(targetAngle),
						Math.toDegrees(adjustAngle),
						Math.toDegrees(editedAngle)));
				return sb.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private double getCurrentShift(double freqency) {
		double currentShift = 0;
		if (freqency == (int) freqency) {
			int index = (int) freqency;
			if (index >= SHIFT.length)
				index = SHIFT.length - 1;
			currentShift = SHIFT[index];
		} else {
			int i1 = (int) freqency;
			int i2 = (int) (freqency + 0.5);
			if (i1 >= SHIFT.length)
				i1 = SHIFT.length - 1;
			if (i2 >= SHIFT.length)
				i2 = SHIFT.length - 1;
			double coeff = freqency - (int) freqency;
			currentShift = SHIFT[i1] * (1 - coeff) + SHIFT[i2] * (coeff);
		}
		return currentShift;
	}

}
