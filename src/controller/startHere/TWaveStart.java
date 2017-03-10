package controller.startHere;

import debug.Debug;
import javafx.application.Platform;
import view.experimentLauncher.ExpLauncher;

public class TWaveStart {

	public static void main(String[] args) {
		Debug.println("public static void main(String[] args)");
		Platform.setImplicitExit(false);
		new ExpLauncher();
	}
}
