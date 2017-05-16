package insuranceesl.insurance.upstream;

import org.economicsl.Trade;
import org.economicsl.Simulation;
import insuranceesl.insurance.upstream.ExplicitObligation;
import org.economicsl.Agent;

//public class ObligationHandlingAgent extends Agent{
public class ObligationHandlingAgent extends Trade{
	
	public ObligationHandlingAgent (Simulation simulation) {
		super("", simulation);
	}

	public ObligationHandlingAgent (String name, Simulation simulation) {
		super(name, simulation);
	}

    public void step () {
        super.step();
        this.obligationsAndGoodsMailbox.fulfilMaturedRequests();
    }

}
