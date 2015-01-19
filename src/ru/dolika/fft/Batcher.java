package ru.dolika.fft;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
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

import org.jtransforms.fft.DoubleFFT_1D;

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

	final double[] SHIFT = new double[] { 0, 71.45293, 72.00104, 72.9386,
			72.51942, 72.5301, 72.82515, 72.67496, 72.75336, 73.49862,
			72.10436, 73.10743, 72.87512, 73.61398, 72.13576, 73.46983,
			73.22098, 73.36099, 73.64398, 73.61175, 73.46766, 73.88014,
			73.65994, 74.27192, 73.91359, 74.72526, 74.27997, 73.96622,
			74.72533, 74.60557, 75.35061 };

	public String call() {

		try {
			ExperimentReader reader = new ExperimentReader(file.toPath());
			int numCol = reader.getColumnCount();
			if (numCol > 1) {
				// Making calculations
				double[] col1 = reader.getDataColumn(0);
				double[] col2 = reader.getDataColumn(1);
				double[] col1S, col2S, FFTdata;
				double signalAngle;
				double modulatorAngle;
				double freqency = reader.getExperimentFrequency();
				int freqIndex = 2;
				{
					Vector<Integer> indicies = new Vector<Integer>();
					double min = Integer.MAX_VALUE;
					double max = Integer.MIN_VALUE;
					for (int i = 0; i < col1.length; i++) {
						if (col1[i] < min) {
							min = col1[i];
						}
						if (col1[i] > max) {
							max = col1[i];
						}
					}
					boolean trigger = false;
					for (int i = 0; i < col1.length; i++) {
						if (col1[i] > min + (max - min) * 0.5) {
							if (!trigger) {
								indicies.add(i);
								trigger = true;
							}
						} else {
							if (trigger) {
								trigger = false;
							}
						}
					}
					int leastSpace = Integer.MAX_VALUE;
					for (int i = 0; i < indicies.size() - 1; i++) {
						int space = indicies.get(i + 1) - indicies.get(i);
						if (space < leastSpace) {
							leastSpace = space;
						}
					}
					if (leastSpace % 2 == 1) {
						leastSpace--;
					}
					if (leastSpace <= 0) {
						System.err.println("LeastSpace computation error");
						System.exit(200);
					}
					indicies.remove(indicies.size() - 1);
					col1S = new double[leastSpace];
					col2S = new double[leastSpace];
					Arrays.fill(col2S, 0);
					Arrays.fill(col1S, 0);
					for (int j = 0; j < indicies.size(); j++) {
						int impIndex = indicies.get(j);
						for (int i = 0; i < col2S.length; i++) {
							col1S[i] += col1[i + impIndex];
							col2S[i] += col2[i + impIndex];
						}
					}
				}

				StringBuilder sb = new StringBuilder();
				DoubleFFT_1D fft = new DoubleFFT_1D(col1S.length);
				{
					FFTdata = col1S;
					fft.realForward(FFTdata);

					modulatorAngle = FFT.getArgument(FFTdata, freqIndex);
				}
				{
					FFTdata = col2S;
					fft.realForward(FFTdata);
					signalAngle = FFT.getArgument(FFTdata, freqIndex);
				}
				while (modulatorAngle < 0) {
					modulatorAngle += Math.PI * 2;
				}
				while (modulatorAngle > Math.PI * 2) {
					modulatorAngle -= Math.PI * 2;
				}
				while (signalAngle < 0) {
					signalAngle += Math.PI * 2;
				}
				while (signalAngle > Math.PI * 2) {
					signalAngle -= Math.PI * 2;
				}
				double targetAngle = modulatorAngle - signalAngle;
				while (targetAngle > Math.PI * 2) {
					targetAngle -= Math.PI * 2;
				}
				while (targetAngle < 0) {
					targetAngle += Math.PI * 2;
				}

				final double l = (2.02) / 1000.0;

				double omega = 2 * Math.PI * freqency;
				double currentShift = getCurrentShift(freqency);
				double adjustAngle = targetAngle + Math.toRadians(currentShift);
				double editedAngle = adjustAngle - Math.PI;

				while (editedAngle < 0)
					editedAngle += Math.PI * 2;

				while (editedAngle > 2 * Math.PI)
					editedAngle -= Math.PI * 2;

				double kappa = Math.sqrt(2) * (editedAngle);

				double A = (omega * l * l) / (kappa * kappa);
				sb.append(String.format(
						"%,5.1f\t%,.5f\t%e\t%,.5f\t%,.5f\t%,.5f", freqency,
						kappa, A, Math.toDegrees(targetAngle),
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
