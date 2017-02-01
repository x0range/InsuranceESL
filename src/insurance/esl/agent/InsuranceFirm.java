/**
 * Created by Torsten Heinrich
 */
package insurance.esl.agent;

//ESL
import esl.inventory.Inventory;
import esl.agent.Agent;

import insurance.esl.risk.InsurableRisk;
import insurance.esl.riskmodel.RiskModel;

//TODO: may have more than one RiskModel in the future
public class InsuranceFirm extends Agent {
	
	private RiskModel riskmodel;
	
	public InsuranceFirm(RiskModel riskmodel){
		this("", riskmodel);
	}
	
	public InsuranceFirm(String name, RiskModel riskmodel){
		super(name);
		this.riskmodel = riskmodel;
	}
	
	public double acceptInsuranceContract(InsurableRisk risk, double suggestedRuntime, double suggestedExcess, double sugestedDeductible) {
		double offeredPremium = riskmodel.evaluate(suggestedRuntime, suggestedExcess, sugestedDeductible);
		return offeredPremium;
	}	
}
	
