package insuranceesl.insurance.upstream;

import insuranceesl.insurance.upstream.ObligationHandlingAgent;
import insuranceesl.insurance.upstream.StatedContract;
import insuranceesl.insurance.messaging.OGCMailbox;
import org.economicsl.Simulation;

public class ContractHandlingAgent extends ObligationHandlingAgent{
	
	protected OGCMailbox obligationsAndGoodsMailbox;

	public ContractHandlingAgent (Simulation simulation) {
		this("", simulation);
	}

	public ContractHandlingAgent (String name, Simulation simulation) {
		super(name , simulation);
        this.obligationsAndGoodsMailbox = new OGCMailbox(this);
	}

    public void step () {
        super.step();
		this.obligationsAndGoodsMailbox.step();
        this.obligationsAndGoodsMailbox.fulfilMaturedRequests();
    }
    
    public void receiveContract(StatedContract contract) {
		this.obligationsAndGoodsMailbox.receiveContract(contract);
	}
}
