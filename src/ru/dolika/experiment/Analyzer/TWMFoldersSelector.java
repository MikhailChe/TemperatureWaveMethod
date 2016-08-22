package ru.dolika.experiment.Analyzer;

import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import ru.dolika.experiment.workspace.Workspace;
import ru.dolika.ui.MemorableDirectoryChooser;

/**
 * @since 05.10.2015
 * @author Mikey
 * 
 */
public class TWMFoldersSelector implements Runnable {
	private Window parent = null;

	public TWMFoldersSelector(JFrame parent) {
		this.parent = parent;
	}

	@Override
	public void run() {
		Workspace workspace = Workspace.getInstance();

		if (workspace.getSample() == null) {
			JOptionPane.showMessageDialog(parent,
					"Не был выбран файл образца.\nПожалуйста закройте это окно и выберите образец или создайте новый",
					"Ошибка образца", JOptionPane.ERROR_MESSAGE);
			return;
		}

		MemorableDirectoryChooser fileChooser = new MemorableDirectoryChooser(this.getClass());
		fileChooser.setDialogTitle("Выберите папку с данными");

		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			List<File> folders = Arrays.asList(fileChooser.getSelectedFiles());
			ProgressMonitor pm = new ProgressMonitor(parent, "Анализ файлов в папке", "Идёт вычисление измерений", 0,
					folders.size());

			pm.setMillisToDecideToPopup(1000);
			final AtomicInteger progress = new AtomicInteger(0);

			// int progress = 0;

			pm.setProgress(progress.incrementAndGet());
			folders.stream().parallel().forEach(folder -> {
				pm.setNote(folder.getName());
				TWMComputer.computeFolder(folder, parent, pm);
				pm.setProgress(progress.incrementAndGet());
			});

			pm.close();

			Toolkit.getDefaultToolkit().beep();
		}
	}
}