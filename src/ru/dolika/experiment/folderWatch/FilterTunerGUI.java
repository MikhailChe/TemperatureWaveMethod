package ru.dolika.experiment.folderWatch;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ru.dolika.debug.Debug;
import ru.dolika.debug.JExceptionHandler;
import ru.dolika.experiment.Analyzer.ExperimentFileReader;
import ru.dolika.experiment.Analyzer.FFT;
import ru.dolika.ui.MemorableDirectoryChooser;

public class FilterTunerGUI extends JDialog implements Runnable, WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1831549678406783975L;

	public File[] filesInFolder;
	public File folder;

	LogPointDrawer measurementViewer = null;

	Integer selectedChannel = null;

	public FilterTunerGUI(JFrame parent) {
		super(parent, false);
		SwingUtilities.invokeLater(() -> {

			try {
				String reply = JOptionPane.showInputDialog("Input channel number");
				if (reply != null) {
					selectedChannel = Integer.parseInt(reply);
				}
			} catch (Exception e) {
				JExceptionHandler.getExceptionHanlder().uncaughtException(Thread.currentThread(), e);
				e.printStackTrace();
			}
			if (selectedChannel == null) {
				return;
			}
			this.setTitle("Я смотрю за тобой!");
			this.addWindowListener(this);
			MemorableDirectoryChooser fileChooser = new MemorableDirectoryChooser(this.getClass());
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setDialogTitle("Выберите папку с данными");

			if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
				folder = fileChooser.getSelectedFile();
				if (folder == null) {
					this.setVisible(false);
					this.dispose();
				}
			} else {
				this.setVisible(false);
				this.dispose();
			}

			this.setTitle("Я смотрю за " + folder.getAbsolutePath());

			this.getContentPane().setLayout(new BorderLayout(16, 16));
			this.getContentPane().setPreferredSize(new Dimension(800, 600));
			this.getContentPane().setSize(this.getContentPane().getPreferredSize());
			this.pack();

			new Thread(this).start();
		});
	}

	public void checkNewFile() {
		File[] files = folder.listFiles(pathname -> {
			return pathname.getName().matches("^[0-9]+.txt$");
		});
		if (files != null) {
			if (filesInFolder == null) {
				if (files.length >= 1) {
					filesInFolder = files;
					for (File f : filesInFolder) {
						updateValuesForFile(f);
					}
				}
				return;
			}

			if (filesInFolder.length != files.length) {
				if (files.length >= 1) {
					filesInFolder = files;
					updateValuesForFile(filesInFolder[filesInFolder.length - 1]);
				}
				return;
			}
		}
	}

	public void updateValuesForFile(File f) {
		try {
			if (Debug.debug)
				System.out.println("Updating values for file " + f.toString());
			ExperimentFileReader reader = new ExperimentFileReader(f.toPath());
			double[] data = reader.getCroppedData()[selectedChannel];
			int minIndex = FFT.getIndex(0, reader.getExperimentFrequency() * 1000, data.length);
			int maxIndex = FFT.getIndex(400, reader.getExperimentFrequency() * 1000, data.length);
			/// minIndex = 1;
			/// maxIndex = data.length / 2;
			double[] fourierAbs = new double[maxIndex - minIndex];

			for (int i = minIndex; i < maxIndex; i++) {
				fourierAbs[i - minIndex] = FFT.getAbs(FFT.getFourierForIndex(data, i), 0);
			}
			if (Debug.debug)
				System.out.println("Fourier done");
			measurementViewer = new LogPointDrawer(fourierAbs);
			if (Debug.debug)
				System.out.println("Component created");
			this.getContentPane().removeAll();
			this.getContentPane().add(measurementViewer);
			this.repaint();
			this.revalidate();
			this.pack();

		} catch (IOException e) {
			JExceptionHandler.getExceptionHanlder().uncaughtException(Thread.currentThread(), e);
			e.printStackTrace();
		}
	}

	boolean isClosing = false;

	@Override
	public void run() {
		while (this.isDisplayable()) {
			if (isClosing)
				break;
			checkNewFile();

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				JExceptionHandler.getExceptionHanlder().uncaughtException(Thread.currentThread(), e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		isClosing = true;
	}

	@Override
	public void windowClosing(WindowEvent e) {
		isClosing = true;

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}
}
