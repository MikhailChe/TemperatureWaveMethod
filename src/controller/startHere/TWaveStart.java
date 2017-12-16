package controller.startHere;

import javax.swing.SwingUtilities;

import debug.Debug;
import javafx.application.Platform;
import view.experimentLauncher.ExpLauncher;

public class TWaveStart {

	public static void main(String[] args) {
		Debug.println("public static void main(String[] args)");
		Platform.setImplicitExit(false);
		ExpLauncher launcher = new ExpLauncher();
		SwingUtilities.invokeLater(() -> launcher.setVisible(true));
	}

}
