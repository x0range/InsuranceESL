/**
 * Created by Torsten Heinrich
 */
package insurance.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.IntStream;

import jdistlib.Exponential;
//import jdistlib.Uniform;
import jdistlib.generic.GenericDistribution;
import jdistlib.rng.MersenneTwister;
import jdistlib.rng.RandomEngine;

public class ExpectedValueMC {

    public double getEV(GenericDistribution dist, Integer sampleSizeIn, Double min, Double max, Double defaultVal, Boolean print) {
		int sampleSize;
		sampleSize = (sampleSizeIn == null) ? 1000 : sampleSizeIn;
		ArrayList<Double> rvs = this.populateArray(dist, sampleSize, min, max, defaultVal);
		
		if (print) System.out.println(rvs);
		
		Double sum = 0.;
		for (Double rv : rvs) {
				sum += rv;
			}
			return sum / rvs.size();
		}
		
		
	//TODO: Separate min and max default values?
	public ArrayList<Double> populateArray(GenericDistribution dist, int sampleSize, Double min, Double max, Double defaultVal) {
		Double rv;
		ArrayList<Double> rvs = new ArrayList<Double>();
		
		//Create new instance of RandomEngine with new seed, otherwise duplicates in the rvs of successive method calls are possible.
		RandomEngine randomE = new MersenneTwister(System.nanoTime());
		
		while (rvs.size() < sampleSize) {
			
			//draw random variates
			IntStream.range(0, sampleSize - rvs.size()).forEach($ -> rvs.add(dist.random(randomE)));	//throws deprecation warning
			
			if (min != null || max != null) {	//If the distribution has boundaries without default value to fall back to, delete rvs outside the boundaries, new ones will be drawn in the next iteration.
				ListIterator<Double> iterator = rvs.listIterator();
				if (defaultVal == null ) {
					while (iterator.hasNext()) {
						rv = iterator.next();
						if (min != null && rv < min) iterator.remove();
						if (max != null && rv > max) iterator.remove();
					}
				} else {						//If the distribution has boundaries with default value, replace inadmissible values by default value. (This is useful for e.g. applying excess values in insurance.)
					while (iterator.hasNext()) {
						rv = iterator.next();
						if (min != null && rv < min) iterator.set(defaultVal);
						if (max != null && rv > max) iterator.set(defaultVal);
					}
				}
			}
		}
		return rvs;
	}
	
		
	public void test () {
		this.getEV(new Exponential(33.33), null, null, null, null, true);
		this.getEV(new Exponential(33.33), 10, null, null, null, true);
		this.getEV(new Exponential(33.33), 10, null, 40., 40., true);
		this.getEV(new Exponential(33.33), 10, null, 40., null, true);
		this.getEV(new Exponential(33.33), 10, 4., null, 4., true);
	}

}
