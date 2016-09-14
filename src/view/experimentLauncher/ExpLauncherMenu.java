/**
 * 
 */
package view.experimentLauncher;

import static model.experiment.sample.SampleFactory.saveSampleXML;

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import controller.experiment.Analyzer.AdjustFileCreator;
import controller.experiment.Analyzer.TWMFoldersSelector;
import debug.Debug;
import debug.JExceptionHandler;
import model.experiment.sample.Sample;
import model.experiment.sample.SampleFactory;
import model.experiment.sample.SampleSettingsDialog;
import model.experiment.signalID.SignalIdentifier;
import model.experiment.workspace.Workspace;
import model.thermocouple.graduate.GraduateConverter;
import model.thermocouple.graduate.GraduateFileCreator;
import view.MemorableDirectoryChooser;
import view.experiment.Analyzer.Angstrem.AngstremAnalyzer;
import view.experiment.folderWatch.FolderWatch;
import view.experiment.signalID.dialog.SignalIDSettingsDialog;

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

		JMenuItem fileNewSample = new JMenuItem(
		        "Новый образец");
		fileNewSample.addActionListener(e -> {
			Sample sample = workspace.getSample();
			if (sample != null) {
				int shouldSaveOption = JOptionPane
				        .showConfirmDialog(parent,
				                "Хотите сохранить изменения в образце перед созданием нового?",
				                "Не забудь сохраниться",
				                JOptionPane.YES_NO_CANCEL_OPTION);
				if (shouldSaveOption == JOptionPane.NO_OPTION) {
					sample = null;
					System.gc();
				}
				if (shouldSaveOption == JOptionPane.YES_OPTION) {
					saveSampleXML(
					        workspace.getSampleFile()
					                .toString(),
					        workspace.getSample());
					sample = null;
					System.gc();
				}
			}
			if (sample == null) {
				Sample s = SampleFactory.getSample();
				int status = SampleSettingsDialog
				        .showSampleSettings(parent, s);
				if (status == SampleSettingsDialog.OK_BUTTON) {
					workspace.setSample(s);
					parent.setTitle(workspace.getSample()
					        .getName());
					parent.statusBar
					        .setText(String.format("%.6f",
					                workspace.getSample()
					                        .getLength()));
					workspace.setSampleFile(null);
				} else {
					/* Добавить обработчик отказа */;
				}

			}
		});
		fileMenu.add(fileNewSample);
		JMenu fileOpen = new JMenu("Открыть");
		fileMenu.add(fileOpen);
		JMenuItem fileOpenProject = new JMenuItem(
		        "Образец...");
		fileOpenProject.addActionListener(e -> {
			Sample sample;
			if ((sample = workspace.getSample()) != null) {
				int shouldSaveOption = JOptionPane
				        .showConfirmDialog(parent,
				                "Хотите сохранить изменения в образце перед открытием нового?",
				                "Не забудь сохраниться",
				                JOptionPane.YES_NO_CANCEL_OPTION);
				if (shouldSaveOption == JOptionPane.NO_OPTION) {
					sample = null;
					System.gc();
				}
				if (shouldSaveOption == JOptionPane.YES_OPTION) {
					saveSampleXML(
					        workspace.getSampleFile()
					                .toString(),
					        sample);
					sample = null;
					System.gc();
				}
			}
			if (sample == null) {
				MemorableDirectoryChooser fileChooser = new MemorableDirectoryChooser(
				        this.getClass());

				fileChooser.setDialogTitle("Открыть...");
				fileChooser.setFileSelectionMode(
				        JFileChooser.FILES_ONLY);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.resetChoosableFileFilters();
				fileChooser.addChoosableFileFilter(
				        Sample.getExtensionfilter());
				fileChooser.setFileFilter(
				        Sample.getExtensionfilter());

				int option = fileChooser
				        .showOpenDialog(parent);
				if (option == JFileChooser.APPROVE_OPTION) {
					if (fileChooser
					        .getSelectedFile() != null) {
						sample = SampleFactory
						        .forXML(fileChooser
						                .getSelectedFile()
						                .getAbsolutePath());
						workspace.setSampleFile(fileChooser
						        .getSelectedFile());
						workspace.setSample(null);
						System.out.println(sample);
						parent.setTitle(sample.getName());
						parent.statusBar.setText(
						        String.format("%.6f", sample
						                .getLength()));
					}
				}

			}

		});
		fileOpen.add(fileOpenProject);

		JMenuItem fileSave = new JMenuItem("Сохранить...");
		fileSave.addActionListener(e -> {
			workspace.save();
			if (workspace.getSampleFile() != null) {
				saveSampleXML(workspace.getSampleFile()
				        .toString(),
				        workspace.getSample());
			} else {
				MemorableDirectoryChooser chooser = new MemorableDirectoryChooser(
				        SampleFactory.class);
				chooser.setMultiSelectionEnabled(false);
				chooser.resetChoosableFileFilters();
				chooser.setFileSelectionMode(
				        JFileChooser.FILES_ONLY);
				chooser.addChoosableFileFilter(
				        Sample.extensionFilter);
				chooser.setFileFilter(
				        Sample.extensionFilter);

				int status = chooser.showSaveDialog(parent);
				if (status == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					workspace
					        .setSampleFile(
					                saveSampleXML(
					                        f.toString(),
					                        workspace
					                                .getSample()));
				}
			}
		});
		fileMenu.add(fileSave);

		JMenuItem fileSaveAs = new JMenuItem(
		        "Сохранить как...");
		fileSaveAs.addActionListener(e -> {
			workspace.save();
			workspace.setSampleFile(saveSampleXML(null,
			        workspace.getSample()));
		});
		fileMenu.add(fileSaveAs);

		JMenu toolsMenu = new JMenu("Инструменты");
		this.add(toolsMenu);
		JMenuItem prepareZeroCrossing = new JMenuItem(
		        "Подготовить юстировку");
		prepareZeroCrossing.addActionListener(e -> {
			new Thread(new AdjustFileCreator(parent))
			        .start();
		});
		toolsMenu.add(prepareZeroCrossing);

		JMenuItem prepareGrads = new JMenuItem(
		        "Подготовить градуировку");
		prepareGrads.addActionListener(e -> {
			new Thread(new GraduateFileCreator(parent))
			        .start();
		});
		toolsMenu.add(prepareGrads);

		JMenuItem toolsDirectoryDiffusivity = new JMenuItem(
		        "Произвести вычисления");
		toolsDirectoryDiffusivity.addActionListener(e -> {
			new Thread(new TWMFoldersSelector(parent))
			        .start();
		});
		toolsMenu.add(toolsDirectoryDiffusivity);

		JMenuItem toolsWatchFolder = new JMenuItem(
		        "Следить за папкой");
		toolsWatchFolder.addActionListener(e -> {
			try {
				FolderWatch fw = FolderWatch
				        .factory(parent);
				fw.setVisible(true);
			} catch (FileNotFoundException exc) {

			}
		});
		toolsMenu.add(toolsWatchFolder);

		toolsMenu.addSeparator();

		JMenuItem convertTemperature = new JMenuItem(
		        "Преобразовать температуру");
		convertTemperature.addActionListener(e -> {
			new GraduateConverter(parent);
		});
		toolsMenu.add(convertTemperature);

		JMenu settingsMenu = new JMenu("Настройки");
		this.add(settingsMenu);

		JMenuItem chooseChannels = new JMenuItem(
		        "Выбрать каналы");
		chooseChannels.addActionListener(e -> {
			if (workspace.getSignalIDs() != null) {
				if (Debug.isDebug()) {
					for (SignalIdentifier sd : workspace
					        .getSignalIDs()) {
						Debug.println(sd);
					}
					Debug.println();
				}
			}
			SignalIDSettingsDialog sidsd = new SignalIDSettingsDialog(
			        parent);
			sidsd.setModal(true);
			sidsd.setVisible(true);
		});
		settingsMenu.add(chooseChannels);

		JMenuItem sampleSettings = new JMenuItem(
		        "Настройки образца");
		sampleSettings.addActionListener(e -> {
			try {

				int status = SampleSettingsDialog
				        .showSampleSettings(parent,
				                workspace.getSample());

				if (status == SampleSettingsDialog.OK_BUTTON) {
					parent.setTitle(workspace.getSample()
					        .getName());
					parent.statusBar
					        .setText(""
					                + workspace.getSample()
					                        .getLength());
				}

			} catch (IllegalArgumentException e1) {
				JExceptionHandler.getExceptionHanlder()
				        .uncaughtException(
				                Thread.currentThread(), e1);
				e1.printStackTrace();
			}

		});
		settingsMenu.add(sampleSettings);

		JMenu angstromMenu = new JMenu("Ангстрем");
		this.add(angstromMenu);

		JMenuItem angstromCompute = new JMenuItem(
		        "МТВ Ангстрема");
		angstromCompute.addActionListener(
		        e -> new AngstremAnalyzer());
		angstromMenu.add(angstromCompute);
	}
}
