/**
 * 
 */
package ru.dolika.experimentLauncher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import ru.dolika.experiment.sample.Sample;
import ru.dolika.experiment.sample.SampleFactory;
import ru.dolika.experiment.sample.SampleSettingsDialog;
import ru.dolika.experiment.workspace.Workspace;
import ru.dolika.experimentAnalyzer.BatcherLaunch;
import ru.dolika.experimentAnalyzer.signalID.dialog.SignalIDSettingsDialog;
import ru.dolika.thermocouple.graduate.GraduateConverter;

/**
 * @author Mikey
 *
 */
public class ExpLauncherMenu extends JMenuBar {
	private static final long serialVersionUID = 2882344753047235272L;

	/**
	 * 
	 */

	public ExpLauncherMenu(Workspace workspace, ExpLauncher parent) {
		JMenu fileMenu = new JMenu("Файл");
		this.add(fileMenu);

		JMenuItem fileNewSample = new JMenuItem("Новый образец");
		fileNewSample.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (workspace.sample != null) {
					int shouldSaveOption = JOptionPane.showConfirmDialog(parent,
							"Файл образца не был сохранен.\nХотите сохранить его перед открытием нового?",
							"Не забудь сохраниться", JOptionPane.YES_NO_CANCEL_OPTION);
					if (shouldSaveOption == JOptionPane.NO_OPTION) {
						workspace.sample = null;
						System.gc();
					}
					if (shouldSaveOption == JOptionPane.YES_OPTION) {
						parent.saveSample();
						workspace.sample = null;
						System.gc();
					}
				}
				if (workspace.sample == null) {
					Sample s = SampleFactory.getSample();
					int status = SampleSettingsDialog.showSampleSettings(parent, s);

					if (status == SampleSettingsDialog.OK_BUTTON) {
						workspace.sample = s;
						parent.setTitle(workspace.sample.name);
						parent.statusBar.setText("" + workspace.sample.length);
						workspace.samplefile = null;
					} else {
						/* Добавить обработчик отказа */;
					}

				}
			}
		});
		fileMenu.add(fileNewSample);
		JMenu fileOpen = new JMenu("Открыть");
		fileMenu.add(fileOpen);
		JMenuItem fileOpenProject = new JMenuItem("Проект...");
		fileOpenProject.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (workspace.sample != null) {
					int shouldSaveOption = JOptionPane.showConfirmDialog(parent,
							"Файл образца не был сохранен.\nХотите сохранить его перед открытием нового?",
							"Не забудь сохраниться", JOptionPane.YES_NO_CANCEL_OPTION);
					if (shouldSaveOption == JOptionPane.NO_OPTION) {
						workspace.sample = null;
						System.gc();
					}
					if (shouldSaveOption == JOptionPane.YES_OPTION) {
						parent.saveSample();
						workspace.sample = null;
						System.gc();
					}
				}
				if (workspace.sample == null) {
					parent.fileChooser.setDialogTitle("Отркыть...");
					parent.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					parent.fileChooser.setMultiSelectionEnabled(false);
					int option = parent.fileChooser.showOpenDialog(parent);
					if (option == JFileChooser.APPROVE_OPTION) {
						if (parent.fileChooser.getSelectedFile() != null) {
							workspace.sample = SampleFactory
									.forBinary(parent.fileChooser.getSelectedFile().getAbsolutePath());
							workspace.samplefile = parent.fileChooser.getSelectedFile();
							System.out.println(workspace.sample);
							parent.setTitle(workspace.sample.name);
							parent.statusBar.setText("" + workspace.sample.length);
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
				workspace.save();
				parent.saveSample();
			}
		});
		fileMenu.add(fileSave);

		JMenuItem fileSaveAs = new JMenuItem("Сохранить как...");
		fileSaveAs.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Workspace w = Workspace.getInstance();
				w.save();
				parent.saveSampleAs();
			}
		});
		fileMenu.add(fileSaveAs);

		JMenu toolsMenu = new JMenu("Инструменты");
		this.add(toolsMenu);
		JMenuItem prepareZeroCrossing = new JMenuItem("Подготовить юстировку");
		toolsMenu.add(prepareZeroCrossing);
		JMenuItem prepareGrads = new JMenuItem("Подготовить градуировку");
		toolsMenu.add(prepareGrads);
		JMenuItem toolsDofiles = new JMenuItem("Произвести измерения");
		toolsDofiles.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new BatcherLaunch(workspace)).start();
			}
		});
		toolsMenu.add(toolsDofiles);

		toolsMenu.add(new JSeparator(SwingConstants.HORIZONTAL));

		JMenuItem convertTemperature = new JMenuItem("Преобразовать температуру");
		convertTemperature.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new GraduateConverter(parent);
			}
		});
		toolsMenu.add(convertTemperature);

		JMenu settingsMenu = new JMenu("Настройки");
		this.add(settingsMenu);
		JMenuItem chooseChannels = new JMenuItem("Выбрать каналы");
		chooseChannels.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SignalIDSettingsDialog().setVisible(true);
			}
		});
		settingsMenu.add(chooseChannels);
		JMenuItem sampleSettings = new JMenuItem("Настройки образца");
		sampleSettings.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {

					int status = SampleSettingsDialog.showSampleSettings(parent, workspace.sample);

					if (status == SampleSettingsDialog.OK_BUTTON) {
						parent.setTitle(workspace.sample.name);
						parent.statusBar.setText("" + workspace.sample.length);
					}

				} catch (IllegalArgumentException e1) {
				}
			}
		});
		settingsMenu.add(sampleSettings);

		// TODO Auto-generated constructor stub
	}
}
