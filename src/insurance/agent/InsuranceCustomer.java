/**
 * Created by Torsten Heinrich
 */

package insurance.agent;

import insurance.contract.InsuranceContract;
import insurance.risk.InsurableRisk;
import jdistlib.Uniform;
import jdistlib.rng.MersenneTwister;
import org.economicsl.agent.SimpleScheduledAgent;
import sim.engine.SimState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//ESL
//JDistLib from: http://jdistlib.sourceforge.net/javadoc/


public class InsuranceCustomer extends SimpleScheduledAgent {

    private final MersenneTwister randomGenerator;
    private List<InsuranceFirm> insurerList;
	private List<InsurableRisk> risks;
	private org.economicsl.contract.handler.AutomaticContractHandler handler;
	private SimState insuranceSimulationState;
	private boolean scheduledEnd;
	private long scheduledEndTime;	
	
	//TODO: it seems wrong to drag  boolean scheduledEnd, long scheduledEndTime through several classes. Change this?
	public InsuranceCustomer(List<InsuranceFirm> insurerList, org.economicsl.contract.handler.AutomaticContractHandler handler, SimState state, boolean scheduledEnd, long scheduledEndTime, MersenneTwister seed) {
		this("", insurerList, handler, state, scheduledEnd, scheduledEndTime, seed);
	}

	public InsuranceCustomer(String name, List<InsuranceFirm> insurerList, org.economicsl.contract.handler.AutomaticContractHandler handler, SimState state, boolean scheduledEnd, long scheduledEndTime, MersenneTwister seed) {
		super(name, state);
		this.insurerList = insurerList;
		this.handler = handler;
		this.insuranceSimulationState = state;
		this.risks = new ArrayList<InsurableRisk>();
		this.scheduledEnd = scheduledEnd;
		this.scheduledEndTime = scheduledEndTime;
		this.randomGenerator = new MersenneTwister(seed.nextLong());
	}
	
	@Override
	public void step(SimState state) {
		this.randomAddRisk();
		super.step(state);
	}
	
	public void randomAddRisk() {
		Double r = Uniform.random(0, 1, this.randomGenerator);
		//Double r = Uniform.random(0, 1, this.randomE);
		if (r > .9) {
			InsurableRisk risk = new InsurableRisk(this.randomGenerator.nextLong());
			this.risks.add(risk);
			if (true) { 			//always seek insurance coverage for risk, should be changed if desired otherwise (e.g. if the org.economicsl.agent should actually evaluate whether she wants coverage
				this.getInsuranceCoverage(risk);
			}
		}
	}
	
	public void getInsuranceCoverage(InsurableRisk risk) {
		double suggestedRuntime = risk.getRuntime();
		double suggestedExcess = risk.getValue();
		double suggestedDeductible = 0.0;
		
		//select cheapest firm
		Collections.shuffle(this.insurerList);
		Double bestPremiumQuote = Double.MAX_VALUE;
		InsuranceFirm chosenInsurer = null;
		for (int i = 0; i < insurerList.size(); i++) {
			double quote = insurerList.get(i).acceptInsuranceContract(risk, suggestedRuntime, suggestedExcess, suggestedDeductible);
			if (quote < bestPremiumQuote) {
				bestPremiumQuote = quote;
				chosenInsurer = insurerList.get(i);
			}
		}
		
		//create insurance org.economicsl.contract
		InsuranceContract currentInsuranceContract = new InsuranceContract("", this.insuranceSimulationState, this.handler, this, chosenInsurer, risk, suggestedRuntime, bestPremiumQuote, suggestedExcess, suggestedDeductible, scheduledEnd, scheduledEndTime);
		
		//TODO: should the InsurableRisk know about the InsuranceContract, then we need here something like:
		//risk.addInsurance(currentInsuranceContract);
	}
	
	public void removeRisk(InsurableRisk risk) {
		this.risks.remove(risk);
	}
	
	public Double scheduleNextEvent(SimState state) {
		Double eventTime = state.schedule.getSteps() + 1.;
		if ((!this.scheduledEnd) || (this.scheduledEndTime >= eventTime)) {
			return eventTime;
		} else {
			return null;
		}
	}
}
	
