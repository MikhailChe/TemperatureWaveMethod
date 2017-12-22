package debug;

import static debug.Debug.println;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class JExceptionHandler extends JFrame implements UncaughtExceptionHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8904598188535756544L;
	private static JExceptionHandler instance = null;

	public static JExceptionHandler getExceptionHanlder() {
		if (instance == null) {
			synchronized (JExceptionHandler.class) {
				if (instance == null) {
					instance = new JExceptionHandler();
				}
			}
		}
		return instance;
	}

	final JScrollPane pane;
	final ScrollablePanel excPanel;

	private JExceptionHandler() {
		super("Ошибки программы");
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		addWindowStateListener(e -> {
			if (Arrays
					.stream(Frame.getFrames())
					.filter(f -> f != null)
					.filter(f -> f.isDisplayable())
					.toArray().length <= 1) {
				this.setVisible(false);
				JExceptionHandler.this.dispose();
			}
		});

		setPreferredSize(new Dimension(320, 240));
		setMinimumSize(new Dimension(320, 240));
		pack();

		excPanel = new ScrollablePanel();
		BoxLayout layout = new BoxLayout(excPanel, BoxLayout.Y_AXIS);

		excPanel.setLayout(layout);

		pane = new JScrollPane(excPanel);

		pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		pane.setAutoscrolls(true);

		SwingUtilities.invokeLater(() -> getContentPane().add(pane, BorderLayout.CENTER));
	}

	public static void showException(Throwable e) {
		showException(Thread.currentThread(), e);
	}

	public static void showException(Thread t, Throwable e) {
		if (instance == null)
			getExceptionHanlder();
		instance.uncaughtException(t, e);
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		println("Thread exception: " + t.getId() + ":" + t.getName());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		e.printStackTrace(ps);
		SwingUtilities.invokeLater(() -> {
			JTextArea area = new JTextArea(os.toString());
			area.setLineWrap(true);
			area.setEditable(false);
			excPanel.add(area);
			excPanel.validate();
			excPanel.revalidate();
			this.setVisible(true);
			this.setAlwaysOnTop(true);
			this.setAlwaysOnTop(false);
		});
	}

	static {
		Thread.setDefaultUncaughtExceptionHandler(getExceptionHanlder());
	}

	private static class ScrollablePanel extends JPanel implements Scrollable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6127700361483790187L;

		public ScrollablePanel() {
			super(true);
		}

		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return super.getPreferredSize(); // tell the JScrollPane that we want to be our 'preferredSize' - but later,
												// we'll say that vertically, it should scroll.
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 16;// set to 16 because that's what you had in your code.
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 16;// set to 16 because that's what you had set in your code.
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;// track the width, and re-size as needed.
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false; // we don't want to track the height, because we want to scroll vertically.
		}
	}

}
