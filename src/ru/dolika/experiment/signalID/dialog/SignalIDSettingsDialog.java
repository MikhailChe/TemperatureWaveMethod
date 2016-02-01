package ru.dolika.experiment.signalID.dialog;

import java.awt.Dialog;
import java.awt.Frame;
import java.util.Arrays;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

/**
 * Диалог настроек каналов
 * 
 * @author Mikey
 *
 */
public class SignalIDSettingsDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2538880279274825385L;

	public SignalIDSettingsDialog() {
		super();
		initDialog();
	}

	public SignalIDSettingsDialog(Dialog owner) {
		super(owner, true);
		initDialog();
	}

	public SignalIDSettingsDialog(Frame owner) {
		super(owner, true);
		initDialog();
	}

	private boolean initialized = false;

	private void initDialog() {
		if (initialized)
			return;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		add(new SignalIDAddNewPanel());

		pack();
	}

}
