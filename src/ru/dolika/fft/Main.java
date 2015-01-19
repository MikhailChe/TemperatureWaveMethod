package ru.dolika.fft;

import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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
		DrawingPlane main2 = new DrawingPlane();
		frame.getContentPane().setLayout(new GridLayout(2, 1));
		frame.getContentPane().add(main);
		frame.getContentPane().add(main2);
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
		loadData(main2, ereader, (arrayIndex + 1) % colNum);
		main.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent arg0) {

				if (arg0.getWheelRotation() > 0) {
					main.zoomIn(arg0.getX(), arg0.getY());
				} else {
					main.zoomOut(arg0.getX(), arg0.getY());
				}
				main.repaint();
				main2.repaint();
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
					main2.repaint();
				} else if (e.getKeyCode() == KeyEvent.VK_Q) {
					DrawingPlane.shouldFFT = !DrawingPlane.shouldFFT;
					main.repaint();
					main2.repaint();
				} else if (e.getKeyCode() == KeyEvent.VK_W) {
					DrawingPlane.isWavelet = !DrawingPlane.isWavelet;
					main.repaint();
					main2.repaint();
				} else if (e.getKeyCode() == KeyEvent.VK_S) {
					loadData(main, ereader, arrayIndex);
				} else if (e.getKeyCode() == KeyEvent.VK_N) {
					loadData(main, ereader, arrayIndex);
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
			double[] pulses = ereader.getDataColumn(0);
			double pMinVal = Integer.MAX_VALUE;
			double pMaxVal = Integer.MIN_VALUE;
			for (int i = 0; i < pulses.length; i++) {
				if (pulses[i] > pMaxVal) {
					pMaxVal = pulses[i];
				}
				if (pulses[i] < pMinVal) {
					pMinVal = pulses[i];
				}
			}
			Vector<Integer> indicies = new Vector<Integer>();
			boolean trigger = false;
			for (int i = 0; i < pulses.length; i++) {
				if (pulses[i] > pMinVal + (pMaxVal - pMinVal) * 0.5) {
					if (!trigger) {
						trigger = true;
						indicies.add(i);
					}
				} else {
					if (trigger) {
						trigger = false;
					}
				}
			}
			int minDistance = Integer.MAX_VALUE;
			for (int i = 0; i < indicies.size() - 1; i++) {
				int distance = indicies.get(i + 1) - indicies.get(i);
				if (distance < minDistance) {
					minDistance = distance;
				}
			}
			if (minDistance % 2 == 1) {
				minDistance--;
			}
			indicies.remove(indicies.size() - 1);
			double data[] = ereader.getDataColumn(index);
			double dataS[] = new double[minDistance];
			for (int i = 0; i < indicies.size(); i++) {
				int curIndex = indicies.get(i);
				for (int j = 0; j < dataS.length; j++) {
					dataS[j] = data[j + curIndex];
				}
			}
			main.data = dataS;
			DrawingPlane.expFreq = ereader.getExperimentFrequency();
			main.repaint();
		}
	}
}
