
package controller.experiment.Analyzer;

import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import debug.JExceptionHandler;
import model.experiment.Analyzer.SignalParameters;
import model.experiment.zeroCrossing.ZeroCrossing;
import model.experiment.zeroCrossing.ZeroCrossingFactory;
import view.MemorableDirectoryChooser;
import view.experiment.zeroCrossing.ZeroCrossingViewerPanel;

public class AdjustFileCreator implements Runnable {
	final private JFrame parent;

	public AdjustFileCreator(JFrame parent) {
		this.parent = parent;
	}

	@Override
	public void run() {
		MemorableDirectoryChooser fileChooser = new MemorableDirectoryChooser(
				TWMComputer.class);
		fileChooser.setDialogTitle("Выберите папку для обработки юстировки");
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		Integer channelNumber = (Integer) JOptionPane.showInputDialog(parent,
				"Выберите номер канала для выполнения юстировки",
				"Каналы юстировки", JOptionPane.QUESTION_MESSAGE,
				null, new Integer[] { 1, 2, 3, 4, 5 }, new Integer(1));

		if (channelNumber == null) {
			return;
		}

		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			fileChooser.saveCurrentSelection();

			File folder = fileChooser.getSelectedFile();
			File[] files = folder.listFiles((pathname) -> {
				return pathname.getName().matches("^[0-9]+.txt$");
			});
			fileChooser = new MemorableDirectoryChooser(this.getClass());
			fileChooser.setDialogTitle(
					"Выберите папку для сохранения юстировочных данных");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			if (fileChooser
					.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
				fileChooser.saveCurrentSelection();
				File resultFolder = fileChooser.getSelectedFile();
				File resultFile = new File(resultFolder,
						System.currentTimeMillis() + "ch" + channelNumber + "."
								+ ZeroCrossing.extensionFilter
										.getExtensions()[0]);
				try {
					BufferedWriter bw = Files.newBufferedWriter(
							resultFile.toPath(), StandardOpenOption.CREATE_NEW,
							StandardOpenOption.WRITE);
					ProgressMonitor pm = new ProgressMonitor(parent,
							"Папка обрабатывается слишком долго", "", 0,
							files.length);
					for (int i = 0; i < files.length; i++) {
						File file = files[i];

						ExperimentFileReader reader = new ExperimentFileReader(
								file.toPath());
						double[][] croppedData = reader.getCroppedData();
						if (channelNumber >= croppedData.length) {
							JOptionPane.showMessageDialog(parent,
									"Выбранного канала не существует в одном или нескольких файлах",
									"Ошибка",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
						final int FREQ_INDEX = reader
								.getCroppedDataPeriodsCount() * 2;
						SignalParameters param = TWMComputer
								.getSignalParameters(croppedData[channelNumber],
										FREQ_INDEX);
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
					JExceptionHandler.getExceptionHanlder()
							.uncaughtException(Thread.currentThread(), e);
					e.printStackTrace();
				}

				ZeroCrossing zc = ZeroCrossingFactory.forFile(resultFile);
				ZeroCrossingViewerPanel zcvp = new ZeroCrossingViewerPanel(zc);

				JDialog dialog = new JDialog(parent, "Файл юстировки");
				dialog.getContentPane().add(zcvp);
				dialog.pack();
				dialog.setModal(true);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);

			}
		}
	}
}