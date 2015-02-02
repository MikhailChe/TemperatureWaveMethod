package ru.dolika.fft;

import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Main {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		JFrame frame = new JFrame("Drawer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		DrawingPlane main = new DrawingPlane();
		frame.getContentPane().setLayout(new GridLayout(1, 1));
		frame.getContentPane().add(main);

		JMenuBar bar = new JMenuBar();

		// Settings Menu, S - Mnemonics
		JMenu settingsMenu = new JMenu("Settings");
		settingsMenu.setMnemonic(KeyEvent.VK_S);
		bar.add(settingsMenu);

		// Settings -> showIndicies, I - Mnemonic
		JCheckBoxMenuItem showIndiciesMenuItem = new JCheckBoxMenuItem(
				"Show Indicies", DrawingPlane.shouldShowIndicies);
		showIndiciesMenuItem.setMnemonic(KeyEvent.VK_I);
		settingsMenu.add(showIndiciesMenuItem);

		// Settings -> filter, I - Mnemonic
		JCheckBoxMenuItem shouldFilterMenuItem = new JCheckBoxMenuItem(
				"Should Filter", DrawingPlane.shouldFilter);
		shouldFilterMenuItem.setMnemonic(KeyEvent.VK_F);
		settingsMenu.add(shouldFilterMenuItem);

		frame.setJMenuBar(bar);
		frame.pack();
		frame.setVisible(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		JFileChooser chooser = new JFileChooser(Main.class
				.getProtectionDomain().getCodeSource().getLocation().getPath());
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int chooserVal = chooser.showOpenDialog(frame);
		ExperimentReader ereader;

		if (chooserVal == JFileChooser.APPROVE_OPTION) {
			try {
				ereader = new ExperimentReader(chooser.getSelectedFile()
						.toPath());
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
				return;
			}
		} else {
			System.exit(0);
			return;
		}
		frame.setTitle(chooser.getSelectedFile().toString() + ", частота: "
				+ ereader.getExperimentFrequency());

		int colNum = ereader.getColumnCount();
		System.out.println(String.format("There are %d number of data columns",
				colNum));

		loadData(main, ereader, arrayIndex);
		showIndiciesMenuItem.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {

				Object o = e.getSource();
				if (o instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem i = (JCheckBoxMenuItem) o;
					if (i.isSelected()) {
						DrawingPlane.shouldShowIndicies = true;
					} else {
						DrawingPlane.shouldShowIndicies = false;
					}
					main.repaint();

				}

			}
		});
		shouldFilterMenuItem.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {

				Object o = e.getSource();
				if (o instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem i = (JCheckBoxMenuItem) o;
					if (i.isSelected()) {
						DrawingPlane.shouldFilter = true;
					} else {
						DrawingPlane.shouldFilter = false;
					}
					main.repaint();
				}
			}
		});

		main.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent arg0) {

				if (arg0.getWheelRotation() > 0) {
					main.zoomIn(arg0.getX(), arg0.getY());
				} else {
					main.zoomOut(arg0.getX(), arg0.getY());
				}
				main.repaint();
			}
		});
		main.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_UP) {
					if (arrayIndex < colNum - 1)
						arrayIndex++;
					loadData(main, ereader, arrayIndex);
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					if (arrayIndex != 0)
						arrayIndex--;
					loadData(main, ereader, arrayIndex);
				} else if (e.getKeyCode() == KeyEvent.VK_F) {
					DrawingPlane.shouldFilter = !DrawingPlane.shouldFilter;
					main.repaint();
				} else if (e.getKeyCode() == KeyEvent.VK_Q) {
					DrawingPlane.shouldFFT = !DrawingPlane.shouldFFT;
					main.repaint();
				} else if (e.getKeyCode() == KeyEvent.VK_W) {
					DrawingPlane.isWavelet = !DrawingPlane.isWavelet;
					main.repaint();
				} else if (e.getKeyCode() == KeyEvent.VK_S) {
					loadData(main, ereader, arrayIndex);
				} else if (e.getKeyCode() == KeyEvent.VK_N) {
					loadData(main, ereader, arrayIndex);
				} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					if (DrawingPlane.maxHarmony > 1) {
						DrawingPlane.maxHarmony--;
					}
					main.repaint();
				} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					if (DrawingPlane.maxHarmony < 301) {
						DrawingPlane.maxHarmony++;
					}
					main.repaint();

				}

			}
		});

	}

	private static int arrayIndex = 0;

	private static void loadData(Object obj, ExperimentReader ereader, int index) {
		if (obj instanceof DrawingPlane) {
			DrawingPlane main = (DrawingPlane) obj;
			if (index < 0)
				return;
			//main.data = SignalAdder.getOnePeriod(ereader.getDataColumn(0),
			//		ereader.getDataColumn(index));
			 main.data = ereader.getDataColumn(index);
			DrawingPlane.fftIndex = 1;
			DrawingPlane.expFreq = ereader.getExperimentFrequency();
			main.repaint();
		}
	}
}
