package ru.dolika.debug;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class JExceptionHandler extends JFrame
		implements UncaughtExceptionHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8904598188535756544L;
	private static JExceptionHandler instance = null;

	synchronized public static JExceptionHandler getExceptionHanlder() {
		if (instance == null) {
			instance = new JExceptionHandler();
		}
		return instance;
	}

	JList<String> list;
	DefaultListModel<String> listModel;

	private JExceptionHandler() {
		super("Ошибки программы");
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		addWindowStateListener(e -> {
			if (JFrame.getFrames().length <= 1) {
				JExceptionHandler.this.dispose();
			}
		});

		setPreferredSize(new Dimension(320, 240));
		pack();
		listModel = new DefaultListModel<>();
		list = new JList<>(listModel);

		SwingUtilities.invokeLater(
				() -> getContentPane().add(list, BorderLayout.CENTER));
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		e.printStackTrace(ps);
		SwingUtilities.invokeLater(() -> {
			listModel.addElement(os.toString());
			this.setVisible(true);
			this.setAlwaysOnTop(true);
			this.setAlwaysOnTop(false);
		});
	}

	static {
		Thread.setDefaultUncaughtExceptionHandler(getExceptionHanlder());
	}

}
