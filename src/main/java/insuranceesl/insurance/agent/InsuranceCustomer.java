/**
 * Created by Torsten Heinrich
 */

package insuranceesl.insurance.agent;

//ESL
import org.economicsl.Simulation;
import org.economicsl.Contract;

import insuranceesl.insurance.upstream.ContractHandlingAgent;

import insuranceesl.insurance.contract.InsuranceContract;
import insuranceesl.insurance.upstream.StatedContract;
import insuranceesl.insurance.risk.InsurableRisk;

//JDistLib from: http://jdistlib.sourceforge.net/javadoc/
import net.sourceforge.jdistlib.Uniform;
import net.sourceforge.jdistlib.rng.MersenneTwister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.stream.Collectors;

public class InsuranceCustomer extends ContractHandlingAgent {

    private final MersenneTwister randomGenerator;
    private List<InsuranceFirm> insurerList;
	private List<InsurableRisk> risks;
	private boolean scheduledEnd;
	private long scheduledEndTime;	
	private Simulation simulation;
	
	//TODO: it seems wrong to drag  boolean scheduledEnd, long scheduledEndTime through several classes. Change this?
	public InsuranceCustomer(List<InsuranceFirm> insurerList,  Simulation simulation, boolean scheduledEnd, long scheduledEndTime, MersenneTwister seed) {
		this("", simulation, insurerList, scheduledEnd, scheduledEndTime, seed);
	}

	public InsuranceCustomer(String name, Simulation simulation, List<InsuranceFirm> insurerList, boolean scheduledEnd, long scheduledEndTime, MersenneTwister seed) {
		super(name, simulation);
		this.insurerList = insurerList;
		this.risks = new ArrayList<InsurableRisk>();
		this.scheduledEnd = scheduledEnd;
		this.scheduledEndTime = scheduledEndTime;
		this.randomGenerator = new MersenneTwister(seed.nextLong());
		this.simulation = simulation;
	}
	
	@Override
	public void step() {
		super.step();
		this.randomAddRisk();
		for (InsurableRisk ir : this.risks) {
			ir.step();
		}
		this.evaluateContractOffers();
     	System.out.println("Iterate Insurance Customer " + this.name + "; Cash " + this.getTotalCash() + ".");		
     	//DEBUG
     	HashSet<? extends StatedContract> inboxContracts = this.obligationsAndGoodsMailbox.getContracts_inbox();
		System.out.println("STEP " + inboxContracts.size());
		this.obligationsAndGoodsMailbox.printMailbox();
	}
	
	private InsuranceContract selectBestInsuranceContract(HashSet<InsuranceContract> currentContracts) {
		List<InsuranceContract> currentContractsList = new ArrayList<InsuranceContract>(currentContracts);
		InsuranceContract chosen = null;
		Double bestPremiumQuote = Double.MAX_VALUE;
		Collections.shuffle(currentContractsList);
		for (InsuranceContract cntrct : currentContractsList) {
			if (cntrct.getPremium() < bestPremiumQuote) {
				bestPremiumQuote = cntrct.getPremium();
				chosen = cntrct;
			}
		}
		return chosen;
	}
	
    public void evaluateContractOffers() {
		System.out.println("ECO");
		HashSet<? extends StatedContract> inboxContracts = this.obligationsAndGoodsMailbox.getContracts_inbox();
		System.out.println("E1 " + inboxContracts.size());
		HashSet<InsuranceContract> inboxInsuranceContracts = inboxContracts.stream()
            .filter(cntrct -> cntrct instanceof InsuranceContract)
            .map(InsuranceContract.class::cast)
			.collect(Collectors.toCollection(HashSet::new));
		System.out.println("E2 " + inboxInsuranceContracts.size());
		for (InsurableRisk insRisk : this.risks) {
			//System.out.println("ECO: riskforloop");
    		HashSet<InsuranceContract> irInsuranceContracts = inboxInsuranceContracts.stream()
                .filter(cntrct -> cntrct.getRisk() == insRisk)
		    	.collect(Collectors.toCollection(HashSet::new));
   			//System.out.println("E3 " + irInsuranceContracts.size());
			if (!(irInsuranceContracts.isEmpty())) {
				System.out.println("ECO: notempty");
    			this.obligationsAndGoodsMailbox.removeFromContractsInbox(irInsuranceContracts);
				if (insRisk.getInsuranceContract() == null) {
					System.out.println("ECO: null");
					InsuranceContract chosenContract = this.selectBestInsuranceContract(irInsuranceContracts);
					chosenContract.contractAccepted();
					irInsuranceContracts.remove(chosenContract);
			    }
			    for (InsuranceContract cntrct : irInsuranceContracts) {
					cntrct.contractRejected();
				}
			}
		}
	}
	
	public void randomAddRisk() {
		Double r = Uniform.random(0, 1, this.randomGenerator);
		if (r > .9) {
			InsurableRisk risk = new InsurableRisk(this, this.randomGenerator.nextLong());
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
		
		for (InsuranceFirm insFirm : this.insurerList) {
			insFirm.createContractOffer(risk, suggestedRuntime, suggestedExcess, suggestedDeductible, this.scheduledEnd, this.scheduledEndTime);
		}
	}
	
	public void removeRisk(InsurableRisk risk) {
		this.risks.remove(risk);
	}
}
	
