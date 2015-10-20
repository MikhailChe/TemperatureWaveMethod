package ru.dolika.experimentLauncher;

import java.awt.BorderLayout;
import java.awt.Button;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import ru.dolika.experiment.sample.SampleFactory;
import ru.dolika.experiment.workspace.Workspace;

public class ExpLauncher extends JFrame {
	private static final long serialVersionUID = 5151838479190943050L;

	public static void main(String[] args) {
		new ExpLauncher();
	}

	Button b = null;
	Workspace workspace = null;

	JFileChooser fileChooser = null;

	JLabel statusBar = new JLabel();

	ExpLauncher() {
		super("Обработчик данных");

		workspace = Workspace.getInstance();

		setLocationRelativeTo(null);

		JMenuBar menuBar = new ExpLauncherMenu(workspace, this);
		this.setJMenuBar(menuBar);

		setLayout(new BorderLayout(16, 16));

		add(statusBar, BorderLayout.SOUTH);

		if (workspace != null) {
			if (workspace.sample != null) {
				if (workspace.sample.name != null)
					ExpLauncher.this.setTitle(workspace.sample.name);

				ExpLauncher.this.statusBar.setText("" + workspace.sample.length);
			}
		}
		pack();
		setVisible(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setAlwaysOnTop(true);
	}

	public void saveSampleAs() {
		File sf = workspace.samplefile;
		workspace.samplefile = null;
		saveSample();
		workspace.samplefile = sf;
	}

	public void saveSample() {
		if (workspace.sample == null) {
			return;
		}
		if (workspace.samplefile == null)

		{
			fileChooser.setDialogTitle("Сохранить как...");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			int option = fileChooser.showSaveDialog(ExpLauncher.this);
			if (option == JFileChooser.APPROVE_OPTION) {
				if (fileChooser.getSelectedFile() != null) {
					if (fileChooser.getSelectedFile().exists()) {
						int confirmer = JOptionPane.showConfirmDialog(ExpLauncher.this,
								"Файл уже существует.\nВы хотите перезаписать его?");
						if (confirmer == JOptionPane.YES_OPTION || confirmer == JOptionPane.OK_OPTION) {
							SampleFactory.saveSample(fileChooser.getSelectedFile().getAbsolutePath(), workspace.sample);
							workspace.sample = null;
							workspace.samplefile = null;
						}
					} else {
						SampleFactory.saveSample(fileChooser.getSelectedFile().getAbsolutePath(), workspace.sample);
						workspace.samplefile = fileChooser.getSelectedFile();
					}
				}
			}
		} else {
			SampleFactory.saveSample(workspace.samplefile.getAbsolutePath(), workspace.sample);
			workspace.sample = null;
			workspace.samplefile = null;
		}
	}
}
