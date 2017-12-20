
package controller.experiment.Analyzer;

import static debug.Debug.println;
import static debug.JExceptionHandler.showException;
import static java.lang.Thread.currentThread;

import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

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
		MemorableDirectoryChooser fileChooser = new MemorableDirectoryChooser(TWMComputer.class);
		fileChooser.setDialogTitle("Выберите папку для обработки юстировки");
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		Integer channelNumber = (Integer) JOptionPane.showInputDialog(parent,
				"Выберите номер канала для выполнения юстировки", "Каналы юстировки", JOptionPane.QUESTION_MESSAGE,
				null, new Integer[] { 1, 2, 3, 4, 5 }, new Integer(1));

		if (channelNumber == null)
			return;

		if (fileChooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION)
			return;
		fileChooser.saveCurrentSelection();

		Path folder = fileChooser.getSelectedFile().toPath();

		File[] files = folder.toFile().listFiles(pathname -> pathname.getName().matches("^[0-9]+.txt$"));
		Stream<Path> filePaths = Arrays.asList(files).stream().map(f -> f.toPath());
		fileChooser = new MemorableDirectoryChooser(this.getClass());
		fileChooser.setDialogTitle("Выберите папку для сохранения юстировочных данных");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		if (fileChooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION)
			return;
		fileChooser.saveCurrentSelection();
		Path resultFolder = fileChooser.getSelectedFile().toPath();

		Path resultFile = resultFolder.resolve(System.currentTimeMillis() + "ch" + channelNumber + "."
				+ ZeroCrossing.extensionFilter.getExtensions()[0]);

		try (BufferedWriter bw = Files.newBufferedWriter(resultFile, StandardOpenOption.CREATE_NEW,
				StandardOpenOption.WRITE)) {
			ProgressMonitor pm = new ProgressMonitor(parent, "Папка обрабатывается слишком долго", "", 0, files.length);

			AtomicInteger progIter = new AtomicInteger(0);
			filePaths.forEach(path -> {
				ExperimentFileReader reader = null;
				try {
					reader = new ExperimentFileReader(path);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (reader == null)
					return;

				double[][] croppedData = reader.getCroppedData();
				if (channelNumber >= croppedData.length) {
					JOptionPane.showMessageDialog(parent,
							"Выбранного канала не существует в одном или нескольких файлах", "Ошибка",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				// TODO: следуюшая строка опирается на то факт, что опорный
				// сигнал той же самой частоты, что и полезный сигнал. Теперь
				// так и есть, раньше это было не так. Кажется, нужно что-то
				// сделать, чтобы в будущем можно было обрабатывать старые
				// файлы...
				final int FREQ_INDEX = reader.getCroppedDataPeriodsCount();
				SignalParameters param = TWMComputer.getSignalParameters(croppedData[channelNumber], FREQ_INDEX);
				try {
					bw.write(String.format("%.1f\t%.3f\r\n", reader.getExperimentFrequency(),
							Math.toDegrees(-param.phase)));
				} catch (IOException e) {
					e.printStackTrace();
				}
				pm.setProgress(progIter.incrementAndGet());
			});

			pm.close();
			bw.flush();
			bw.close();

			Toolkit.getDefaultToolkit().beep();
		} catch (IOException e) {
			showException(currentThread(), e);
			println("Ошибка ввода-вывода. " + e.getLocalizedMessage());
		}

		SwingUtilities.invokeLater(() -> {
			ZeroCrossing zc = ZeroCrossingFactory.forFile(resultFile.toFile());
			ZeroCrossingViewerPanel zcvp = new ZeroCrossingViewerPanel(zc);

			JDialog dialog = new JDialog(parent, "Файл юстировки");
			dialog.getContentPane().add(zcvp);
			dialog.pack();
			dialog.setModal(true);
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		});
	}
}
