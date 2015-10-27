package ru.dolika.experiment.folderWatch;

import java.awt.Dimension;
import java.io.File;
import java.io.FileFilter;
import java.util.prefs.Preferences;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import ru.dolika.experiment.Analyzer.ExperimentComputer;
import ru.dolika.experiment.measurement.Measurement;
import ru.dolika.experiment.workspace.Workspace;

public class FolderWatch extends JDialog implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1831549678406783975L;

	static final String LAST_FOLDER = "folderWatch.experiment_storage_lastdirectory";

	static Preferences prefs = Preferences
			.userNodeForPackage(FolderWatch.class);

	private Workspace workspace;

	public File[] filesInFolder;
	public File folder;

	JLabel temperatureLabel = new JLabel("здесь будет температура");

	public FolderWatch(JFrame parent, Workspace workspace) throws Exception {
		super(parent, true);
		this.workspace = workspace;
		if (workspace.sample == null) {
			JOptionPane
					.showMessageDialog(
							parent,
							"Не был выбран файл образца.\nПожалуйста закройте это окно и выберите образец или создайте новый",
							"Ошибка образца", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setPreferredSize(new Dimension(800, 600));
		fileChooser.setDialogTitle("Выберите папку с данными");

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

		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			folder = fileChooser.getSelectedFile();
			if (folder == null) {
				this.setVisible(false);
				this.dispose();
				throw new Exception();
			}

		} else {
			this.setVisible(false);
			this.dispose();
			throw new Exception();
		}
		temperatureLabel.setPreferredSize(new Dimension(320, 100));
		this.getContentPane().add(temperatureLabel);
		this.pack();

		new Thread(this).start();

	}

	public void checkNewFile() {
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().matches("^[0-9]+.txt$");
			}
		});

		if (filesInFolder == null) {
			filesInFolder = files;
			updateValuesForFile(filesInFolder[filesInFolder.length - 1]);
			return;
		}

		if (filesInFolder.length != files.length) {
			filesInFolder = files;
			updateValuesForFile(filesInFolder[filesInFolder.length - 1]);
			return;
		}
	}

	public void updateValuesForFile(File f) {
		ExperimentComputer exc = new ExperimentComputer(f, workspace);
		Measurement m = exc.call();
		temperatureLabel.setText((m.temperature.isEmpty() ? temperatureLabel
				.getText() : m.temperature.get(0) + ""));
	}

	@Override
	public void run() {
		while (this.isDisplayable()) {
			checkNewFile();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
