package thesisfinal;

import java.util.Random;

public class NormalDistribution {
	double factor, mean, standardDeviation;

	public NormalDistribution (double factor, double mean, double standardDeviation) {
		this.factor = factor;
		this.mean = mean;
		this.standardDeviation = standardDeviation;
	}

	protected double getARandomValue () {
		return new Random().nextGaussian() * standardDeviation + mean;
	}

	protected double getARandomValueWithFactor () {
		return factor * (new Random().nextGaussian() * standardDeviation + mean);
	}

	protected double getProbabilityDensity (double x) {
		double powerOfExp = -((x-mean)*(x-mean)) / (2*standardDeviation*standardDeviation);
		return Math.exp(powerOfExp) / (standardDeviation * Math.sqrt(2*Math.PI));
	}

	protected double getProbabilityDensityWithFactor (double x) {
		double powerOfExp = -((x-mean)*(x-mean)) / (2*standardDeviation*standardDeviation);
		return factor * (Math.exp(powerOfExp) / (standardDeviation * Math.sqrt(2*Math.PI)));
	}
}