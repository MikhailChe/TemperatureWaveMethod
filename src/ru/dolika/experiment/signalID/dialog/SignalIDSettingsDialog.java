package ru.dolika.experiment.signalID.dialog;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import ru.dolika.experiment.signalID.BaseSignalID;
import ru.dolika.experiment.signalID.DCsignalID;
import ru.dolika.experiment.signalID.SignalIdentifier;

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
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		Container contentPane = getContentPane();
		SignalIdentifier[] SHIFTS = { null, new DCsignalID(),
				// new AdjustmentSignalID(),
				new BaseSignalID(new File("config/just/20160428newAmpChangeTauLastCascade.txt")),
				new BaseSignalID(new File("config/just/20160427oldAmp.txt"))
				// new AdjustmentSignalID(),
		};
		for (SignalIdentifier shift : SHIFTS) {
			if (shift instanceof BaseSignalID) {
				contentPane.add(new BaseSignalIDPanel((BaseSignalID) shift));
			} else if (shift instanceof DCsignalID) {
				contentPane.add(new DCSignalIDPanel((DCsignalID) shift));
			}
		}
		contentPane.add(new SignalIDAddNewPanel());

		pack();
	}

}
