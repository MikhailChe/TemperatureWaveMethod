package ru.dolika.fft;

import java.io.File;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;

public class BatcherLaunch {
	static final String LAST_FOLDER = "experiment_storage_lastdirectory";
	static Preferences prefs = Preferences.userNodeForPackage(Batcher.class);

	public static void main(String[] args) {
		Locale.setDefault(new Locale("ru"));
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getCrossPlatformLookAndFeelClassName());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

		JFrame frame = new JFrame("Экспериментатор 2.0");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setLocationRelativeTo(null);
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
			File[] folders = fileChooser.getSelectedFiles();
			ProgressMonitor pm = new ProgressMonitor(frame,
					"Анализ файлов в папке", "Идёт вычисление измерений", 0,
					folders.length);
			pm.setMillisToDecideToPopup(0);
			int progress = 0;
			pm.setProgress(progress++);
			for (File f : folders) {
				pm.setNote(f.getName());
				Batcher.compute(f);
				pm.setProgress(progress++);
				if (pm.isCanceled()) {
					break;
				}
			}
			pm.close();
			if (folders.length > 0) {
				prefs.put(LAST_FOLDER, folders[folders.length - 1].toString());
			}
		}
		frame.setVisible(false);
		frame.dispose();
		System.out.println(Profiler.getInstance());
	}
}