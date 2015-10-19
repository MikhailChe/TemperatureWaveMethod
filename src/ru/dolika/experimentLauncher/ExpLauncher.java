package ru.dolika.experimentLauncher;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import ru.dolika.experimentAnalyzer.signalID.dialog.SignalIDSettingsDialog;

public class ExpLauncher extends JFrame {
	private static final long serialVersionUID = 5151838479190943050L;

	public static void main(String[] args) {
		new ExpLauncher();
	}

	Button b = null;
	Workspace workspace = Workspace.getInstance();

	JFileChooser fileChooser = null;

	ExpLauncher() {
		super("Обработчик данных");

		setLocationRelativeTo(null);

		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu("Файл");
		menuBar.add(fileMenu);

		JMenuItem fileNewSample = new JMenuItem("Новый образец");
		fileNewSample.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (workspace.sample != null) {
					int shouldSaveOption = JOptionPane.showConfirmDialog(ExpLauncher.this,
							"Файл образца не был сохранен.\nХотите сохранить его перед открытием нового?",
							"Не забудь сохраниться", JOptionPane.YES_NO_CANCEL_OPTION);
					if (shouldSaveOption == JOptionPane.NO_OPTION) {
						workspace.sample = null;
						System.gc();
					}
					if (shouldSaveOption == JOptionPane.YES_OPTION) {
						saveSample();
						workspace.sample = null;
						System.gc();
					}
				}
				if (workspace.sample == null) {
					String name = JOptionPane.showInputDialog(ExpLauncher.this, "Введите имя образца");
					if (name == null)
						return;
					String comment = JOptionPane.showInputDialog(ExpLauncher.this, "Комментарий");
					if (comment == null) {
						return;
					}
					NumberFormat format = NumberFormat.getInstance();
					format.setMaximumFractionDigits(7);
					format.setMinimumFractionDigits(0);
					JFormattedTextField formatter = new JFormattedTextField(format);

					formatter.addKeyListener(new KeyListener() {

						public void yo() {
							if (formatter.isEditValid())
								formatter.setBackground(java.awt.Color.WHITE);
							else
								formatter.setBackground(java.awt.Color.RED);
						}

						@Override
						public void keyPressed(KeyEvent arg0) {
							yo();
						}

						@Override
						public void keyReleased(KeyEvent arg0) {
							yo();
						}

						@Override
						public void keyTyped(KeyEvent arg0) {
							yo();
						}
					});
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

					workspace.sample = SampleFactory.getSample();
					workspace.sample.name = name;
					workspace.sample.comments = comment;
					workspace.sample.length = length;

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
				if (workspace.sample != null) {
					int shouldSaveOption = JOptionPane.showConfirmDialog(ExpLauncher.this,
							"Файл образца не был сохранен.\nХотите сохранить его перед открытием нового?",
							"Не забудь сохраниться", JOptionPane.YES_NO_CANCEL_OPTION);
					if (shouldSaveOption == JOptionPane.NO_OPTION) {
						workspace.sample = null;
						System.gc();
					}
					if (shouldSaveOption == JOptionPane.YES_OPTION) {
						saveSample();
						workspace.sample = null;
						System.gc();
					}
				}
				if (workspace.sample == null) {
					fileChooser.setDialogTitle("Отркыть...");
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setMultiSelectionEnabled(false);
					int option = fileChooser.showOpenDialog(ExpLauncher.this);
					if (option == JFileChooser.APPROVE_OPTION) {
						if (fileChooser.getSelectedFile() != null) {
							workspace.sample = SampleFactory.forBinary(fileChooser.getSelectedFile().getAbsolutePath());
							workspace.samplefile = fileChooser.getSelectedFile();
							System.out.println(workspace.sample);
							ExpLauncher.this.setTitle(workspace.sample.name);
						}
					}

				}
			}
		});
		fileOpen.add(fileOpenProject);

		JMenuItem fileSave = new JMenuItem("Сохранить...");
		fileSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Workspace w = Workspace.getInstance();
				w.save();
				saveSample();
			}
		});
		fileMenu.add(fileSave);

		JMenuItem fileSaveAs = new JMenuItem("Сохранить как...");
		fileSaveAs.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Workspace w = Workspace.getInstance();
				w.save();
				saveSampleAs();
			}
		});
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
		chooseChannels.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SignalIDSettingsDialog().setVisible(true);
			}
		});
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
					}
				}
			}
		} else {
			SampleFactory.saveSample(workspace.samplefile.getAbsolutePath(), workspace.sample);
			workspace.sample = null;
			workspace.samplefile = null;
		}
	}

	class BAC implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent evt) {
			new Thread(new BatcherLaunch(workspace)).start();
		}

	}
}
