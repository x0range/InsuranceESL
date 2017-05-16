/**
 * Created by Torsten Heinrich
 */
package insuranceesl.insurance.agent;

//ESL
import org.economicsl.Simulation;

import insuranceesl.insurance.upstream.ContractHandlingAgent;
import insuranceesl.insurance.risk.InsurableRisk;
import insuranceesl.insurance.riskmodel.RiskModel;
import insuranceesl.insurance.contract.InsuranceContract;

//TODO: may have more than one RiskModel in the future
public class InsuranceFirm extends ContractHandlingAgent {
	
	private RiskModel riskmodel;
	
	private Simulation simulation;

    //private ArrayList<InsuranceContract>() contractList; //TODO: use parent contract implementation

	public InsuranceFirm(Simulation simulation, RiskModel riskmodel){
		this("", simulation, riskmodel);
	}
	
	public InsuranceFirm(String name, Simulation simulation, RiskModel riskmodel){
		super(name, simulation);
		this.riskmodel = riskmodel;
		this.simulation = simulation;
		//this.contractList = new ArrayList<InsurableRisk>();
	}
	
	public void createContractOffer(InsurableRisk risk, double suggestedRuntime, double suggestedExcess, double suggestedDeductible, boolean scheduledEnd, long scheduledEndTime) {
		double offeredPremium = riskmodel.evaluate(suggestedRuntime, suggestedExcess, suggestedDeductible);
		InsuranceContract InsContract = new InsuranceContract("", simulation, risk.getOwner(), this, risk, suggestedRuntime, offeredPremium, suggestedExcess, suggestedDeductible, scheduledEnd, scheduledEndTime);
		this.obligationsAndGoodsMailbox.addToContractsOutbox(InsContract);
		risk.getOwner().receiveContract(InsContract);
	}

	public void step() {
		super.step();
     	System.out.println("Iterate Insurance Firm " + this.name + "; Cash " + this.getTotalCash() + ".");		
	}
}
	
