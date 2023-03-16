package thesisfinal;

import java.util.Scanner;

import static thesisfinal.Controller.startSimulation;

public class DhakaSim {
	static boolean viewMode = false;
	

	public static int dummyVar = 1;

	public static void main(String[] args) {

		/*for (int i=0; i<5000; i++) {
			System.out.println(Utilities.getUniformlyDistributedRandomBlockageOfObject(4));
		}
		exit(0);*/
		//System.out.println("Started at " + new Date());

		//System.out.println("arg 0:"+args[0]);
		boolean isGaussian = args[0].equalsIgnoreCase("gauss") ? true: false;

		/*parameters arguments:
		isGaussian : gaussian or uniform
		arg[1] :	time
		arg[2] :	speed
		arg[3] :	std ped probability
		arg[4] :	car probability
		arg[5] :	rickshaw probability
		arg[6] :	CNG probability
		*/ 
		new Parameters(isGaussian, args[1], args[2], Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]));
		new Controller();
		if (viewMode) {
			new DhakaSimFrame();
		}
		else {
			startSimulation();
		}
	}
}
