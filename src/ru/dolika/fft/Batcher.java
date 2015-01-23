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
			File folder = fileChooser.getSelectedFile();
			compute(folder);
			prefs.put(LAST_FOLDER, folder.toString());
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
		ExecutorService pool = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors());
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

	double[] oldAdjustAD = new double[] { 0, 157.92366, 202.35604, 219.76022,
			227.49335, 232.46174, 235.95052, 237.89532, 240.84918, 240.88662,
			243.05881 };
	double[] newAdjustWithShld = new double[] { 0, 70.36014, 71.38645,
			70.84705, 72.06953, 72.13395, 71.54485, 71.64011, 72.71662,
			71.9964, 74.04955, 75.48641, 74.7094, 74.34681, 75.87396, 74.45393,
			74.59327, 75.85669, 75.20807, 77.03656, 76.87302, 77.95942,
			76.56962, 76.47844, 78.47918, 79.40677, 78.0511, 82.36202,
			81.95725, 83.30791, 82.55963 };
	double[] newAdjustWoShld = new double[] { 0, 70.92, 70.92, 71.28, 71.28,
			70.92, 70.69509, 70.47667, 71.1, 70.74, 70.92, 71.82, 71.4702,
			70.92, 71.4899, 70.2, 71.1, 71.09557, 70.38, 71.64, 70.776, 71.46,
			70.13042, 69.40577, 69.66, 70.92, 69.03794, 72.18, 71.47712, 71.82,
			72.72 };

	final double[] SHIFT = oldAdjustAD;

	public String call() {

		try {
			ExperimentReader reader = new ExperimentReader(file.toPath());
			int numCol = reader.getColumnCount();
			if (numCol > 1) {
				// Making calculations
				double[] col1 = reader.getDataColumn(0);
				double[] col2 = reader.getDataColumn(1);
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
				long time = System.currentTimeMillis();
				signalAngle = FFT.getArgument(
						FFT.getFourierForIndex(col2S, freqIndex), 0);
				System.out.println(System.currentTimeMillis() - time);
				double targetAngle = -signalAngle;
				final double l = (2.02) / 1000.0;
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
				sb.append(String.format(
						"%,5.1f\t%,.5f\t%e\t%,.5f\t%,.5f\t%,.5f\t%,.5f",
						freqency, kappa, A, Math.toDegrees(signalAngle),
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
