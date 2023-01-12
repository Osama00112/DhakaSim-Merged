package thesisfinal;

import javax.swing.*;

public class DhakaSimFrame extends JFrame {

	static JSlider showProgressSlider;

	public DhakaSimFrame() {
		getContentPane().add(new OptionPanel(this));
		setTitle("DhakaSim");
		setSize(1250, 700);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

}
