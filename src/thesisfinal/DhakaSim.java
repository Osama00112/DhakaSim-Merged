package thesisfinal;

import static thesisfinal.Controller.startSimulation;

public class DhakaSim {
	static boolean viewMode = true;
	public static void main(String[] args) {
		/*for (int i=0; i<5000; i++) {
			System.out.println(Utilities.getUniformlyDistributedRandomBlockageOfObject(4));
		}
		exit(0);*/

		//System.out.println("Started at " + new Date());

		new Parameters();
		new Controller();
		if (viewMode) {
			new DhakaSimFrame();
		}
		else {
			startSimulation();
		}
	}
}
