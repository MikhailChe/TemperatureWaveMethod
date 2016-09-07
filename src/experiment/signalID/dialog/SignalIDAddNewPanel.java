package experiment.signalID.dialog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * Кнопка для добавления нового канала в список
 * 
 * @author Mikey
 *
 */
public class SignalIDAddNewPanel extends JButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6251168837166000430L;

	public SignalIDAddNewPanel() {

		super("Добавить канал", getIconImage());
	}

	public static Icon getIconImage() {
		BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setColor(Color.GREEN);
		g.setStroke(new BasicStroke(15f));
		g.drawLine(0, 16, 32, 16);
		g.drawLine(16, 0, 16, 32);
		return new ImageIcon(image);
	}
}
