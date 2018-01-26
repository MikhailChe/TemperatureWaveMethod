package view.experiment.signalID.dialog;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import model.signalID.BaseSignalID;
import model.signalID.DCsignalID;
import model.signalID.SignalIdentifier;
import model.workspace.Workspace;

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
		Container contentPane = getContentPane();

		contentPane.removeAll();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		List<SignalIdentifier> sigids = Workspace.getInstance().getSignalIDs();
		if (Workspace.getInstance().getSignalIDs() != null) {
			// sigids.forEach(action);
			sigids.forEach(sigid -> {
				SignalIDPanel panel;
				if (sigid instanceof BaseSignalID) {
					panel = new BaseSignalIDPanel((BaseSignalID) sigid);
				} else if (sigid instanceof DCsignalID) {
					panel = new DCSignalIDPanel((DCsignalID) sigid);
				} else {
					panel = new SignalIDPanel();
				}
				panel.deleteButton.addActionListener(e -> {
					Workspace.getInstance().getSignalIDs().remove(sigid);
					getParent().remove(this);
					SwingUtilities.invokeLater(() -> {
						initialized = false;
						initDialog();
					});
				});
				SwingUtilities.invokeLater(() -> contentPane.add(panel));
			});
		}
		SignalIDAddNewPanel addNewPanel = new SignalIDAddNewPanel();
		addNewPanel.addActionListener(e -> {
			String[] options = new String[] { "Постоянка", "Переменка", "Пустой канал" };
			int optionNumber = JOptionPane.showOptionDialog(SignalIDSettingsDialog.this, "Какой тип канала добавить?",
					"Выберите тип канала", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
					SignalIDAddNewPanel.getIconImage(), options, options[0]);
			if (optionNumber >= 0) {
				Workspace space = Workspace.getInstance();
				switch (optionNumber) {
				case 0:
					space.getSignalIDs().add(new DCsignalID());
					break;
				case 1:
					space.getSignalIDs().add(new BaseSignalID());
					break;
				case 2:
					space.getSignalIDs().add(null);
					break;
				default:
					break;
				}
				SwingUtilities.invokeLater(() -> {
					initialized = false;
					initDialog();
				});
			}
		});
		SwingUtilities.invokeLater(() -> {
			contentPane.add(addNewPanel);
			pack();
		});

		initialized = true;
	}

}
