package ru.dolika.experimentLauncher;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

import ru.dolika.experimentAnalyzer.BatcherLaunch;

public class ExpLauncher extends JFrame {
	private static final long serialVersionUID = 5151838479190943050L;

	public static void main(String[] args) {
		new ExpLauncher();
	}

	Button b = null;

	ExpLauncher() {
		super("Launcher");

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("Файл");
		menuBar.add(fileMenu);
		JMenu fileOpen = new JMenu("Открыть");
		fileMenu.add(fileOpen);

		JMenuItem fileOpenProject = new JMenuItem("Проект...");
		fileOpen.add(fileOpenProject);
		this.setJMenuBar(menuBar);

		JMenu toolsMenu = new JMenu("Инструменты");
		menuBar.add(toolsMenu);
		JMenuItem prepareZeroCrossing = new JMenuItem("Подготовить юстировку");
		toolsMenu.add(prepareZeroCrossing);
		JMenuItem prepareGrads = new JMenuItem("Подготовить градуировку");
		toolsMenu.add(prepareGrads);

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
