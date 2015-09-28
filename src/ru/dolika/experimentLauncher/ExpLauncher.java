﻿package ru.dolika.experimentLauncher;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import ru.dolika.experiment.sample.SampleFactory;
import ru.dolika.experiment.workspace.Workspace;
import ru.dolika.experimentAnalyzer.BatcherLaunch;

public class ExpLauncher extends JFrame {
	private static final long serialVersionUID = 5151838479190943050L;

	public static void main(String[] args) {
		new ExpLauncher();
	}

	Button b = null;
	Workspace workspace = Workspace.getInstance();

	JFileChooser fileChooser = null;

	ExpLauncher() {
		super("Launcher");

		setLocationRelativeTo(null);

		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu("Файл");
		menuBar.add(fileMenu);

		JMenuItem fileNewSample = new JMenuItem("Новый образец");
		fileNewSample.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (workspace.s != null) {
					int shouldSaveOption = JOptionPane.showConfirmDialog(ExpLauncher.this,
							"Файл образца не был сохранен.\nХотите сохранить его перед открытием нового?",
							"Не забудь сохраниться", JOptionPane.YES_NO_CANCEL_OPTION);
					if (shouldSaveOption == JOptionPane.NO_OPTION) {
						workspace.s = null;
						System.gc();
					}
					if (shouldSaveOption == JOptionPane.YES_OPTION) {
						saveSample();
						workspace.s = null;
						System.gc();
					}
				}
				if (workspace.s == null) {
					String name = JOptionPane.showInputDialog(ExpLauncher.this, "Введите имя образца");
					if (name == null)
						return;
					String comment = JOptionPane.showInputDialog(ExpLauncher.this, "Комментарий");
					if (comment == null) {
						return;
					}
					NumberFormat format = NumberFormat.getInstance();
					JFormattedTextField formatter = new JFormattedTextField(format);

					JOptionPane.showMessageDialog(ExpLauncher.this, formatter, "Толщина", JOptionPane.QUESTION_MESSAGE);
					String lengthString = formatter.getText();
					if (lengthString == null) {
						return;
					}

					double length;
					try {
						length = format.parse(lengthString).doubleValue();
					} catch (ParseException e1) {
						e1.printStackTrace();
						return;
					}

					workspace.s = SampleFactory.getSample();
					workspace.s.name = name;
					workspace.s.comments = comment;
					workspace.s.length = length;

				}
			}
		});
		fileMenu.add(fileNewSample);
		JMenu fileOpen = new JMenu("Открыть");
		fileMenu.add(fileOpen);
		fileChooser = new JFileChooser();
		JMenuItem fileOpenProject = new JMenuItem("Проект...");
		fileOpenProject.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (workspace.s != null) {
					int shouldSaveOption = JOptionPane.showConfirmDialog(ExpLauncher.this,
							"Файл образца не был сохранен.\nХотите сохранить его перед открытием нового?",
							"Не забудь сохраниться", JOptionPane.YES_NO_CANCEL_OPTION);
					if (shouldSaveOption == JOptionPane.NO_OPTION) {
						workspace.s = null;
						System.gc();
					}
					if (shouldSaveOption == JOptionPane.YES_OPTION) {
						saveSample();
						workspace.s = null;
						System.gc();
					}
				}
				if (workspace.s == null) {
					fileChooser.setDialogTitle("Отркыть...");
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setMultiSelectionEnabled(false);
					int option = fileChooser.showOpenDialog(ExpLauncher.this);
					if (option == JFileChooser.APPROVE_OPTION) {
						if (fileChooser.getSelectedFile() != null) {
							workspace.s = SampleFactory.forBinary(fileChooser.getSelectedFile().getAbsolutePath());
							workspace.samplefile = fileChooser.getSelectedFile();
							System.out.println(workspace.s);
							ExpLauncher.this.setTitle(workspace.s.name);
						}
					}

				}
			}
		});
		fileOpen.add(fileOpenProject);

		JMenuItem fileSave = new JMenuItem("Сохранить...");
		fileMenu.add(fileSave);

		JMenuItem fileSaveAs = new JMenuItem("Сохранить как...");
		fileMenu.add(fileSaveAs);

		JMenu toolsMenu = new JMenu("Инструменты");
		menuBar.add(toolsMenu);
		JMenuItem prepareZeroCrossing = new JMenuItem("Подготовить юстировку");
		toolsMenu.add(prepareZeroCrossing);
		JMenuItem prepareGrads = new JMenuItem("Подготовить градуировку");
		toolsMenu.add(prepareGrads);
		JMenuItem toolsDofiles = new JMenuItem("Произвести измерения");

		toolsMenu.add(toolsDofiles);

		JMenu settingsMenu = new JMenu("Настройки");
		menuBar.add(settingsMenu);
		JMenuItem chooseChannels = new JMenuItem("Выбрать каналы");
		settingsMenu.add(chooseChannels);
		JMenuItem sampleSettings = new JMenuItem("Настройки образца");
		settingsMenu.add(sampleSettings);

		setLayout(new BorderLayout(16, 16));

		b = new Button("Push me harder");
		add(b);
		b.addActionListener(new BAC());

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
		if (workspace.s == null) {
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
							SampleFactory.saveSample(fileChooser.getSelectedFile().getAbsolutePath(), workspace.s);
							workspace.s = null;
							workspace.samplefile = null;
						}
					}
				}
			}
		} else

		{
			SampleFactory.saveSample(workspace.samplefile.getAbsolutePath(), workspace.s);
			workspace.s = null;
			workspace.samplefile = null;
		}
	}

	class BAC implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent evt) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					new BatcherLaunch();
				}
			}).start();
		}

	}
}
