/**
 * Created by Torsten Heinrich
 */

package insurance;

import java.util.ArrayList;
import java.util.List;

//ESL
import org.economicsl.contract.handler.AutomaticContractHandler;
import org.economicsl.inventory.Good;

import insurance.agent.InsuranceCustomer;
import insurance.agent.InsuranceFirm;
import insurance.riskmodel.RiskModel;

import jdistlib.rng.MersenneTwister;
import sim.engine.SimState;

public class Insurance extends SimState {

	private long numberOfInsurers;
	private long numberOfRiskholders;
	private boolean scheduledEnd;
	private long scheduledEndTime;
	
	public Insurance(long seed) {
		/**
		 *  Set the following default values:
		 *     numberOfInsurers    = 5
		 *     numberOfRiskholders = 100
		 *     scheduledEnd		   = true
		 *     scheduledEndTime    = 200
		 */
		this(seed, 5, 100, true, 200);
	}

	public Insurance(long seed, long numberOfInsurers, long numberOfRiskholders, boolean scheduledEnd, long scheduledEndTime) {
		super(seed);
		this.numberOfInsurers = numberOfInsurers;
		this.numberOfRiskholders = numberOfRiskholders;
		this.scheduledEnd = scheduledEnd;
		this.scheduledEndTime = scheduledEndTime;
	}
	
	public void start() {
         
		super.start();
		MersenneTwister seed = new MersenneTwister(5);
		
		List<InsuranceFirm> insurers = new ArrayList<InsuranceFirm>();
		List<InsuranceCustomer> riskholders = new ArrayList<InsuranceCustomer>();
		//global handler saves memory
		AutomaticContractHandler handler = new AutomaticContractHandler();
		
		for (int i = 1; i <= this.numberOfInsurers; i++) {
			RiskModel riskmodel = new RiskModel();
			InsuranceFirm a = new InsuranceFirm("Insurer " + i, riskmodel);
			try {
				a.getInventory().add(new Good("cash", 100000.0));
			} catch (Exception e) {
				e.printStackTrace();
			}
			insurers.add(a);
		}

		for (int i = 1; i <= this.numberOfRiskholders; i++) {
			//TODO: discuss whether this.insurers, this.handler should be sent to InsuranceCustomer or should just be a class level variable here
			//TODO: insurers should become a global list since the set of insurers will eventually change over the course of the simulation
			//TODO: ... or perhaps not since the list is a pointer, as long as everyone has the same pointer, that is fine. but it should not be a local var of this.start()
			InsuranceCustomer a = new InsuranceCustomer("Risk Holder " + i, insurers, handler, this, this.scheduledEnd, this.scheduledEndTime, seed);
			try {
				a.getInventory().add(new Good("cash", 1000.0));
			} catch (Exception e) {
				e.printStackTrace();
			}
			riskholders.add(a);
		}
	}

	public static void main(String[] args) {
		doLoop(Insurance.class, args);
		System.exit(0);
	}
}
