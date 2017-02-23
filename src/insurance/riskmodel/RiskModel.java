/**
 * Created by Torsten Heinrich
 */
package insurance.riskmodel;

//JDistLib from: http://jdistlib.sourceforge.net/javadoc/
import jdistlib.evd.GeneralizedPareto;
import jdistlib.Exponential;
//import jdistlib.Uniform;
import jdistlib.generic.GenericDistribution;

import insurance.statistics.ExpectedValueMC;

/** TODO: we will at some point need to discuss the intended relation between RiskModel and InsurableRisk:
 *                           - do we only want generic distributions or do we want to create our own distributions
 *                           - or do we want to just disturb the true distributions (from InsurableRisk) a little bit?
 *
 * 	TODO: how is the expectation (for damages and risk incidence/period) computed:
 *          - Currently implemented: MC simulation over generic distribution
 *          - Possible: integral over generic distribution. In this case, the integral (\int{xf(x)}) should be included as constructor agrument. This is to avoid numerical computation of integrals of asymmetric functions. (This would be unlikely to always give the correct result.)
 *          - Possible: distribution function estimated over observations, fitted to predefined function. The integral for expectation (\int{xf(x)}) should be supplied as constructor argument.
 *
 *TODO (DONE, THOUGH IMPRACTICAL.): random number generator draws the same numbers for different agents/risks (apparently for all calls within a certain time window). How to solve this?
 *     DONE through creating new RandomEngine instances with new random seeds
 */
public class  RiskModel {
	private GenericDistribution riskDistribution;	//distribution of expected losses per event
	private GenericDistribution riskPeriod;	    //distribution of expected times between events
	
	public RiskModel(String name, GenericDistribution riskDist, GenericDistribution riskPeriod) {
		this.riskDistribution = riskDist;
		this.riskPeriod = riskPeriod;
	}
	
	public RiskModel() {
		/**
		 * setting default distributions: 
		 *    Power Law with x_min = 10.
		 *                   alpha = 3.
		 *               => PDF(x) = 200 * x^(-3)
		 *    Exponential with lambda = .03
		 *               => PDF(x) = 0.03 * e^(-0.03*x) 
		 */
		this("", new GeneralizedPareto(10., 10.*3., 1./3.), new Exponential(33.33));
	}
	
	public double getDistributionPPF(double tailSize) {	//allows to check for expected size of 1/x period (e.g. 1/200 year) events ... and to compute expected value (.5)
		return this.riskDistribution.quantile(tailSize);
	}

	public double getPPF(double tailSize) {	//alias of getDistributionPPF
		return this.getDistributionPPF(tailSize);
	}

	public double getPeriodPPF(double tailSize) {	//allows to compute expected value (.5)
		return this.riskPeriod.quantile(tailSize);
	}
	
	public double getDistributionInverseCDF(double x) {
		return 1. - this.riskDistribution.cumulative(x);
	}
	
	public double evaluate(double runtime, double excess, double deductible) {
		return this.evaluate(runtime, excess, deductible, 0.15);	//default to 15% expected return (set premium 15% over expected losses)
	}
	
	//TODO: deductible applied correctly?
	public double evaluate(double runtime, double excess, double deductible, double expectedReturn) {
		/** Returns x% (e.g., 15%) over the expected loss which is computed using Monte Carlo Simulations of risk distribution (excess respected) and risk period
		 *                      1/riskPeriod.mean * riskDistribution.mean * runtime - deductible = riskFreq.mean * riskDistribution.mean * runtime - deductible
		 */
		ExpectedValueMC evmc = new ExpectedValueMC();
		
		double distributionExpectedValue = evmc.getEV(this.riskDistribution, null, null, null, excess, false);
		double periodExpectedValue = evmc.getEV(this.riskPeriod, null, null, null, null, false);
				
		double expectedLoss = distributionExpectedValue * (1./periodExpectedValue) * runtime - deductible;
		return expectedLoss * (1. + expectedReturn);
	}
}
