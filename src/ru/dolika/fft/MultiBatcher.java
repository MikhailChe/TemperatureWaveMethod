package ru.dolika.fft;

import java.io.File;
import java.io.FileFilter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;

public class MultiBatcher {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		JFrame frame = new JFrame("Экспериментатор 2.0");
		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		{
			String lastFolder = Batcher.prefs.get(Batcher.LAST_FOLDER, null);
			if (lastFolder != null) {
				try {
					File dir = new File(new File(lastFolder).getCanonicalPath());
					fileChooser.setSelectedFile(dir);
				} catch (Exception e) {

				}
			}
		}
		fileChooser.setDialogTitle("Обработать несколько папок");
		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			
			runBatches(fileChooser.getSelectedFile(), frame);
		}
		frame.dispose();
	}

	public static void runBatches(File folder, JFrame frame) {
		if (!folder.isDirectory())
			return;

		File[] innerFolders = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					if (pathname.getName().matches(
							"[0-9]{4}\\.[0-9]{2}\\.[0-9]{2}.*")) {
						return true;
					}
				}
				return false;
			}
		});
		System.out.println("Нашел " + innerFolders.length + " папок-кандидатов");
		ProgressMonitor pm = new ProgressMonitor(frame, "Анализ файлов в папке",
				"Идёт вычисление измерений", 0, innerFolders.length);
		pm.setMillisToDecideToPopup(0);
		int progress = 0;
		pm.setProgress(progress++);
		for (File f : innerFolders) {
			System.out.println(f.getAbsolutePath());
			pm.setNote(f.getName());
			
			Batcher.compute(f);
			pm.setProgress(progress++);

		}
		

	}
}