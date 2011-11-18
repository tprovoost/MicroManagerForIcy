package plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.border.Border;

import org.micromanager.acquisition.ComponentTitledBorder;

/**
 * Simple panel with a Title and a border.
 * 
 * @author Thomas Provoost
 */
public class ComponentTitledPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ComponentTitledBorder compTitledBorder;
	public boolean borderSet_ = false;
	public Component titleComponent;

	public ComponentTitledPanel() {
	}

	public void setBorder(Border border) {
		if ((this.compTitledBorder != null) && (this.borderSet_))
			this.compTitledBorder.setBorder(border);
		else
			super.setBorder(border);
	}

	public Border getBorder() {
		return this.compTitledBorder;
	}

	public void setTitleFont(Font font) {
		this.titleComponent.setFont(font);
	}
}