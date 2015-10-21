package ru.dolika.experimentLauncher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import ru.dolika.experiment.sample.SampleFactory;
import ru.dolika.experiment.workspace.Workspace;

public class ExpLauncher extends JFrame {
	private static final long serialVersionUID = 5151838479190943050L;

	public static void main(String[] args) {
		new ExpLauncher();
	}

	Workspace workspace = null;

	JFileChooser fileChooser = null;

	JLabel statusBar = new JLabel();

	public void createAndShowGUI() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				workspace = Workspace.getInstance();

				setLocationRelativeTo(null);

				JMenuBar menuBar = new ExpLauncherMenu(workspace,
						ExpLauncher.this);
				ExpLauncher.this.setJMenuBar(menuBar);

				getContentPane().setLayout(new BorderLayout(16, 16));

				getContentPane().add(statusBar, BorderLayout.SOUTH);

				getContentPane().setPreferredSize(new Dimension(640, 480));

				if (workspace != null) {
					if (workspace.sample != null) {
						if (workspace.sample.name != null)
							ExpLauncher.this.setTitle(workspace.sample.name);

						ExpLauncher.this.statusBar.setText(""
								+ workspace.sample.length);
					}
				}
				pack();
				setLocationRelativeTo(null);
				setVisible(true);
				setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			}
		});
	}

	ExpLauncher() {
		super("Обработчик данных");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {

		}
		createAndShowGUI();
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
						int confirmer = JOptionPane
								.showConfirmDialog(ExpLauncher.this,
										"Файл уже существует.\nВы хотите перезаписать его?");
						if (confirmer == JOptionPane.YES_OPTION
								|| confirmer == JOptionPane.OK_OPTION) {
							SampleFactory.saveSample(fileChooser
									.getSelectedFile().getAbsolutePath(),
									workspace.sample);
							workspace.sample = null;
							workspace.samplefile = null;
						}
					} else {
						SampleFactory.saveSample(fileChooser.getSelectedFile()
								.getAbsolutePath(), workspace.sample);
						workspace.samplefile = fileChooser.getSelectedFile();
					}
				}
			}
		} else {
			SampleFactory.saveSample(workspace.samplefile.getAbsolutePath(),
					workspace.sample);
			workspace.sample = null;
			workspace.samplefile = null;
		}
	}
}
