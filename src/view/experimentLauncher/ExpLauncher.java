package view.experimentLauncher;

import static debug.JExceptionHandler.showException;
import static java.lang.Thread.currentThread;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import debug.Debug;
import javafx.application.Platform;
import model.experiment.sample.Sample;
import model.experiment.workspace.Workspace;

public class ExpLauncher extends JFrame {
    private static final long serialVersionUID = 5151838479190943050L;

    final private Workspace workspace;
    JLabel statusBar;
    JDesktopPane desktop;

    public void createAndShowGUI() {
	Debug.println("Creating gui");

	try {
	    setIconImage(ImageIO.read(ExpLauncher.class.getResourceAsStream("/resources/icon.png")));
	} catch (Exception e) {
	    e.printStackTrace();
	}

	statusBar = new JLabel();
	try {
	    desktop = new JDesktopPane() {
		private static final long serialVersionUID = 4767387908656225258L;
		private Image img = ImageIO
			.read(ExpLauncher.class.getResourceAsStream("/resources/windows_xp_bliss-wide.jpg"));

		@Override
		public void paintComponent(Graphics g) {
		    int imgWidth = img.getWidth(null);
		    int imgHeight = img.getHeight(null);

		    int width = getWidth();
		    int height = getHeight();

		    float imgratio = (float) imgWidth / (float) imgHeight;

		    int crpH, crpW, crpX = 0, crpY = 0;

		    if ((float) width / (float) height > imgratio) {
			crpH = (int) ((float) imgWidth * (float) height / (float) width);
			crpW = imgWidth;
			crpY = (imgHeight - crpH) / 2;
		    } else {
			crpW = (int) ((float) imgHeight * width / height);
			crpH = imgHeight;
			crpX = (imgWidth - crpW) / 2;
		    }

		    g.drawImage(img, 0, 0, getWidth(), getHeight(), crpX, crpY, crpX + crpW, crpY + crpH, null);
		}
	    };
	} catch (IOException e) {
	    e.printStackTrace();
	}
	if (desktop == null) {
	    desktop = new JDesktopPane();
	}
	desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

	setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

	JMenuBar menuBar = new ExpLauncherMenu(ExpLauncher.this);
	ExpLauncher.this.setJMenuBar(menuBar);

	getContentPane().setLayout(new BorderLayout(16, 16));

	getContentPane().add(statusBar, BorderLayout.SOUTH);

	getContentPane().add(desktop, BorderLayout.CENTER);

	getContentPane().setPreferredSize(new Dimension(640, 480));

	if (workspace != null) {
	    Sample sample;
	    if ((sample = workspace.getSample()) != null) {
		ExpLauncher.this.setTitle(sample.getName());
		ExpLauncher.this.statusBar.setText("" + sample.getLength());
	    }
	}
	pack();
	setLocationRelativeTo(null);
    }

    public ExpLauncher() {
	super("Обработчик данных");

	Debug.println("Getting workspace instance");
	workspace = Workspace.getInstance();
	Debug.println("Got workspace instance");
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (Exception e) {
	    showException(currentThread(), e);
	}
	SwingUtilities.invokeLater(this::createAndShowGUI);
	setName("Главный экран");
	addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosed(WindowEvent e) {
		Debug.println(e);
		Platform.exit();
	    }

	});
    }

}
