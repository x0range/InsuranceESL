package insuranceesl.insurance.upstream;

//import org.economicsl.obligation.Obligation;
import org.economicsl.NotEnoughGoods;
import org.economicsl.Agent;
import org.economicsl.Simulation;
import org.economicsl.obligations.Obligation;

import insuranceesl.insurance.upstream.ObligationHandlingAgent;
import insuranceesl.insurance.upstream.StatedContract;

public class ExplicitObligation extends Obligation{

    private String good_name;
    private final boolean PAY_DEFAULTED_CONTRACT = false;
    private ObligationHandlingAgent from;
    private ObligationHandlingAgent to;
    private StatedContract contract;
    private final boolean DEBUGGING = false;
    
	public ExplicitObligation(StatedContract contract, ObligationHandlingAgent recipient, ObligationHandlingAgent debtor, String good_name, double amount, int timeLeftToPay, Simulation simulation) {
        super(contract, amount, timeLeftToPay, simulation);
        this.contract = contract;
        this.from = debtor;
        this.to = recipient;
        this.good_name = good_name;   // why is this not in upstream class (org.economicsl.obligations.Obligation)?
        if (this.DEBUGGING) {
	        System.out.print("DEBUG ExplicitObligation created: ");
			this.printObligation();
			System.out.println("DEBUG ... should have been: Obligation from " + this.from.getName() + " to pay " + amount + " of " + this.good_name + " to " + this.to.getName());
			//System.out.println("DEBUG ... should have been: Obligation from " + debtor.getName() + " to pay " + amount + " of " + this.good_name + " to " + recipient.getName());
		}
    }
    
    @Override
    public void fulfil() {
		if (this.PAY_DEFAULTED_CONTRACT || this.contract.isNotDefault()) {
			try {
			    this.from.give(this.to, this.good_name, this.getAmount());
		        this.setFulfilled();
		    } catch (NotEnoughGoods notEnoughGoods) {
			    this.contract.setDefault();
		    }
		}
	}

}
