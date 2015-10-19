package ru.dolika.experimentAnalyzer;

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
import javax.swing.WindowConstants;

import ru.dolika.experiment.workspace.Workspace;

/**
 * @since 05.10.2015
 * @author Mikey
 * 
 */
public class BatcherLaunch extends JFrame implements Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4738936172017785472L;
	static final String LAST_FOLDER = "experiment_storage_lastdirectory";
	static Preferences prefs = Preferences.userNodeForPackage(ExperimentComputer.class);

	private Workspace workspace = null;

	public BatcherLaunch(Workspace ws) {
		this();
		workspace = ws;
	}

	private BatcherLaunch() {
		super("Экспериментатор 2.0");
		Locale.setDefault(new Locale("ru", "ru"));
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setUndecorated(true);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	@Override
	public void run() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setPreferredSize(new Dimension(800, 600));
		fileChooser.setDialogTitle("Выберите папку с данными");

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

		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File[] folders = fileChooser.getSelectedFiles();
			ProgressMonitor pm = new ProgressMonitor(this, "Анализ файлов в папке", "Идёт вычисление измерений", 0,
					folders.length);
			pm.setMillisToDecideToPopup(0);
			int progress = 0;
			pm.setProgress(progress++);

			for (File f : folders) {
				pm.setNote(f.getName());
				ExperimentComputer.computeFolder(f, workspace);
				pm.setProgress(progress++);
				if (pm.isCanceled()) {
					break;
				}
			}
			pm.close();
			if (folders.length > 0) {
				prefs.put(LAST_FOLDER, folders[folders.length - 1].getAbsolutePath());
			}
		}
		Toolkit.getDefaultToolkit().beep();
		this.setVisible(false);
		workspace = null;
		this.dispose();
	}
}