﻿package ru.dolika.experimentAnalyzer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;

import ru.dolika.experiment.sample.Sample;

public class BatcherLaunch {
	static final String LAST_FOLDER = "experiment_storage_lastdirectory";
	static Preferences prefs = Preferences.userNodeForPackage(Batcher.class);

	private Sample sample = null;

	public static void main(String[] args) {
		new BatcherLaunch();
	}

	public BatcherLaunch(Sample s) {
		this();
		this.sample = s;
	}

	public BatcherLaunch() {

		Locale.setDefault(new Locale("ru", "ru"));
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
		fileChooser.setPreferredSize(new Dimension(800, 600));
		Action details = fileChooser.getActionMap().get("viewTypeDetails");
		details.actionPerformed(null);

		{
			String lastFolder = prefs.get(LAST_FOLDER, null);
			if (lastFolder != null) {
				try {
					File dir = new File(new File(lastFolder).getCanonicalPath());
					fileChooser.setCurrentDirectory(dir);
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
				Batcher.compute(f, sample);
				pm.setProgress(progress++);
				if (pm.isCanceled()) {
					break;
				}
			}
			pm.close();
			if (folders.length > 0) {
				prefs.put(LAST_FOLDER,
						folders[folders.length - 1].getAbsolutePath());
			}
		}
		Toolkit.getDefaultToolkit().beep();
		frame.setVisible(false);
		frame.dispose();
	}
}