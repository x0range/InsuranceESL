/**
 * Created by Torsten Heinrich
 */
package insurance.agent;

//ESL
import org.economicsl.agent.Agent;
import insurance.risk.InsurableRisk;
import insurance.riskmodel.RiskModel;

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
	
