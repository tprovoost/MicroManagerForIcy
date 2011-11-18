package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigurationWizardWrapper;

import icy.network.NetworkUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.micromanager.conf.Device;
import org.micromanager.conf.MMConfigFileException;
import org.micromanager.conf.MicroscopeModel;
import org.micromanager.utils.ReportingUtils;

//Referenced classes of package org.micromanager.conf:
//         Device, MMConfigFileException, MicroscopeModel, DevicesPage

public class AddDeviceDlg extends JDialog implements MouseListener, TreeSelectionListener {
	
	class TreeNodeShowsDeviceAndDescription extends DefaultMutableTreeNode {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3078781906745731974L;

		public TreeNodeShowsDeviceAndDescription(String value) {
			super(value);
		}

		public String toString() {
			String ret = "";
			Object uo = getUserObject();
			if (null != uo)
				if (uo.getClass().isArray()) {
					Object userData[] = (Object[]) (Object[]) uo;
					if (2 < userData.length)
						ret = (new StringBuilder()).append(userData[1].toString()).append(" | ").append(userData[2].toString()).toString();
				} else {
					ret = uo.toString();
				}
			return ret;
		}

		public Object[] getUserDataArray() {
			Object ret[] = null;
			Object uo = getUserObject();
			if (null != uo && uo.getClass().isArray()) {
				Object userData[] = (Object[]) (Object[]) uo;
				if (1 < userData.length)
					ret = userData;
			}
			return ret;
		}

	}

	class TreeMouseListener extends MouseAdapter {

		public void mousePressed(MouseEvent e) {
			if (2 == e.getClickCount() && addDevice())
				rebuildTable();
		}
		
	}

	class TreeWContextMenu extends JTree implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5351790156074826454L;

		public void actionPerformed(ActionEvent ae) {
			if (ae.getActionCommand().equals("help")) {
				d_.displayDocumentation();
			}
			else if (ae.getActionCommand().equals("add") && d_.addDevice())
				d_.rebuildTable();
		}

		JPopupMenu popupMenu_;
		AddDeviceDlg d_;

		public TreeWContextMenu(DefaultMutableTreeNode n, AddDeviceDlg d) {
			super(n);
			d_ = d;
			popupMenu_ = new JPopupMenu();
			JMenuItem jmi = new JMenuItem("Add");
			jmi.setActionCommand("add");
			jmi.addActionListener(this);
			popupMenu_.add(jmi);
			jmi = new JMenuItem("Help");
			jmi.setActionCommand("help");
			jmi.addActionListener(this);
			popupMenu_.add(jmi);
			popupMenu_.setOpaque(true);
			popupMenu_.setLightWeightPopupEnabled(true);
			addMouseListener(new MouseAdapter() {

				public void mouseReleased(MouseEvent e) {
					if (e.isPopupTrigger())
						popupMenu_.show((JComponent) e.getSource(), e.getX(), e.getY());
				}

			});
		}
	}

	public AddDeviceDlg(MicroscopeModel model, DevicesPage devicesPage) {
		setModal(true);
		setResizable(false);
		getContentPane().setLayout(null);
		setTitle("Add Device");
		setBounds(400, 100, 596, 529);
		devicesPage_ = devicesPage;
		JButton addButton = new JButton();
		addButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (addDevice())
					rebuildTable();
			}

		});
		addButton.setText("Add");
		addButton.setBounds(490, 10, 93, 23);
		getContentPane().add(addButton);
		getRootPane().setDefaultButton(addButton);
		JButton doneButton = new JButton();
		doneButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}

		});
		doneButton.setText("Done");
		doneButton.setBounds(490, 39, 93, 23);
		getContentPane().add(doneButton);
		getRootPane().setDefaultButton(doneButton);
		model_ = model;
		JButton documentationButton = new JButton();
		documentationButton.setText("Help");
		documentationButton.setBounds(490, 68, 93, 23);
		getContentPane().add(documentationButton);
		cbShowAll_ = new JCheckBox("Show all");
		cbShowAll_.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				buildTree(model_);
				scrollPane_.setViewportView(theTree_);
			}

		});
		cbShowAll_.setBounds(487, 462, 81, 23);
		cbShowAll_.setSelected(false);
		getContentPane().add(cbShowAll_);
		documentationButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				displayDocumentation();
			}

		});
		buildTree(model);
		scrollPane_ = new JScrollPane(theTree_);
		scrollPane_.setBounds(10, 10, 471, 475);
		getContentPane().add(scrollPane_);
	}

	private void buildTree(MicroscopeModel model) {
		Device devices_[] = null;
		if (cbShowAll_.isSelected())
			devices_ = model.getAvailableDeviceList();
		else
			devices_ = model.getAvailableDevicesCompact();
		String thisLibrary = "";
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Devices supported by Micro-Manager");
		TreeNodeShowsDeviceAndDescription node = null;
		for (int idd = 0; idd < devices_.length; idd++) {
			if (0 != thisLibrary.compareTo(devices_[idd].getLibrary())) {
				node = new TreeNodeShowsDeviceAndDescription(devices_[idd].getLibrary());
				root.add(node);
				thisLibrary = devices_[idd].getLibrary();
			}
			Object userObject[] = { devices_[idd].getLibrary(), devices_[idd].getAdapterName(), devices_[idd].getDescription() };
			TreeNodeShowsDeviceAndDescription aLeaf = new TreeNodeShowsDeviceAndDescription("");
			aLeaf.setUserObject(((Object) (userObject)));
			node.add(aLeaf);
		}

		theTree_ = new TreeWContextMenu(root, this);
		theTree_.addTreeSelectionListener(this);
		MouseListener ml = new TreeMouseListener();
		theTree_.addMouseListener(ml);
		theTree_.setRootVisible(false);
		theTree_.setShowsRootHandles(true);
	}

	private void displayDocumentation() {
		String gotoURL = (new StringBuilder()).append(documentationURLroot_).append(libraryDocumentationName_).toString();
		System.out.println("asking for : "+gotoURL);
		NetworkUtil.openURL(gotoURL);
	}

	private void rebuildTable() {
		devicesPage_.rebuildTable();
	}

	public void mouseClicked(MouseEvent mouseevent) {
	}

	public void mousePressed(MouseEvent mouseevent) {
	}

	public void mouseReleased(MouseEvent mouseevent) {
	}

	public void mouseEntered(MouseEvent mouseevent) {
	}

	public void mouseExited(MouseEvent mouseevent) {
	}

	protected boolean addDevice() {
		int srows[] = theTree_.getSelectionRows();
		if (srows == null)
			return false;
		if (0 < srows.length && 0 < srows[0]) {
			TreeNodeShowsDeviceAndDescription node = (TreeNodeShowsDeviceAndDescription) theTree_.getLastSelectedPathComponent();
			Object userData[] = node.getUserDataArray();
			if (null == userData && 1 == node.getLeafCount()) {
				node = (TreeNodeShowsDeviceAndDescription) node.getChildAt(0);
				userData = node.getUserDataArray();
				if (null == userData)
					return false;
			}
			boolean validName = false;
			do {
				if (validName)
					break;
				String name = JOptionPane.showInputDialog("Please type in the new device name", userData[1].toString());
				if (name == null)
					return false;
				Device newDev = new Device(name, userData[0].toString(), userData[1].toString(), userData[2].toString());
				try {
					model_.addDevice(newDev);
					validName = true;
				} catch (MMConfigFileException e) {
					ReportingUtils.showError(e);
					return false;
				}
			} while (true);
		}
		return true;
	}

	public void valueChanged(TreeSelectionEvent event) {
		int srows[] = theTree_.getSelectionRows();
		if (null != srows && 0 < srows.length && 0 < srows[0]) {
			TreeNodeShowsDeviceAndDescription node = (TreeNodeShowsDeviceAndDescription) theTree_.getLastSelectedPathComponent();
			Object uo = node.getUserObject();
			if (uo != null)
				if (uo.getClass().isArray())
					libraryDocumentationName_ = ((Object[]) (Object[]) uo)[0].toString();
				else
					libraryDocumentationName_ = uo.toString();
		}
	}

	private static final long serialVersionUID = 1L;
	private MicroscopeModel model_;
	private DevicesPage devicesPage_;
	private TreeWContextMenu theTree_;
	final String documentationURLroot_ = "https://valelab.ucsf.edu/~MM/MMwiki/index.php/";
	String libraryDocumentationName_;
	private JCheckBox cbShowAll_;
	private JScrollPane scrollPane_;

}
