package ru.dolika.experiment.Analyzer;

import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import ru.dolika.experiment.zeroCrossing.ZeroCrossing;
import ru.dolika.experiment.zeroCrossing.ZeroCrossingFactory;
import ru.dolika.experiment.zeroCrossing.ZeroCrossingViewerPanel;
import ru.dolika.ui.MemorableDirectoryChooser;

public class AdjustFileCreator implements Runnable {
	// final private Workspace workspace;
	final private JFrame parent;

	public AdjustFileCreator(JFrame parent) {
		// this.workspace = workspace;
		this.parent = parent;
	}

	@Override
	public void run() {

		MemorableDirectoryChooser fileChooser = new MemorableDirectoryChooser(
				ExperimentComputer.class);
		fileChooser.setDialogTitle("Выберите папку для обработки юстировки");
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int channelNumber = (int) JOptionPane.showInputDialog(parent,
				"Выберите номер канала для выполнения юстировки",
				"Каналы юстировки", JOptionPane.QUESTION_MESSAGE, null,
				new Integer[] { 1, 2, 3, 4, 5 }, new Integer(1));

		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			fileChooser.saveCurrentSelection();

			File folder = fileChooser.getSelectedFile();
			File[] files = folder.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getName().matches("^[0-9]+.txt$");
				}
			});
			fileChooser = new MemorableDirectoryChooser(this.getClass());
			fileChooser
					.setDialogTitle("Выберите папку для сохранения юстировочных данных");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
				fileChooser.saveCurrentSelection();
				File resultFolder = fileChooser.getSelectedFile();
				File resultFile = new File(resultFolder,
						System.currentTimeMillis() + "ch" + channelNumber
								+ ".txt");
				try {
					BufferedWriter bw = Files.newBufferedWriter(
							resultFile.toPath(), StandardOpenOption.CREATE_NEW,
							StandardOpenOption.WRITE);
					ProgressMonitor pm = new ProgressMonitor(parent,
							"Папка обрабатывается слишком долго", "", 0,
							files.length);
					for (int i = 0; i < files.length; i++) {
						File file = files[i];

						ExperimentReader reader = new ExperimentReader(
								file.toPath());
						double[][] croppedData = reader.getCroppedData();
						if (channelNumber >= croppedData.length) {
							JOptionPane
									.showMessageDialog(
											parent,
											"Выбранного канала не существует в одном или нескольких файлах",
											"Ошибка", JOptionPane.ERROR_MESSAGE);
							return;
						}
						final int FREQ_INDEX = reader
								.getCroppedDataPeriodsCount() * 2;
						SignalParameters param = ExperimentComputer
								.getSignalParameters(
										croppedData[channelNumber], FREQ_INDEX);
						bw.write(String.format("%.1f\t%.3f\r\n",
								reader.getExperimentFrequency(),
								Math.toDegrees(-param.phase)));
						pm.setProgress(i);
					}
					pm.close();
					bw.flush();
					bw.close();

					Toolkit.getDefaultToolkit().beep();
				} catch (IOException e) {
					e.printStackTrace();
				}
				ZeroCrossing zc = ZeroCrossingFactory.forFile(resultFile);
				ZeroCrossingViewerPanel zcvp = new ZeroCrossingViewerPanel(zc);
				JDialog dialog = new JDialog(parent, "Файл градуировки");
				dialog.getContentPane().add(zcvp);
				dialog.pack();
				dialog.setModal(true);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);

			}
		}
	}
}
