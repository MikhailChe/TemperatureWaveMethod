﻿package ru.dolika.experimentLauncher;

import static ru.dolika.debug.Debug.debug;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import ru.dolika.debug.Debug;
import ru.dolika.experiment.sample.Sample;
import ru.dolika.experiment.workspace.Workspace;

public class ExpLauncher extends JFrame {
	private static final long serialVersionUID = 5151838479190943050L;

	public static void main(String[] args) {
		if (Debug.debug)
			System.out.println("public static void main(String[] args)");
		new ExpLauncher();
	}

	final private Workspace workspace;
	JLabel statusBar;

	JPanel contentPanel;

	public void createAndShowGUI() {
		SwingUtilities.invokeLater(() -> {
			if (debug)
				System.out.println("Creating gui");
			statusBar = new JLabel();
			contentPanel = new JPanel();
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

			JMenuBar menuBar = new ExpLauncherMenu(ExpLauncher.this);
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
		});
	}

	ExpLauncher() {
		super("Обработчик данных");

		if (debug)
			System.out.println("Getting workspace instance");
		workspace = Workspace.getInstance();
		if (debug)
			System.out.println("Got workspace instance");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		createAndShowGUI();
	}

}
