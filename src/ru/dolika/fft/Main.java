package ru.dolika.fft;

import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jtransforms.fft.DoubleFFT_1D;

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
		if (colNum > 1) {
			// Making calculations
			double[] col1 = ereader.getDataColumn(0);
			double[] col2 = ereader.getDataColumn(1);
			double[] FFTdata = Arrays.copyOf(col1, col1.length * 2);
			DoubleFFT_1D fft = new DoubleFFT_1D(col1.length);
			fft.realForwardFull(FFTdata);
			int index1 = FFT.getIndex(22.05, 44100, col1.length);
			int index2 = FFT.getIndex(44.1, 44100, col1.length);

			double angle1 = Math.toDegrees(FFT.getArgument(FFTdata, index1));

			FFTdata = Arrays.copyOf(col2, col2.length * 2);
			fft.realForwardFull(FFTdata);

			double angle2 = Math.toDegrees(FFT.getArgument(FFTdata, index2));

			double targetAngle = ((((angle1 + 360) * 2) % 360) - angle2 - 90);
			System.out.println("Target Angle: " + targetAngle);
		}
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
				} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					if (DrawingPlane.RCamountHP > 10) {
						DrawingPlane.RCamountHP--;
						DrawingPlane.RCamountLP--;
						System.out.println(DrawingPlane.RCamountHP);
					}
					main.repaint();
					main2.repaint();
				} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					DrawingPlane.RCamountHP++;
					DrawingPlane.RCamountLP++;
					System.out.println(DrawingPlane.RCamountHP);
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
			main.data = ereader.getDataColumn(index);
			DrawingPlane.expFreq = ereader.getExperimentFrequency();
			main.repaint();
		}
	}

}
