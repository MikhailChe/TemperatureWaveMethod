package ru.dolika.experimentLauncher;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import ru.dolika.experiment.sample.Sample;
import ru.dolika.experiment.workspace.Workspace;
import ru.dolika.ui.MemorableDirectoryChooser;

public class ExpLauncher extends JFrame {
	private static final long serialVersionUID = 5151838479190943050L;

	public static void main(String[] args) {
		new ExpLauncher();
	}

	final private Workspace workspace;

	MemorableDirectoryChooser fileChooser = new MemorableDirectoryChooser(this.getClass());

	JLabel statusBar = new JLabel();

	JPanel contentPanel = new JPanel();

	public void createAndShowGUI() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				JMenuBar menuBar = new ExpLauncherMenu(workspace, ExpLauncher.this);
				ExpLauncher.this.setJMenuBar(menuBar);

				getContentPane().setLayout(new BorderLayout(16, 16));

				getContentPane().add(statusBar, BorderLayout.SOUTH);

				getContentPane().add(contentPanel, BorderLayout.CENTER);

				getContentPane().setPreferredSize(new Dimension(640, 480));

				if (workspace != null) {
					Sample sample;
					if ((sample = workspace.getSample()) != null) {
						ExpLauncher.this.setTitle(sample.name);
						ExpLauncher.this.statusBar.setText("" + sample.length);
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
		workspace = Workspace.getInstance();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {

		}
		createAndShowGUI();
	}

}
