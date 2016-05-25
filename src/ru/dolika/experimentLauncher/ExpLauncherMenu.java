/**
 * 
 */
package ru.dolika.experimentLauncher;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import ru.dolika.debug.Debug;
import ru.dolika.debug.JExceptionHandler;
import ru.dolika.experiment.Analyzer.AdjustFileCreator;
import ru.dolika.experiment.Analyzer.BatcherLaunch;
import ru.dolika.experiment.folderWatch.FilterTunerGUI;
import ru.dolika.experiment.folderWatch.FolderWatch;
import ru.dolika.experiment.sample.Sample;
import ru.dolika.experiment.sample.SampleFactory;
import ru.dolika.experiment.sample.SampleSettingsDialog;
import ru.dolika.experiment.signalID.SignalIdentifier;
import ru.dolika.experiment.signalID.dialog.SignalIDSettingsDialog;
import ru.dolika.experiment.workspace.Workspace;
import ru.dolika.thermocouple.graduate.GraduateConverter;
import ru.dolika.thermocouple.graduate.GraduateFileCreator;
import ru.dolika.ui.MemorableDirectoryChooser;

/**
 * @author Mikey
 *
 */
public class ExpLauncherMenu extends JMenuBar {
	private static final long serialVersionUID = 2882344753047235272L;

	/**
	 * 
	 */

	public ExpLauncherMenu(ExpLauncher parent) {
		Workspace workspace = Workspace.getInstance();
		JMenu fileMenu = new JMenu("Файл");
		this.add(fileMenu);

		JMenuItem fileNewSample = new JMenuItem("Новый образец");
		fileNewSample.addActionListener(e -> {
			Sample sample = workspace.getSample();
			if (sample != null) {
				int shouldSaveOption = JOptionPane.showConfirmDialog(parent,
						"Хотите сохранить изменения в образце перед созданием нового?", "Не забудь сохраниться",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (shouldSaveOption == JOptionPane.NO_OPTION) {
					sample = null;
					System.gc();
				}
				if (shouldSaveOption == JOptionPane.YES_OPTION) {
					SampleFactory.saveSample(workspace.getSampleFile().toString(), workspace.getSample());
					sample = null;
					System.gc();
				}
			}
			if (sample == null) {
				Sample s = SampleFactory.getSample();
				int status = SampleSettingsDialog.showSampleSettings(parent, s);
				if (status == SampleSettingsDialog.OK_BUTTON) {
					workspace.setSample(s);
					parent.setTitle(workspace.getSample().name);
					parent.statusBar.setText(String.format("%.6f", workspace.getSample().length));
					workspace.setSampleFile(null);
				} else {
					/* Добавить обработчик отказа */;
				}

			}
		});
		fileMenu.add(fileNewSample);
		JMenu fileOpen = new JMenu("Открыть");
		fileMenu.add(fileOpen);
		JMenuItem fileOpenProject = new JMenuItem("Образец...");
		fileOpenProject.addActionListener(e -> {
			Sample sample;
			if ((sample = workspace.getSample()) != null) {
				int shouldSaveOption = JOptionPane.showConfirmDialog(parent,
						"Хотите сохранить изменения в образце перед открытием нового?", "Не забудь сохраниться",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (shouldSaveOption == JOptionPane.NO_OPTION) {
					sample = null;
					System.gc();
				}
				if (shouldSaveOption == JOptionPane.YES_OPTION) {
					SampleFactory.saveSample(workspace.getSampleFile().toString(), sample);
					sample = null;
					System.gc();
				}
			}
			if (sample == null) {
				MemorableDirectoryChooser fileChooser = new MemorableDirectoryChooser(this.getClass());

				fileChooser.setDialogTitle("Отркыть...");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.resetChoosableFileFilters();
				fileChooser.addChoosableFileFilter(Sample.extensionFilter);
				fileChooser.setFileFilter(Sample.extensionFilter);

				int option = fileChooser.showOpenDialog(parent);
				if (option == JFileChooser.APPROVE_OPTION) {
					if (fileChooser.getSelectedFile() != null) {
						sample = SampleFactory.forBinary(fileChooser.getSelectedFile().getAbsolutePath());
						workspace.setSampleFile(fileChooser.getSelectedFile());
						workspace.setSample(null);
						System.out.println(sample);
						parent.setTitle(sample.name);
						parent.statusBar.setText(String.format("%.6f", sample.length));
					}
				}

			}

		});
		fileOpen.add(fileOpenProject);

		JMenuItem fileSave = new JMenuItem("Сохранить...");
		fileSave.addActionListener(e -> {
			workspace.save();
			if (workspace.getSampleFile() != null) {

				SampleFactory.saveSample(workspace.getSampleFile().toString(), workspace.getSample());
			} else {
				SampleFactory.saveSample(null, workspace.getSample());
			}
		});
		fileMenu.add(fileSave);

		JMenuItem fileSaveAs = new JMenuItem("Сохранить как...");
		fileSaveAs.addActionListener(e -> {
			Workspace w = Workspace.getInstance();
			w.save();
			w.setSampleFile(SampleFactory.saveSample(null, w.getSample()));
		});
		fileMenu.add(fileSaveAs);

		JMenu toolsMenu = new JMenu("Инструменты");
		this.add(toolsMenu);
		JMenuItem prepareZeroCrossing = new JMenuItem("Подготовить юстировку");
		prepareZeroCrossing.addActionListener(e -> {
			new Thread(new AdjustFileCreator(parent)).start();
		});
		toolsMenu.add(prepareZeroCrossing);

		JMenuItem prepareGrads = new JMenuItem("Подготовить градуировку");
		prepareGrads.addActionListener(e -> {
			new Thread(new GraduateFileCreator(parent)).start();
		});
		toolsMenu.add(prepareGrads);

		JMenuItem toolsDofiles = new JMenuItem("Произвести измерения");
		toolsDofiles.addActionListener(e -> {
			new Thread(new BatcherLaunch(parent, workspace)).start();
		});
		toolsMenu.add(toolsDofiles);

		JMenuItem toolsWatchFolder = new JMenuItem("Следить за папкой");
		toolsWatchFolder.addActionListener(event -> {
			try {
				FolderWatch fw = new FolderWatch(parent);
				fw.setVisible(true);
			} catch (Exception e) {
			}
		});
		toolsMenu.add(toolsWatchFolder);

		JMenuItem toolsFilterTuner = new JMenuItem("Настроить фильтр");
		toolsFilterTuner.addActionListener(e -> {
			try {
				FilterTunerGUI fw = new FilterTunerGUI(parent);
				fw.setVisible(true);
			} catch (Exception exception) {

			}
		});
		toolsMenu.add(toolsFilterTuner);

		toolsMenu.add(new JSeparator(SwingConstants.HORIZONTAL));

		JMenuItem convertTemperature = new JMenuItem("Преобразовать температуру");
		convertTemperature.addActionListener(e -> {
			new GraduateConverter(parent);
		});
		toolsMenu.add(convertTemperature);

		JMenu settingsMenu = new JMenu("Настройки");
		this.add(settingsMenu);
		JMenuItem chooseChannels = new JMenuItem("Выбрать каналы");
		chooseChannels.addActionListener(e -> {
			if (workspace.getSignalIDs() != null) {
				if (Debug.debug) {
					for (SignalIdentifier sd : workspace.getSignalIDs()) {
						System.out.println(sd);
					}
					System.out.println();
				}
			}
			SignalIDSettingsDialog sidsd = new SignalIDSettingsDialog(parent);
			sidsd.setModal(true);
			sidsd.setVisible(true);
		});
		settingsMenu.add(chooseChannels);
		JMenuItem sampleSettings = new JMenuItem("Настройки образца");
		sampleSettings.addActionListener(e -> {
			try {

				int status = SampleSettingsDialog.showSampleSettings(parent, workspace.getSample());

				if (status == SampleSettingsDialog.OK_BUTTON) {
					parent.setTitle(workspace.getSample().name);
					parent.statusBar.setText("" + workspace.getSample().length);
				}

			} catch (IllegalArgumentException e1) {
				JExceptionHandler.getExceptionHanlder().uncaughtException(Thread.currentThread(), e1);
				e1.printStackTrace();
			}

		});
		settingsMenu.add(sampleSettings);
	}
}
