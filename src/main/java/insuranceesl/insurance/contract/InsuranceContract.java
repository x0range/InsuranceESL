/**
 * Created by Torsten Heinrich
 */

package insuranceesl.insurance.contract;

import java.util.ArrayList;
import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import org.economicsl.Agent;
import org.economicsl.Action;
import org.economicsl.Simulation;
import org.economicsl.obligations.Obligation;

import insuranceesl.insurance.upstream.ExplicitObligation;
import insuranceesl.insurance.upstream.StatedContract;
import insuranceesl.insurance.upstream.ObligationHandlingAgent;
import insuranceesl.insurance.risk.InsurableRisk;

//Class is adapted (with substantial modifications) from Paul Rauwolf's example ESL contracts (FixedBond)
//public class InsuranceContract extends ScheduledContracts {
public class InsuranceContract extends StatedContract{

    private State currentState;
    private State backupState = null;
    private ObligationHandlingAgent policyholder;
	private ObligationHandlingAgent insurer;
	private InsurableRisk risk;
	private double premium;
	private double excess;
	private double deductible;
	private double terminationTime;
	private boolean scheduledEnd;
	private long scheduledEndTime;
	private String name;
	
	private Obligation premiumObligation = null;

	//constructors
    public InsuranceContract(String name, Simulation simulation, ObligationHandlingAgent policyholder, ObligationHandlingAgent insurer, InsurableRisk risk, double runtime, double premium, double excess, double deductible, boolean scheduledEnd, long scheduledEndTime) {
		super(name, simulation);
		
		currentState = State.PROPOSED;
		
		this.name = name;
		this.policyholder = policyholder;
		this.insurer = insurer;
		this.premium = premium;
		this.excess = excess;
		this.deductible = deductible;
		this.risk = risk;
		this.terminationTime = this.getTime() + runtime;
    	System.out.println("New Contract Proposed: Insurance Contract " + this.name + " for Risk " + this.risk.getName() + " between Agents " 
    	                            + this.insurer.getName() + " (insurer) and " + this.policyholder.getName() + " (risk holder) proposed.");		
    }

    public InsuranceContract(String name, Simulation simulation, ObligationHandlingAgent policyholder, ObligationHandlingAgent insurer, InsurableRisk risk, double runtime, double premium, double excess, boolean scheduledEnd, long scheduledEndTime) {
		this(name, simulation, policyholder, insurer, risk, runtime, premium, excess, 0.0, scheduledEnd, scheduledEndTime);
    }

    //abstract method implementations
    
    public Agent getAssetParty() {
	    return this.policyholder;	
	}

    public Agent getLiabilityParty() {
	    return this.insurer;
	}

    public double getValue(Agent me) {
	    if (me == this.policyholder) {
			return getValueToPolicyholder();
		} else if (me == this.insurer) {
			return getValueToInsurer();			
		} else {
			return 0;
		}
	}

    public double getValueToPolicyholder() {
	    throw new NotImplementedException();
	    //return 0;
	}
	
    public double getValueToInsurer() {
		throw new NotImplementedException();
		//return 0;
	}

    public List<Action> getAvailableActions(Agent me) {
		List<Action> actionList = new ArrayList<Action>();
		return actionList;
	}

    public String getName(Agent me) {
		return this.name;
	}
    
    public void contractAccepted() {
		ExplicitObligation o = new ExplicitObligation(this, this.insurer, this.policyholder, "cash", this.premium, 1, this.simulation);
		this.insurer.sendObligation(this.policyholder, o);
		this.currentState = State.RUNNING;
		this.risk.addInsurance(this);
    	System.out.println("Contract Accepted: Insurance Contract " + this.name + " for Risk " + this.risk.getName() + " between Agents " 
           + this.insurer.getName() + " (insurer) and " + this.policyholder.getName() + " (risk holder) accepted. Premium: " + this.premium + ".");		
	}

    public void contractRejected() {
		this.currentState = State.REJECTED;
	}
    
    public void invokeContract(Double damage) {
		if (this.currentState == State.RUNNING) {
			double claim = Math.min(this.excess, damage) - this.deductible;
    		ExplicitObligation o = new ExplicitObligation(this, this.policyholder, this.insurer, "cash", claim, 1, this.simulation);
    		this.policyholder.sendObligation(this.insurer, o);
	    	System.out.println("Insurance Event: Insurance Contract " + this.name + " for Risk " + this.risk.getName() + " invoked; nsurer " 
	    	      + this.insurer.getName() + " pays " + claim + " to risk holder " + this.policyholder.getName() + ".");
		}
	}
	
	public void terminate() {
		this.currentState = State.TERMINATED;		
    	System.out.println("Insurance Contract " + this.name + " for Risk " + this.risk.getName() + " between Agents " + this.insurer.getName() 
    	                                                     + " (insurer) and " + this.policyholder.getName() + " (risk holder) terminated.");
	}
    
    public void step() {
		if (this.getTime() >= this.terminationTime) {
			this.terminate();
		}
	}
	
	public double getPremium() {
		return this.premium;
	}

	public InsurableRisk getRisk() {
		return this.risk;
	}

    //@Override
	private enum State {
		NONE, REJECTED, PROPOSED, RUNNING, DEFAULT, TERMINATED
    }
}
