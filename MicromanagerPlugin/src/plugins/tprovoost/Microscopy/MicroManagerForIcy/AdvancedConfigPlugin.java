package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.gui.dialog.ConfirmDialog;
import icy.gui.dialog.MessageDialog;
import icy.main.Icy;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.micromanager.utils.StateItem;

public class AdvancedConfigPlugin extends MicroscopePlugin {

	private boolean waitingForObjectiveChange = false;
	private boolean waitingForBlockChange = false;
	private MyJDialog dlg;

	@Override
	public void start() {
		dlg = new MyJDialog();
		dlg.setVisible(true);
	}

	private class MyJDialog extends JDialog {

		/** */
		private static final long serialVersionUID = 1L;
		JLabel lblCurrentObjectiveTurretGroup;
		JLabel lblCurrentFilterBLockGroup;

		public MyJDialog() {
			super(Icy.getMainInterface().getFrame(), "Advanced Dialog", false);

			JPanel panelDevices = new JPanel(new GridLayout(2, 3));

			if (mCore == null) {
				
			}
			
			// OBJECTIVES TURRET
			panelDevices.add(new JLabel("Objectives Turret Config"));
			lblCurrentObjectiveTurretGroup = new JLabel("");
			String turretGroup = mCore.getCurrentObjectiveTurretGroup();
			if (turretGroup != null)
				lblCurrentObjectiveTurretGroup.setText(turretGroup);
			panelDevices.add(lblCurrentObjectiveTurretGroup);
			JButton btnSetTurretConfig = new JButton("Set");
			btnSetTurretConfig.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					MessageDialog.showDialog("<html>Please modify the preset of the objective turret's group.<br/>" + "<br/><i>Exemple: switch from 10x to 20x.</i></html>");
					if (waitingForBlockChange)
						waitingForBlockChange = false;
					waitingForObjectiveChange = true;
				}
			});
			panelDevices.add(btnSetTurretConfig);

			// FILTER BLOCKS
			panelDevices.add(new JLabel("Filter Blocks Config"));
			lblCurrentFilterBLockGroup = new JLabel("");
			String filterBlock = mCore.getCurrentFilterBlockGroup();
			if (filterBlock != null)
				lblCurrentFilterBLockGroup.setText(filterBlock);
			panelDevices.add(lblCurrentFilterBLockGroup);
			JButton btnSetFilterBlock = new JButton("Set");
			btnSetFilterBlock.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					MessageDialog.showDialog("<html>Please modify the preset of the objective turret's group.<br/>" + "<br/><i>Exemple: switch from 10x to 20x.</i></html>");
					if (waitingForObjectiveChange)
						waitingForObjectiveChange = false;
					waitingForBlockChange = true;
				}
			});
			panelDevices.add(btnSetFilterBlock);

			JPanel mainPanel = new JPanel(new BorderLayout());
			JButton btnClose = new JButton("Close");
			mainPanel.add(panelDevices, BorderLayout.CENTER);
			mainPanel.add(btnClose, BorderLayout.SOUTH);
			setLayout(new BorderLayout());
			add(mainPanel, BorderLayout.CENTER);
			pack();
		}
	}

	@Override
	public void notifyConfigAboutToChange(StateItem item) throws Exception {
	}

	@Override
	public void notifyConfigChanged(StateItem item) throws Exception {
		if (waitingForObjectiveChange) {
			if (ConfirmDialog.confirm("Confirmation", "Are you sure you want to set this as your Objective Turret configuration ?")) {
				mCore.setCurrentObjectiveTurretGroup(item.group);
				String res = mCore.getCurrentObjectiveTurretGroup();
				if (res != null)
					dlg.lblCurrentObjectiveTurretGroup.setText(res);
			}
			waitingForObjectiveChange = false;
		} else if (waitingForBlockChange) {
			if (ConfirmDialog.confirm("Confirmation", "Are you sure you want to set this as your Filter Block configuration ?")) {
				mCore.setCurrentObjectiveTurretGroup(item.group);
				String res = mCore.getCurrentFilterBlockGroup();
				if (res != null)
					dlg.lblCurrentFilterBLockGroup.setText(res);
			}
			waitingForBlockChange = false;
		}
	}

	@Override
	public void MainGUIClosed() {
		dlg.setVisible(false);
	}

}
