/**
 * Created by Torsten Heinrich
 */

package insurance.contract;

import org.economicsl.agent.Agent;
import org.economicsl.contract.handler.ContractHandler;
import org.economicsl.contract.messages.ObligationResponse;
import org.economicsl.contract.obligation.Obligation;
import org.economicsl.contract.obligation.ScheduledObligation;
import org.economicsl.inventory.Contract;
import org.economicsl.inventory.Good;
import org.economicsl.contract.ScheduledContracts;
import insurance.risk.InsurableRisk;
import sim.engine.SimState;

//Class is adapted (with substantial modifications) from Paul Rauwolf's example ESL contracts (FixedBond)
public class InsuranceContract extends ScheduledContracts {

    private State currentState;
    private Agent policyholder;
	private Agent insurer;
	private InsurableRisk risk;
	private double premium;
	private double excess;
	private double deductible;
	private double terminationTime;
	private boolean scheduledEnd;
	private long scheduledEndTime;

    public InsuranceContract(String name, SimState state, ContractHandler handler, Agent policyholder, Agent insurer, InsurableRisk risk, double runtime, double premium, double excess, double deductible, boolean scheduledEnd, long scheduledEndTime) {
		super(name, state, handler);
		
		currentState = State.SETUP;
		this.policyholder = policyholder;
		this.insurer = insurer;
		this.premium = premium;
		this.excess = excess;
		this.deductible = deductible;
		this.risk = risk;
		this.terminationTime = state.schedule.getSteps() + runtime + 1.0; //one timestep to set up the org.economicsl.contract, transfer the premium etc.
		
		this.scheduleEvent(requestNextObligation(state));
		
    }

    public InsuranceContract(String name, SimState state, ContractHandler handler, Agent policyholder, Agent insurer, InsurableRisk risk, double runtime, double premium, double excess, boolean scheduledEnd, long scheduledEndTime) {
		this(name, state, handler, policyholder, insurer, risk, runtime, premium, excess, 0.0, scheduledEnd, scheduledEndTime);
    }


    public ScheduledObligation requestNextObligation(SimState state) {

		Obligation o = null;
		Double time = new Double(1.0);

		switch (this.currentState) {

		case SETUP:
			o = new Obligation(this.insurer, this.policyholder, new Good("cash", premium));
			break;
		case RUNNING:
			double nextevent = state.schedule.getSteps() + risk.getTimeToNextEvent();
			if (nextevent < this.terminationTime) {
				double claim = Math.min(this.excess, risk.getSizeOfEvent()) - this.deductible;
				o = new Obligation(this.policyholder, this.insurer, new Good("cash", claim));
				time = nextevent;
			}
			else {
				this.currentState = State.TERMINATED;
			}
			break;
		}
		
		double eventTime = state.schedule.getSteps() + time;
		if ((!this.scheduledEnd) || (this.scheduledEndTime >= eventTime)) {
			return (new ScheduledObligation(o, eventTime));
		} else {
			return null;
		}
    }

    @Override
    public void handleResponse(ObligationResponse response) {

		// switch state to DEFAULT if the response is false
		if (!response.getFilled()) {
			this.currentState = State.DEFAULT;
			printObligation(response.getObligation(), false);
			return;
		}

		printObligation(response.getObligation(), true);

		// change the state based on the response to the previous obligation
		switch (this.currentState) {

			case SETUP:
				this.currentState = State.RUNNING;
				break;
			/*case TERMINATING:
				this.currentState = State.TERMINATED;
				break;*/
		}
		
	}

    private void printObligation(Obligation o, boolean fulfilled) {	//printObligation method identical from Paul Rauwolf's FixedBond class

		if (o == null) {
			return;
		}
		Agent from = o.getFrom();
		Agent to = o.getTo();
		String what = o.getWhat().getName();
		Double quantity = 1.0;

		if (o.getWhat() instanceof Good) {
			quantity = ((Good) o.getWhat()).getQuantity();
		}
		if (fulfilled) {
			System.out.println("The current state is: " + this.currentState + ". Therefore, " + from.getName() + " gave "
				+ to.getName() + " " + quantity + " of " + what);
		} else {
			System.out.println("The current state is: " + this.currentState + ". " + from.getName() + " defaulted on obligation to pay " 
				+ to.getName() + " " + quantity + " of " + what);			
		}

		try {
			System.out.println("Agent " + from.getName() + " has $" + from.getInventory().getGood("cash").getQuantity());
			System.out.println("Agent " + to.getName() + " has $" + to.getInventory().getGood("cash").getQuantity());
		}
		catch (Exception e) {

		}

	}

	private enum State {
	    SETUP, RUNNING, DEFAULT, TERMINATED
    }

	@Override
	public Contract addition(Contract c) {
		return null;
	}

}
